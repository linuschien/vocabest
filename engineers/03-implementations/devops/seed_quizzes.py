import argparse
import sqlite3
import re
import math
import uuid
import json
import os
import sys
from collections import defaultdict
from pathlib import Path

# Try to import openai, fail gracefully if not installed
try:
    from openai import OpenAI
except ImportError:
    OpenAI = None

DB_PATH = 'ai_cache.db'
JUNIOR_HIGH_SQL = 'output/V1__Seed_Vocabulary_Junior_High.sql'
SENIOR_HIGH_SQL = 'output/V1__Seed_Vocabulary_Senior_High.sql'

def init_db():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS quiz_question (
            id TEXT PRIMARY KEY,
            word_bank_id TEXT,
            word TEXT,
            target_level TEXT,
            contextual_cloze TEXT,
            chinese_translation TEXT,
            correct_answer TEXT,
            distractor1 TEXT,
            distractor2 TEXT,
            distractor3 TEXT,
            explanation_root_affix TEXT,
            explanation_mnemonic TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    conn.commit()
    return conn

def parse_sql_file(filepath):
    words = []
    if not os.path.exists(filepath):
        print(f"Warning: {filepath} not found.")
        return words
        
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    pattern = r"\('([^']+)',\s*'([^']+)',\s*'([^']+)',\s*'([^']+)',\s*'([^']+)',\s*(\d+),\s*(\d+)\)"
    for match in re.finditer(pattern, content):
        word_text = match.group(2)
        # Handle escaped single quotes in SQL like ''
        word_text = word_text.replace("''", "'")
        words.append({
            'id': match.group(1),
            'word': match.group(2),
            'parts_of_speech': match.group(3),
            'chinese_translation': match.group(4),
            'target_level': match.group(5),
            'difficulty_level': int(match.group(6)),
            'exam_frequency': int(match.group(7))
        })
    return words

def get_target_question_count(frequency):
    if frequency <= 0:
        return 1
    val = math.ceil(math.log(frequency + 1, 5))
    return max(1, min(5, val))

def generate_command(args):
    if OpenAI is None:
        print("Error: openai package is not installed. Please run `pip install openai`")
        sys.exit(1)
        
    client = OpenAI(base_url=args.base_url, api_key="lm-studio")
    conn = init_db()
    cursor = conn.cursor()
    
    words = parse_sql_file(JUNIOR_HIGH_SQL) + parse_sql_file(SENIOR_HIGH_SQL)
    if args.prefix:
        words = [w for w in words if w['word'].lower().startswith(args.prefix.lower())]
        
    print(f"Found {len(words)} words to process (Prefix filter: {args.prefix})")
    
    for word_data in words:
        word_id = word_data['id']
        word_text = word_data['word']
        target_count = get_target_question_count(word_data['exam_frequency'])
        
        # Check how many we already have
        cursor.execute("SELECT COUNT(*) FROM quiz_question WHERE word_bank_id = ?", (word_id,))
        existing_count = cursor.fetchone()[0]
        
        if existing_count >= target_count:
            continue # Already generated
            
        needed = target_count - existing_count
        print(f"Generating {needed} questions for '{word_text}' (Target: {target_count})...")
        
        system_prompt = (
            "You are a professional English teacher for Taiwanese students. "
            "Generate high-quality English quiz questions in JSON format. "
            "Output ONLY a valid JSON object matching this schema, without any markdown formatting or extra text.\n\n"
            "SCHEMA:\n"
            "{\n"
            "  \"questions\": [\n"
            "    {\n"
            "      \"contextual_cloze\": \"Scenario sentence with a blank (use ___ for the blank).\",\n"
            "      \"chinese_translation\": \"Traditional Chinese translation of the sentence.\",\n"
            "      \"correct_answer\": \"The correct answer, properly inflected to fit grammar.\",\n"
            "      \"distractor1\": \"Wrong option 1 (same part of speech).\",\n"
            "      \"distractor2\": \"Wrong option 2.\",\n"
            "      \"distractor3\": \"Wrong option 3.\",\n"
            "      \"explanation_root_affix\": \"Traditional Chinese explanation of word root/affix.\",\n"
            "      \"explanation_mnemonic\": \"Traditional Chinese mnemonic for memorization.\"\n"
            "    }\n"
            "  ]\n"
            "}"
        )
        
        user_prompt = (
            f"Generate exactly {needed} question(s) for the following vocabulary word:\n"
            f"Word: {word_text}\n"
            f"Part of Speech: {word_data['parts_of_speech']}\n"
            f"Translation: {word_data['chinese_translation']}\n"
            f"Difficulty Level: {word_data['difficulty_level']}\n"
            "Ensure distractors are similar in difficulty and the correct answer fits the grammatical context."
        )
        
        try:
            response = client.chat.completions.create(
                model=args.model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                temperature=0.7
            )
            
            raw_content = response.choices[0].message.content.strip()
            
            # Extract JSON block using regex in case the model adds conversational text
            json_match = re.search(r'\{.*\}', raw_content, re.DOTALL)
            if json_match:
                json_str = json_match.group(0)
            else:
                json_str = raw_content
                
            data = json.loads(json_str)
            
            for idx, q in enumerate(data.get('questions', [])):
                # Use deterministic UUID5 based on word ID and index offset
                q_uuid = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"{word_id}_{existing_count + idx}"))
                
                cursor.execute('''
                    INSERT OR REPLACE INTO quiz_question 
                    (id, word_bank_id, word, target_level, contextual_cloze, chinese_translation, 
                     correct_answer, distractor1, distractor2, distractor3, 
                     explanation_root_affix, explanation_mnemonic)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ''', (
                    q_uuid, word_id, word_text, word_data['target_level'],
                    q['contextual_cloze'], q['chinese_translation'], q['correct_answer'],
                    q['distractor1'], q['distractor2'], q['distractor3'],
                    q['explanation_root_affix'], q['explanation_mnemonic']
                ))
            conn.commit()
            print(f"  -> Successfully saved {len(data.get('questions', []))} questions.")
            
        except Exception as e:
            print(f"  -> Error generating for '{word_text}': {e}")
            print(f"  -> Raw output was:\n{raw_content}")
            
    conn.close()

def status_command(args):
    conn = init_db()
    cursor = conn.cursor()
    
    words = parse_sql_file(JUNIOR_HIGH_SQL) + parse_sql_file(SENIOR_HIGH_SQL)
    
    # Calculate target per letter
    target_by_letter = defaultdict(int)
    current_by_letter = defaultdict(int)
    
    for w in words:
        letter = w['word'][0].upper() if w['word'][0].isalpha() else '#'
        target_by_letter[letter] += get_target_question_count(w['exam_frequency'])
        
    cursor.execute("SELECT word, COUNT(*) FROM quiz_question GROUP BY word")
    for row in cursor.fetchall():
        word, count = row
        letter = word[0].upper() if word[0].isalpha() else '#'
        current_by_letter[letter] += count
        
    print("=== AI Quizzes Generation Status ===")
    all_letters = sorted(list(set(target_by_letter.keys()) | set(current_by_letter.keys())))
    
    total_target = 0
    total_current = 0
    for letter in all_letters:
        t = target_by_letter[letter]
        c = current_by_letter[letter]
        total_target += t
        total_current += c
        print(f"[{letter}] Generated: {c:4d} / {t:4d} ({(c/t*100) if t > 0 else 0:5.1f}%)")
        
    print("-" * 35)
    print(f"TOTAL Generated: {total_current} / {total_target} ({(total_current/total_target*100) if total_target > 0 else 0:.1f}%)")
    conn.close()

def export_md_command(args):
    conn = init_db()
    cursor = conn.cursor()
    
    query = "SELECT word, contextual_cloze, chinese_translation, correct_answer, distractor1, distractor2, distractor3, explanation_root_affix, explanation_mnemonic FROM quiz_question WHERE 1=1"
    params = []
    
    if args.level:
        query += " AND target_level = ?"
        params.append(args.level)
        
    if args.prefix:
        query += " AND word LIKE ?"
        params.append(f"{args.prefix}%")
        
    if args.words:
        word_list = [w.strip() for w in args.words.split(',')]
        placeholders = ','.join(['?'] * len(word_list))
        query += f" AND word IN ({placeholders})"
        params.extend(word_list)
        
    query += f" LIMIT {args.limit}"
    
    cursor.execute(query, params)
    rows = cursor.fetchall()
    
    md_content = "# AI Quizzes Review\n\n"
    for r in rows:
        md_content += f"## Word: {r[0]}\n"
        md_content += f"**Cloze**: {r[1]}\n\n"
        md_content += f"**Translation**: {r[2]}\n\n"
        md_content += f"- [x] {r[3]} (Correct)\n"
        md_content += f"- [ ] {r[4]}\n"
        md_content += f"- [ ] {r[5]}\n"
        md_content += f"- [ ] {r[6]}\n\n"
        md_content += f"**Root/Affix**: {r[7]}\n\n"
        md_content += f"**Mnemonic**: {r[8]}\n\n"
        md_content += "---\n\n"
        
    output_file = 'review_quizzes.md'
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(md_content)
        
    print(f"Exported {len(rows)} questions to {output_file}")
    conn.close()

def export_sql_command(args):
    conn = init_db()
    cursor = conn.cursor()
    
    cursor.execute("SELECT id, word_bank_id, contextual_cloze, chinese_translation, correct_answer, distractor1, distractor2, distractor3, explanation_root_affix, explanation_mnemonic FROM quiz_question")
    rows = cursor.fetchall()
    
    buckets = defaultdict(list)
    for r in rows:
        q_id = r[0]
        # Hash bucket based on UUID hex
        bucket_idx = int(q_id.replace('-', ''), 16) % 100
        buckets[bucket_idx].append(r)
        
    os.makedirs('output/quizzes', exist_ok=True)
    
    for bucket_idx, questions in buckets.items():
        filename = f"output/quizzes/V2_{bucket_idx:03d}__Seed_Quizzes.sql"
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(f"/* Seed Data for Quizzes - Bucket {bucket_idx:03d} */\n")
            f.write("INSERT INTO quiz_question (id, word_bank_id, contextual_cloze, chinese_translation, correct_answer, distractor1, distractor2, distractor3, explanation_root_affix, explanation_mnemonic) VALUES\n")
            
            values = []
            for q in questions:
                # Escape single quotes for SQL
                safe_q = [str(x).replace("'", "''") for x in q]
                val_str = f"('{safe_q[0]}', '{safe_q[1]}', '{safe_q[2]}', '{safe_q[3]}', '{safe_q[4]}', '{safe_q[5]}', '{safe_q[6]}', '{safe_q[7]}', '{safe_q[8]}', '{safe_q[9]}')"
                values.append(val_str)
                
            f.write(",\n".join(values) + ";\n")
            
    print(f"Exported {len(rows)} questions across {len(buckets)} SQL files in 'output/quizzes/'.")
    conn.close()

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Offline Script for US-00-03: AI Batch Generation of Static Quiz Banks")
    subparsers = parser.add_subparsers(dest="command", required=True)

    # generate command
    gen_parser = subparsers.add_parser("generate", help="Generate questions via LLM and save to SQLite")
    gen_parser.add_argument("--model", type=str, default="gemma4", help="Model name to use")
    gen_parser.add_argument("--base-url", type=str, default="http://localhost:1234/v1", help="LLM Base URL")
    gen_parser.add_argument("--prefix", type=str, help="Filter words starting with prefix")
    
    # status command
    status_parser = subparsers.add_parser("status", help="Check generation status grouped by letter")
    
    # export-md command
    md_parser = subparsers.add_parser("export-md", help="Export DB to Markdown for review")
    md_parser.add_argument("--limit", type=int, default=100, help="Max number of words to export")
    md_parser.add_argument("--level", type=str, help="Filter by target level (e.g. JUNIOR_HIGH)")
    md_parser.add_argument("--words", type=str, help="Comma-separated list of words to export")
    md_parser.add_argument("--prefix", type=str, help="Filter words starting with prefix")
    
    # export-sql command
    sql_parser = subparsers.add_parser("export-sql", help="Export DB to 100 V2__Seed_Quizzes bucket SQL files")
    
    args = parser.parse_args()
    
    if args.command == "generate":
        generate_command(args)
    elif args.command == "status":
        status_command(args)
    elif args.command == "export-md":
        export_md_command(args)
    elif args.command == "export-sql":
        export_sql_command(args)
