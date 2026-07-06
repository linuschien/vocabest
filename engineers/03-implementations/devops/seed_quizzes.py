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

try:
    from google import genai
    from google.genai import types
    from google.genai.errors import APIError
except ImportError:
    genai = None

DB_PATH = 'ai_cache.db'
JUNIOR_HIGH_SQL = 'output/V1__Seed_Vocabulary_Junior_High.sql'
SENIOR_HIGH_SQL = 'output/V1__Seed_Vocabulary_Senior_High.sql'

def init_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
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
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS word_bank (
            id TEXT PRIMARY KEY,
            word TEXT,
            parts_of_speech TEXT,
            chinese_translation TEXT,
            target_level TEXT,
            difficulty_level INTEGER,
            exam_frequency INTEGER
        )
    ''')
    
    cursor.execute("SELECT COUNT(*) FROM word_bank")
    if cursor.fetchone()[0] == 0:
        print("Initializing word_bank from SQL files...")
        for filepath in [JUNIOR_HIGH_SQL, SENIOR_HIGH_SQL]:
            if os.path.exists(filepath):
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                cursor.executescript(content)
            else:
                print(f"Warning: {filepath} not found.")
                
    conn.commit()
    return conn



def get_target_question_count(frequency):
    if frequency <= 0:
        return 1
    val = math.ceil(math.log(frequency + 1, 5))
    return max(1, min(5, val))

def generate_command(args):
    if OpenAI is None:
        print("Error: openai package is not installed. Please run `pip install openai`")
        sys.exit(1)
        
    local_client = OpenAI(base_url=args.base_url, api_key="lm-studio")
    google_client = None
    if args.gemini_api_key:
        if genai is None:
            print("Error: google-genai package is not installed. Please run `pip install google-genai`")
            sys.exit(1)
        google_client = genai.Client(api_key=args.gemini_api_key)
    conn = init_db()
    cursor = conn.cursor()
    
    query = "SELECT * FROM word_bank WHERE 1=1"
    params = []
    
    if args.prefix:
        query += " AND word LIKE ?"
        params.append(f"{args.prefix}%")
        
    if getattr(args, 'words', None):
        word_list = [w.strip() for w in args.words.split(',')]
        placeholders = ','.join(['?'] * len(word_list))
        query += f" AND word IN ({placeholders})"
        params.extend(word_list)
        
    cursor.execute(query, params)
    words = cursor.fetchall()
        

    print(f"Found {len(words)} words to process")
    
    total_words = len(words)
    for idx, word_data in enumerate(words, 1):
        word_id = word_data['id']
        word_text = word_data['word']
        target_level = word_data['target_level']
        target_count = get_target_question_count(word_data['exam_frequency'])
        
        if getattr(args, 'override', False):
            cursor.execute("DELETE FROM quiz_question WHERE word_bank_id = ?", (word_id,))
            existing_count = 0
            print(f"[{idx}/{total_words}] Cleared existing questions for '{word_text}' ({target_level}) due to --override.")
        else:
            # Check how many we already have
            cursor.execute("SELECT COUNT(*) FROM quiz_question WHERE word_bank_id = ?", (word_id,))
            existing_count = cursor.fetchone()[0]
        
        if existing_count >= target_count:
            print(f"[{idx}/{total_words}] Skipping '{word_text}' ({target_level}): already has {existing_count}/{target_count} questions.")
            continue # Already generated
            
        needed = target_count - existing_count
        print(f"[{idx}/{total_words}] Generating {needed} question(s) for '{word_text}' ({target_level}) (Target: {target_count})...")
        
        system_prompt = (
            "You are a professional English teacher for Taiwanese students. "
            "Output ONLY a valid JSON object matching this schema. "
            "ABSOLUTELY NO backslashes (\\) or LaTeX math symbols (like $) in your output. Use the unicode symbol '→' for arrows.\n\n"
            "SCHEMA:\n"
            "{\n"
            "  \"questions\": [\n"
            "    {\n"
            "      \"contextual_cloze\": \"Scenario sentence with a blank (use ___ for the blank).\",\n"
            "      \"chinese_translation\": \"Traditional Chinese translation of the sentence.\",\n"
            "      \"correct_answer\": \"The correct answer, properly inflected to fit grammar.\",\n"
            "      \"distractor1\": \"Wrong option 1 (completely different word, same part of speech, plausible but incorrect meaning).\",\n"
            "      \"distractor2\": \"Wrong option 2 (completely different word, same part of speech, plausible but incorrect meaning).\",\n"
            "      \"distractor3\": \"Wrong option 3 (completely different word, same part of speech, plausible but incorrect meaning).\",\n"
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
            f"Target Level: {word_data['target_level']}\n"
            "CRITICAL DISTRACTOR RULES:\n"
            "1. Distractors MUST be COMPLETELY DIFFERENT WORDS from the correct answer, but belong to the SAME PART OF SPEECH.\n"
            "2. DO NOT use morphological variations of the target word (e.g., if the word is 'interesting', DO NOT use 'interested', 'interest', or 'interestingly').\n"
            "3. Distractors should be semantically plausible in the sentence structure but logically incorrect in meaning.\n"
            "4. Distractors must match the target level.\n"
            "5. DO NOT use LaTeX formatting (e.g., use -> or → instead of \\rightarrow).\n"
            f"CRITICAL: The sentence complexity and vocabulary used in the contextual_cloze MUST be appropriate for a {word_data['target_level']} student in Taiwan."
        )
        
        try:
            response = None
            raw_content = None
            if google_client:
                print("  -> Sending request to Google AI Studio...")
                try:
                    google_resp = google_client.models.generate_content(
                        model=args.google_model,
                        contents=user_prompt,
                        config=types.GenerateContentConfig(
                            system_instruction=system_prompt,
                            temperature=0.7,
                            response_mime_type="application/json",
                        )
                    )
                    raw_content = google_resp.text.strip()
                    response = True
                except APIError as e:
                    if e.code == 429:
                        print(f"  -> Google AI Studio rate limit hit (429). Falling back to LM Studio...")
                    else:
                        print(f"  -> Google AI Studio error: {e}. Falling back to LM Studio...")
                    response = None
                except Exception as e:
                    print(f"  -> Google AI Studio error: {e}. Falling back to LM Studio...")
                    response = None
            
            if response is None:
                print("  -> Sending request to LM Studio...")
                response = local_client.chat.completions.create(
                    model=args.model,
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": user_prompt}
                    ],
                    temperature=0.7
                )
                raw_content = response.choices[0].message.content.strip()
            
            # Extract potential JSON strings
            potential_jsons = []
            
            # 1. Best guess: Find the last occurrence of the "questions" key
            last_q_idx = raw_content.rfind('"questions"')
            if last_q_idx != -1:
                start_idx = raw_content.rfind('{', 0, last_q_idx)
                end_idx = raw_content.rfind('}')
                if start_idx != -1 and end_idx != -1:
                    potential_jsons.append(raw_content[start_idx:end_idx+1])
                    
            # 2. Markdown blocks (reverse order, latest first)
            json_blocks = re.findall(r'```(?:json)?\s*(\{.*?\})\s*```', raw_content, re.DOTALL)
            potential_jsons.extend(reversed(json_blocks))
            
            data = None
            for j_str in potential_jsons:
                try:
                    data = json.loads(j_str)
                    break
                except json.JSONDecodeError:
                    continue
                    
            if data is None:
                # Let it crash normally so we can see the error
                data = json.loads(potential_jsons[0] if potential_jsons else raw_content)
            
            # Robust extraction of the questions list
            questions_list = data if isinstance(data, list) else data.get('questions', [])
            
            def sanitize(text):
                if not isinstance(text, str): return text
                text = text.replace('$\\rightarrow$', '→')
                text = text.replace('\\rightarrow', '→')
                text = text.replace('\\\\rightarrow', '→')
                return text
            
            for idx, q in enumerate(questions_list):
                # Sanitize LaTeX out of explanations
                q['explanation_root_affix'] = sanitize(q.get('explanation_root_affix', ''))
                q['explanation_mnemonic'] = sanitize(q.get('explanation_mnemonic', ''))
                
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
            print(f"  -> Successfully saved {len(questions_list)} questions.")
            
        except Exception as e:
            print(f"  -> Error generating for '{word_text}': {e}")
            print(f"  -> Raw output was:\n{raw_content}")
            
    conn.close()

def status_command(args):
    conn = init_db()
    cursor = conn.cursor()
    
    cursor.execute("SELECT * FROM word_bank")
    words = cursor.fetchall()
    
    # Calculate target per level and letter
    target_by_level_letter = defaultdict(lambda: defaultdict(int))
    current_by_level_letter = defaultdict(lambda: defaultdict(int))
    
    for w in words:
        level = w['target_level']
        letter = w['word'][0].upper() if w['word'][0].isalpha() else '#'
        target_by_level_letter[level][letter] += get_target_question_count(w['exam_frequency'])
        
    cursor.execute("SELECT target_level, word, COUNT(*) FROM quiz_question GROUP BY target_level, word")
    for row in cursor.fetchall():
        level, word, count = row
        letter = word[0].upper() if word[0].isalpha() else '#'
        current_by_level_letter[level][letter] += count
        
    print("=== AI Quizzes Generation Status ===")
    all_levels = sorted(list(set(target_by_level_letter.keys()) | set(current_by_level_letter.keys())))
    
    total_target_all = 0
    total_current_all = 0
    
    for level in all_levels:
        print(f"\n--- {level} ---")
        targets = target_by_level_letter[level]
        currents = current_by_level_letter[level]
        all_letters = sorted(list(set(targets.keys()) | set(currents.keys())))
        
        level_target = 0
        level_current = 0
        for letter in all_letters:
            t = targets[letter]
            c = currents[letter]
            level_target += t
            level_current += c
            print(f"[{letter}] Generated: {c:4d} / {t:4d} ({(c/t*100) if t > 0 else 0:5.1f}%)")
            
        print("-" * 35)
        print(f"{level} Subtotal: {level_current} / {level_target} ({(level_current/level_target*100) if level_target > 0 else 0:.1f}%)")
        total_target_all += level_target
        total_current_all += level_current
        
    print("=" * 35)
    print(f"OVERALL TOTAL: {total_current_all} / {total_target_all} ({(total_current_all/total_target_all*100) if total_target_all > 0 else 0:.1f}%)")
    conn.close()

def export_md_command(args):
    conn = init_db()
    cursor = conn.cursor()
    
    query = "SELECT word, target_level, contextual_cloze, chinese_translation, correct_answer, distractor1, distractor2, distractor3, explanation_root_affix, explanation_mnemonic FROM quiz_question WHERE 1=1"
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
        md_content += f"## Word: {r[0]} ({r[1]})\n"
        md_content += f"**Cloze**: {r[2]}\n\n"
        md_content += f"**Translation**: {r[3]}\n\n"
        md_content += f"- [x] {r[4]} (Correct)\n"
        md_content += f"- [ ] {r[5]}\n"
        md_content += f"- [ ] {r[6]}\n"
        md_content += f"- [ ] {r[7]}\n\n"
        md_content += f"**Root/Affix**: {r[8]}\n\n"
        md_content += f"**Mnemonic**: {r[9]}\n\n"
        md_content += "---\n\n"
        
    output_file = 'review_quizzes.md'
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(md_content)
        
    print(f"Exported {len(rows)} questions to {output_file}")
    conn.close()

def export_sql_command(args):
    conn = init_db()
    cursor = conn.cursor()
    
    cursor.execute("SELECT id, word_bank_id, contextual_cloze, chinese_translation, correct_answer, distractor1, distractor2, distractor3, explanation_root_affix, explanation_mnemonic FROM quiz_question ORDER BY word ASC, id ASC")
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
    gen_parser.add_argument("--model", type=str, default="google/gemma-4-26b-a4b-qat", help="Model name to use")
    gen_parser.add_argument("--base-url", type=str, default="http://localhost:1234/v1", help="LLM Base URL")
    gen_parser.add_argument("--google-model", type=str, default="models/gemma-4-26b-a4b-it", help="Google AI Studio model")
    gen_parser.add_argument("--gemini-api-key", type=str, default=os.environ.get("GEMINI_API_KEY"), help="Google API Key")
    gen_parser.add_argument("--prefix", type=str, help="Filter words starting with prefix")
    gen_parser.add_argument("--words", type=str, help="Comma-separated list of words to process")

    gen_parser.add_argument("--override", action="store_true", help="Delete existing questions and force regeneration")
    
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
