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
        '感嘆詞': 'int.', '感歎詞': 'int.', '冠詞': 'art.',
        '縮寫': 'abbr.', '縮短形': 'abbr.'
    }

    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    for line in lines[1:]: # Skip header
        parts = [p.strip() for p in line.strip('\n').split('\t') if p.strip()]
        if not parts:
            continue
            
        word_raw = parts[0]
        # Some words might have multiple spaces like 'contact  lens', normalize spaces
        word_cased = re.sub(r'\s+', ' ', word_raw)
        word_key = word_cased.lower()
        
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
        parsed_parts = []
        for m in matches:
            matched_any = True
            tag = m.group(1).strip()
            content = m.group(2).strip()
            if content.endswith(';'):
                content = content[:-1].strip()
            
            # Map tag or use it directly with a dot fallback
            if tag.lower() in POS_MAPPING or tag in POS_MAPPING:
                std_tag = POS_MAPPING.get(tag.lower(), POS_MAPPING.get(tag))
                if std_tag not in pos_list:
                    pos_list.append(std_tag)
                parsed_parts.append({"tag": std_tag, "content": content})
            else:
                if parsed_parts:
                    parsed_parts[-1]["content"] += f" [{tag}] {content}"
                    parsed_parts[-1]["content"] = parsed_parts[-1]["content"].strip()
                else:
                    parsed_parts.append({"tag": "", "content": f"[{tag}] {content}".strip()})
                    
        for part in parsed_parts:
            tag = part["tag"]
            content = part["content"]
            if tag:
                if content:
                    chinese_list.append(f"({tag}){content}")
                else:
                    chinese_list.append(f"({tag})")
            else:
                chinese_list.append(content)
                
        if not matched_any and definition:
            chinese_list.append(definition)
            
        pos_str = ", ".join(pos_list)
        chinese_str = " ".join(chinese_list)
        
        # If there's only 1 POS type, strip the prefixes from Chinese translation
        if len(set(pos_list)) <= 1 and pos_list:
            chinese_str = " ".join([re.sub(r'^\(.*?\)','', c).strip() for c in chinese_list])
            
        words_data[word_key] = {
            'word': word_cased,
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
        if re.match(r'^\d+\.\s+[a-zA-Z]', l):
            continue
        # If this line starts a new alphabetical section, ensure previous line has a comma
        if re.match(r'^[A-Z]-\s*', l):
            if cleaned_lines and not cleaned_lines[-1].endswith(','):
                cleaned_lines[-1] += ','
                
        # Remove prefix "A-  "
        l = re.sub(r'^[A-Z]-\s*', '', l)
        
        # Strip ---others: first since it's more specific
        l = re.sub(r'^---others:\s*', ', ', l)
        
        # Replace thematic prefix "---" with a comma to separate it from the previous line's end
        l = re.sub(r'^---\s*', ', ', l)
        
        cleaned_lines.append(l)
        
    # Join with space to re-attach words broken by newlines like "cook\n(cooking)"
    text = " ".join(cleaned_lines)
    
    # Replace slashes with commas so they get split
    text = text.replace('/', ',')
    
    parts = []
    current = []
    in_paren = False
    for char in text:
        if char == '(':
            in_paren = True
            current.append(char)
        elif char == ')':
            in_paren = False
            current.append(char)
        elif char == ',' and not in_paren:
            parts.append("".join(current).strip())
            current = []
        else:
            current.append(char)
    if current:
        parts.append("".join(current).strip())
        
    words = set()
    for p in parts:
        p = re.sub(r'\s+', ' ', p)
        # Verify it has alphabet letters
        if p and any(c.isalpha() for c in p):
            words.add(p.lower())
    return words

def load_existing_sql_state(filepath):
    """Loads existing SQL seed data to preserve UUIDs and manual translations."""
    if not os.path.exists(filepath):
        return {}
    state = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            match = re.search(r"\('([a-f0-9\-]{36})',\s*'(.*?)',\s*'(.*?)',\s*'(.*?)',", line)
            if match:
                uuid_str = match.group(1)
                word = match.group(2).replace("''", "'")
                pos = match.group(3).replace("''", "'")
                trans = match.group(4).replace("''", "'")
                word_key = word.lower()
                state[word_key] = {
                    'uuid': uuid_str,
                    'parts_of_speech': pos,
                    'chinese_translation': trans,
                    'original_casing': word
                }
    return state

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
        
    official_basic_lines = []
    official_advanced_lines = []
    official_thematic_lines = []
    
    current_section = None
    for line in all_lines:
        if "表一、基本 1, 200 字（依字母排列）" in line:
            current_section = 'basic'
            continue
        elif "表二、其他常用 800 字（依字母排列）" in line:
            current_section = 'advanced'
            continue
        elif "表三、參考字彙表" in line:
            current_section = 'thematic'
            continue
        elif "附錄六：國民中學英語文基礎文法句構參考表" in line and current_section == 'thematic':
            break
            
        if current_section == 'basic':
            official_basic_lines.append(line)
        elif current_section == 'advanced':
            official_advanced_lines.append(line)
        elif current_section == 'thematic':
            official_thematic_lines.append(line)
            
    official_basic = extract_official_words(official_basic_lines)
    official_advanced = extract_official_words(official_advanced_lines)
    official_thematic = extract_official_words(official_thematic_lines)
    
    # Combine datasets
    final_words = {}
    
    output_path = os.path.join(output_dir, "V1__Seed_Vocabulary_Junior_High.sql")
    print("Loading existing SQL state to preserve UUIDs and manual translations...")
    existing_state = load_existing_sql_state(output_path)
    
    def get_base_word(word):
        return re.sub(r'\s*\(.*\)', '', word).strip().lower()

    official_basic_lower = {w.lower() for w in official_basic}
    official_advanced_lower = {w.lower() for w in official_advanced}
    official_basic_bases = {get_base_word(w) for w in official_basic}
    official_advanced_bases = {get_base_word(w) for w in official_advanced}

    # 1. First load all TSV words, assigning difficulty based on Official MD presence
    print("Merging TSV datasets and assigning official difficulties...")
    for origin_dict, default_diff in [(basic_tsv, 1), (advanced_tsv, 2)]:
        for w_key, data in origin_dict.items():
            diff = default_diff
            w_lower = w_key.lower()
            
            if w_lower in official_basic_lower or w_lower in official_basic_bases:
                diff = 1
            elif w_lower in official_advanced_lower or w_lower in official_advanced_bases:
                diff = 2
            
            # Use original casing for insertion key
            w_cased = data['word']
            if w_cased not in final_words or final_words[w_cased]['difficulty_level'] > diff:
                final_words[w_cased] = {
                    'id': existing_state.get(w_lower, {}).get('uuid', str(uuid.uuid4())),
                    'word': w_cased,
                    'parts_of_speech': data['parts_of_speech'],
                    'chinese_translation': data['chinese_translation'],
                    'target_level': 'JUNIOR_HIGH',
                    'difficulty_level': diff,
                    'exam_frequency': data['exam_frequency']
                }

    # 2. Add missing official words as FIXMEs
    print("Injecting missing official words as FIXMEs...")
    # Map lowercase to original case for final_words lookup
    final_words_lower_map = {k.lower(): k for k in final_words.keys()}
    
    for official_set, diff in [(official_basic, 1), (official_advanced, 2), (official_thematic, 1)]:
        for w in official_set:
            w_lower = w.lower()
            base_w_lower = get_base_word(w)
            
            if w_lower not in final_words_lower_map and base_w_lower not in final_words_lower_map:
                pos = '[FIXME]'
                trans = '[FIXME]'
                
                # Preserve existing manual translations if present
                if w_lower in existing_state:
                    if existing_state[w_lower]['parts_of_speech'] != '[FIXME]':
                        pos = existing_state[w_lower]['parts_of_speech']
                    if existing_state[w_lower]['chinese_translation'] != '[FIXME]':
                        trans = existing_state[w_lower]['chinese_translation']

                final_words[w] = {
                    'id': existing_state.get(w_lower, {}).get('uuid', str(uuid.uuid4())),
                    'word': w,
                    'parts_of_speech': pos,
                    'chinese_translation': trans,
                    'target_level': 'JUNIOR_HIGH',
                    'difficulty_level': diff,
                    'exam_frequency': 0
                }
                # Update map to prevent duplicates across sets
                final_words_lower_map[w_lower] = w

    print(f"Total unique words prepared: {len(final_words)}")
    
    # Sort alphabetically
    sorted_words = sorted(final_words.values(), key=lambda x: x['word'].lower())
    
    # Generate SQL
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
