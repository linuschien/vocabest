import re
import uuid
import os

script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.abspath(os.path.join(script_dir, "../../../"))
docs_dir = os.path.join(project_root, "docs/01-requirements/PRD")
output_dir = os.path.join(script_dir, "output")
os.makedirs(output_dir, exist_ok=True)

def parse_markdown(filepath, difficulty_level):
    words_data = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    pages = content.split('\x0c')
    
    for i, page in enumerate(pages):
        if not page.strip():
            continue
            
        words_and_mixed = []
        pure_defs = []
        
        for line in page.split('\n'):
            l = line.strip()
            if not l: continue
            if '單字' in l or '解釋' in l or 'Powered by' in l or l.isdigit(): continue
            
            # Check if it starts with English letter
            if re.match(r'^[a-zA-Z]', l):
                # Check for inline Chinese (mixed line)
                match = re.search(r'([\u4e00-\u9fff].*)$', l)
                if match:
                    word = l[:match.start()].strip()
                    inline_def = match.group(1).strip()
                    words_and_mixed.append( (word, inline_def) )
                else:
                    words_and_mixed.append( (l, None) )
            else:
                pure_defs.append(l)
                
        pure_word_count = sum(1 for w, d in words_and_mixed if d is None)
        
        # Merge multi-line definitions
        while len(pure_defs) > pure_word_count and len(pure_defs) > 0:
            merged = False
            for j in range(len(pure_defs) - 1):
                if not re.search(r'[\)\]a-zA-Z]$', pure_defs[j]): 
                    pure_defs[j] = pure_defs[j] + pure_defs[j+1]
                    del pure_defs[j+1]
                    merged = True
                    break
            if not merged:
                pure_defs[-2] = pure_defs[-2] + pure_defs[-1]
                del pure_defs[-1]
                
        def_index = 0
        for word, inline_def in words_and_mixed:
            if inline_def is not None:
                definition = inline_def
            else:
                if def_index < len(pure_defs):
                    definition = pure_defs[def_index]
                    def_index += 1
                else:
                    definition = ""
            
            POS_MAPPING = {
                'n': 'n.', 'v': 'v.', 'vt': 'vt.', 'vi': 'vi.', 
                'adj': 'adj.', 'adv': 'adv.', 'prep': 'prep.', 
                'pron': 'pron.', 'conj': 'conj.', 'num': 'num.', 
                'art': 'art.', 'int': 'int.', 'aux': 'aux.', '片': 'phr.',
                '名詞': 'n.', '動詞': 'v.', '形容詞': 'adj.', '副詞': 'adv.',
                '介系詞': 'prep.', '代名詞': 'pron.', '連接詞': 'conj.',
                '助動詞': 'aux.', '片語': 'phr.', '數字': 'num.',
                '感嘆詞': 'int.'
            }
            pos_pattern = r'[\(\[](' + '|'.join(POS_MAPPING.keys()) + r')[\)\]]'
            
            parts = re.split(pos_pattern, definition, flags=re.IGNORECASE)
            reconstructed = []
            texts_only = []
            found_pos = []
            
            for k in range(1, len(parts), 2):
                original_tag = parts[k].lower()
                if original_tag not in POS_MAPPING:
                    original_tag = parts[k]
                
                std_tag = POS_MAPPING.get(original_tag, original_tag + '.')
                
                if std_tag not in found_pos:
                    found_pos.append(std_tag)
                    
                text = parts[k-1].strip()
                if text:
                    reconstructed.append(f"({std_tag}){text}")
                    texts_only.append(text)
                else:
                    reconstructed.append(f"({std_tag})")
                    
            if parts[-1].strip():
                if reconstructed:
                    reconstructed[-1] += " " + parts[-1].strip()
                else:
                    reconstructed.append(parts[-1].strip())
                texts_only.append(parts[-1].strip())
                    
            pos = ", ".join(found_pos)
            if len(found_pos) <= 1:
                chinese = " ".join(texts_only)
            else:
                chinese = " ".join(reconstructed)
            
            if word not in words_data or words_data[word]['difficulty_level'] > difficulty_level:
                words_data[word] = {
                    'word': word,
                    'parts_of_speech': pos,
                    'chinese_translation': chinese,
                    'target_level': 'JUNIOR_HIGH',
                    'difficulty_level': difficulty_level,
                    'exam_frequency': 3
                }
    return words_data

def generate_sql(words_dict, output_filepath):
    sql_statements = []
    sql_statements.append("/* Seed Data for JUNIOR_HIGH Vocabulary */")
    sql_statements.append("INSERT INTO word_bank (id, word, parts_of_speech, chinese_translation, target_level, difficulty_level, exam_frequency) VALUES")
    
    values = []
    for word, data in words_dict.items():
        w_id = str(uuid.uuid4())
        w_word = data['word'].replace("'", "''")
        w_pos = data['parts_of_speech'].replace("'", "''")
        w_chinese = data['chinese_translation'].replace("'", "''")
        w_level = data['target_level']
        w_diff = data['difficulty_level']
        w_freq = data['exam_frequency']
        
        values.append(f"('{w_id}', '{w_word}', '{w_pos}', '{w_chinese}', '{w_level}', {w_diff}, {w_freq})")
    
    # Join values with comma and end with semicolon
    sql_statements.append(",\n".join(values) + ";")
    
    with open(output_filepath, 'w', encoding='utf-8') as f:
        f.write("\n".join(sql_statements))
        f.write("\n")
        
def main():
    print("Starting Junior High vocabulary data pipeline...")
    
    basic_path = os.path.join(docs_dir, "國中基礎1200單字.md")
    advanced_path = os.path.join(docs_dir, "國中進階800字.md")
    
    print("Parsing basic 1200...")
    basic_words = parse_markdown(basic_path, 1)
    
    print("Parsing advanced 800...")
    advanced_words = parse_markdown(advanced_path, 2)
    
    print("Merging datasets and deduplicating...")
    final_words = {}
    for w, d in advanced_words.items():
        final_words[w] = d
        
    for w, d in basic_words.items():
        if w in final_words:
            final_words[w] = d
        else:
            final_words[w] = d
            
    print(f"Total unique words: {len(final_words)}")
    
    sql_output = os.path.join(output_dir, "V1__Seed_Vocabulary_Junior_High.sql")
    print(f"Generating SQL file to {sql_output} ...")
    generate_sql(final_words, sql_output)
    print("Done!")

if __name__ == "__main__":
    main()
