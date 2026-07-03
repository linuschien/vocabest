import os
import re
import uuid

def parse_tsv(filepath):
    """Parses a TSV file and returns a dictionary of words mapping to their metadata."""
    words_data = {}
    
    POS_MAPPING = {
        'n': 'n.', 'v': 'v.', 'vt': 'vt.', 'vi': 'vi.', 
        'adj': 'adj.', 'adv': 'adv.', 'prep': 'prep.', 
        'pron': 'pron.', 'conj': 'conj.', 'num': 'num.', 
        'art': 'art.', 'int': 'int.', 'aux': 'aux.', '片': 'phr.',
        '名詞': 'n.', '動詞': 'v.', '形容詞': 'adj.', '副詞': 'adv.',
        '介系詞': 'prep.', '代名詞': 'pron.', '連接詞': 'conj.',
        '助動詞': 'aux.', '片語': 'phr.', '數字': 'num.',
        '感嘆詞': 'int.', '冠詞': 'art.'
    }

    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    for line in lines[1:]: # Skip header
        parts = [p.strip() for p in line.strip('\n').split('\t') if p.strip()]
        if not parts:
            continue
            
        word = parts[0].lower()
        # Some words might have multiple spaces like 'contact  lens', normalize spaces
        word = re.sub(r'\s+', ' ', word)
        
        definition = parts[1] if len(parts) > 1 else ""
        freq_str = parts[2] if len(parts) > 2 else ""
        
        try:
            freq = int(freq_str)
        except ValueError:
            freq = 0
            
        pos_list = []
        chinese_list = []
        
        # Regex to find [POS] translation
        matches = re.finditer(r'\[(.*?)\]([^[\]]*)', definition)
        matched_any = False
        for m in matches:
            matched_any = True
            tag = m.group(1).strip()
            content = m.group(2).strip().strip(';')
            
            # Map tag or use it directly with a dot fallback
            std_tag = POS_MAPPING.get(tag.lower(), POS_MAPPING.get(tag, tag + '.'))
            if std_tag not in pos_list:
                pos_list.append(std_tag)
                
            if content:
                chinese_list.append(f"({std_tag}){content}")
            else:
                chinese_list.append(f"({std_tag})")
                
        if not matched_any and definition:
            chinese_list.append(definition)
            
        pos_str = ", ".join(pos_list)
        chinese_str = " ".join(chinese_list)
        
        # If there's only 1 POS type, strip the prefixes from Chinese translation
        if len(set(pos_list)) <= 1 and pos_list:
            chinese_str = " ".join([re.sub(r'^\(.*?\)','', c).strip() for c in chinese_list])
            
        words_data[word] = {
            'word': word,
            'parts_of_speech': pos_str,
            'chinese_translation': chinese_str,
            'exam_frequency': freq
        }
        
    return words_data

def extract_official_words(lines):
    cleaned_lines = []
    for l in lines:
        l = l.strip()
        if not l or l.isdigit() or l.startswith('表'):
            continue
        # Remove prefix "A-  "
        l = re.sub(r'^[A-Z]-\s*', '', l)
        cleaned_lines.append(l)
        
    # Join with comma to prevent missing commas across lines or pages
    text = ",".join(cleaned_lines)
    
    # Replace slashes and parentheses with commas so they get split
    text = text.replace('/', ',').replace('(', ',').replace(')', ',')
    
    words = set()
    parts = [p.strip() for p in text.split(',')]
    for p in parts:
        p = re.sub(r'\s+', ' ', p)
        # Verify it has alphabet letters
        if p and any(c.isalpha() for c in p):
            words.add(p.lower())
    return words

def main():
    print("Starting Junior High vocabulary data pipeline (TSV + Official MD)...")
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_dir = os.path.join(script_dir, "output")
    os.makedirs(output_dir, exist_ok=True)
    
    project_root = os.path.abspath(os.path.join(script_dir, "../../.."))
    docs_dir = os.path.join(project_root, "docs/01-requirements/PRD")
    
    # Parse TSV files
    print("Parsing TSV files...")
    basic_tsv = parse_tsv(os.path.join(docs_dir, "國中基礎1200單字.txt"))
    advanced_tsv = parse_tsv(os.path.join(docs_dir, "國中進階800字.txt"))
    
    # Extract official words
    print("Extracting official words from curriculum guidelines...")
    md_file = os.path.join(docs_dir, "十二年國民基本教育課程綱要語文領域-英語文.md")
    with open(md_file, 'r', encoding='utf-8') as f:
        all_lines = f.readlines()
        
    official_basic = extract_official_words(all_lines[5154:5323])
    official_advanced = extract_official_words(all_lines[5325:5452])
    
    # Combine datasets
    final_words = {}
    
    # 1. First load all TSV words, assigning difficulty based on Official MD presence
    print("Merging TSV datasets and assigning official difficulties...")
    for origin_dict, default_diff in [(basic_tsv, 1), (advanced_tsv, 2)]:
        for w, data in origin_dict.items():
            diff = default_diff
            if w in official_basic:
                diff = 1
            elif w in official_advanced:
                diff = 2
            
            # If word is completely new or has a lower difficulty from official check, apply it
            if w not in final_words or final_words[w]['difficulty_level'] > diff:
                final_words[w] = {
                    'id': str(uuid.uuid4()),
                    'word': data['word'],
                    'parts_of_speech': data['parts_of_speech'],
                    'chinese_translation': data['chinese_translation'],
                    'target_level': 'JUNIOR_HIGH',
                    'difficulty_level': diff,
                    'exam_frequency': data['exam_frequency']
                }

    # 2. Add missing official words as FIXMEs
    print("Injecting missing official words as FIXMEs...")
    for official_set, diff in [(official_basic, 1), (official_advanced, 2)]:
        for w in official_set:
            if w not in final_words:
                final_words[w] = {
                    'id': str(uuid.uuid4()),
                    'word': w,
                    'parts_of_speech': '[FIXME]',
                    'chinese_translation': '[FIXME]',
                    'target_level': 'JUNIOR_HIGH',
                    'difficulty_level': diff,
                    'exam_frequency': 0
                }

    print(f"Total unique words prepared: {len(final_words)}")
    
    # Sort alphabetically
    sorted_words = sorted(final_words.values(), key=lambda x: x['word'].lower())
    
    # Generate SQL
    output_path = os.path.join(output_dir, "V1__Seed_Vocabulary_Junior_High.sql")
    print(f"Generating SQL file to {output_path} ...")
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write("/* Seed Data for JUNIOR_HIGH Vocabulary */\n")
        f.write("INSERT INTO word_bank (id, word, parts_of_speech, chinese_translation, target_level, difficulty_level, exam_frequency) VALUES\n")
        
        for i, data in enumerate(sorted_words):
            # Escape single quotes for SQL insertion
            w = data['word'].replace("'", "''")
            pos = data['parts_of_speech'].replace("'", "''")
            trans = data['chinese_translation'].replace("'", "''")
            
            is_last = (i == len(sorted_words) - 1)
            line = f"('{data['id']}', '{w}', '{pos}', '{trans}', '{data['target_level']}', {data['difficulty_level']}, {data['exam_frequency']})"
            if is_last:
                f.write(line + ";\n")
            else:
                f.write(line + ",\n")
                
    print("Done!")

if __name__ == "__main__":
    main()
