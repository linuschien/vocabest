import sys
import os
import re
import uuid

FILE_111 = '/home/linus/workspace/vocabest/docs/01-requirements/PRD/高中英文參考詞彙表(111學年度起適用).txt'
FILE_91 = '/home/linus/workspace/vocabest/docs/01-requirements/PRD/高中英文91參考詞彙表.txt'
OUTPUT_FILE = '/home/linus/workspace/vocabest/engineers/03-implementations/devops/output/V1__Seed_Vocabulary_Senior_High.sql'

def expand_word(word_str):
    # 1. Handle "we (us, our, ours, ourselves)"
    m = re.match(r'^([\w\-]+)\s*\((.*?)\)$', word_str)
    if m:
        base = m.group(1).strip()
        others = m.group(2).split(',')
        res = [base] + [o.strip() for o in others if o.strip()]
        return [w for w in res if w]
        
    # 2. Handle / and suffixes like (ment), (s), (e)s
    parts = word_str.split('/')
    res = []
    for p in parts:
        p = p.strip()
        if not p: continue
        m2 = re.match(r'^([\w\-]+)\((.*?)\)$', p)
        if m2:
            base = m2.group(1).strip()
            suffix = m2.group(2).strip()
            res.append(base)
            res.append(base + suffix)
        else:
            res.append(p)
    return [w for w in res if w]

def format_pos(pos):
    if not pos: return ""
    # Strip parentheses if they are the only thing surrounding it
    if pos.startswith('(') and pos.endswith(')'):
        pos = pos[1:-1]
        
    # Sometimes there's no slash but just vt.vi.n.
    pos = pos.replace('/', ', ')
    if '.' in pos and ', ' not in pos:
        parts = [p.strip() + '.' for p in pos.split('.') if p.strip()]
        pos = ', '.join(parts)
        
    # Standardize comma
    pos = pos.replace('.,', ',')
    return pos.strip()

def clean_translation(trans):
    # Replace embedded POS with brackets
    # e.g., ' 抽象的 n. 摘要' -> ' 抽象的 (n.) 摘要'
    trans = re.sub(r'(?:\s+|^)(n\.|v\.|adj\.|adv\.|prep\.|conj\.|pron\.|art\.|vi\.|vt\.|a\.)\s*', r' (\1) ', trans)
    return trans.strip()

def parse_111():
    dict_111 = {}
    with open(FILE_111, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    start_idx = 0
    for i, line in enumerate(lines):
        if "依字母排序" in line and "A" in line:
            start_idx = i + 1
            break
            
    buffer = []
    for line in lines[start_idx:]:
        line = line.strip()
        if not line: continue
        if line.startswith("高中英文參考詞彙表") or line.startswith("依字母排序"): continue
        if len(line) == 1 and line.isalpha(): continue
        if line.isdigit() and int(line) > 6: continue
            
        buffer.append(line)
        full_text = " ".join(buffer).strip()
        parts = full_text.split()
        if not parts: continue
            
        last_token = parts[-1]
        if last_token.isdigit() and 1 <= int(last_token) <= 6:
            level = int(last_token)
            parts = parts[:-1]
            if len(parts) > 1 and ('.' in parts[-1] or parts[-1] in ['art.', 'pron.'] or parts[-1].endswith(')')):
                pos = parts[-1]
                word_str = " ".join(parts[:-1])
            else:
                word_str = " ".join(parts)
                pos = ""
                
            expanded_words = expand_word(word_str)
            for w in expanded_words:
                if not w: continue
                dict_111[w.lower()] = {
                    'word': w,
                    'pos': format_pos(pos),
                    'level': level,
                }
            buffer = []
            
    return dict_111

def parse_91():
    dict_91 = {}
    with open(FILE_91, 'r', encoding='utf-8') as f:
        text = f.read()

    level_blocks = re.split(r'大考中心字彙表 LEVEL (\d+) \([^\)]+\)', text)
    
    # We use a multiline regex to extract words.
    # The lookahead `|\Z` ensures we don't accidentally stop at a newline inside a translation.
    pattern = re.compile(
        r'^([^\n\[【\u4e00-\u9fa5]+?)\s*'  
        r'(?:[/\s]*\[[^\]]+\]\s*)*'            
        r'(n\.|v\.|adj\.|adv\.|prep\.|conj\.|pron\.|art\.|vi\.|vt\.|a\.|int\.|aux\.|phr\.|abbr\.)\s*' 
        r'(.+?)'                      
        r'(?=\n[^\n\[【\u4e00-\u9fa5]+?(?:[/\s]*\[[^\]]+\]\s*)*\s*(?:n\.|v\.|adj\.|adv\.|prep\.|conj\.|pron\.|art\.|vi\.|vt\.|a\.|int\.|aux\.|phr\.|abbr\.)|\Z)', 
        re.MULTILINE | re.DOTALL
    )

    for lvl_idx in range(1, len(level_blocks), 2):
        current_level = int(level_blocks[lvl_idx])
        block_text = level_blocks[lvl_idx+1]
        
        for m in pattern.finditer(block_text):
            raw_word = m.group(1).strip()
            # Words can be split across lines like daddy/...\n[...]
            raw_word = raw_word.replace('\n', ' ')
            
            pos = m.group(2).strip()
            
            # Translation could be split across lines; replace newline to join them
            trans = m.group(3).strip()
            trans = trans.replace('\n', '')
            trans = re.sub(r'\s+', ' ', trans)
            
            tokens = re.split(r'\s*/\s*', raw_word)
            for t in tokens:
                clean_t = t.strip('()1234567890').lower()
                if not clean_t: continue
                
                if clean_t not in dict_91:
                    dict_91[clean_t] = []
                    
                display_word = raw_word.split('/')[0].split('(')[0].strip()
                if not display_word: display_word = clean_t
                
                clean_pos = format_pos(pos.replace('a.', 'adj.'))
                clean_trans = clean_translation(trans)
                clean_trans = clean_trans.replace(' 同：tap1', '')
                clean_trans = clean_trans.replace('`midɪə', 'midɪə')
                
                display_word = display_word.replace('’', "'")
                
                dict_91[clean_t].append({
                    'word': display_word,
                    'pos': clean_pos,
                    'trans': clean_trans,
                    'level': current_level,
                    'token': clean_t
                })
                        
    return dict_91

def main():
    print("Parsing 111 list...")
    dict_111 = parse_111()
    
    print("Parsing 91 list...")
    dict_91 = parse_91()
    
    print(f"111 list items: {len(dict_111)}")
    print(f"91 list dictionary keys: {len(dict_91)}")

    used_91_words = set()
    final_entries = {}
    
    for key_111, data_111 in dict_111.items():
        w = data_111['word']
        pos = data_111['pos']
        lvl = data_111['level']
        
        lookup_keys = [
            w.lower(),
            w.split('/')[0].split('(')[0].strip().lower(),
            w.replace('-', '').lower()
        ]
        
        trans = "[FIXME]"
        for k in lookup_keys:
            if k in dict_91:
                entries = dict_91[k]
                trans_parts = []
                for entry in entries:
                    entry_pos = entry['pos']
                    if entry_pos and (len(entries) > 1 or ',' in entry_pos):
                        trans_parts.append(f"({entry_pos}) {entry['trans']}")
                    else:
                        trans_parts.append(entry['trans'])
                    entry['used'] = True
                    used_91_words.add(entry['token'])
                trans = " ".join(trans_parts)
                
                if not pos and entries:
                    pos = ", ".join([e['pos'] for e in entries if e['pos']])
                break
                
        final_entries[w.lower()] = {
            'word': w,
            'pos': pos,
            'level': lvl,
            'trans': trans
        }
        
    for key_91, entries in dict_91.items():
        for data_91 in entries:
            t = data_91['token']
            if t in used_91_words: continue
            
            w = t
            if w.lower() in final_entries: continue
            
            trans_parts = []
            for entry in entries:
                entry_pos = entry['pos']
                if entry_pos and (len(entries) > 1 or ',' in entry_pos):
                    trans_parts.append(f"({entry_pos}) {entry['trans']}")
                else:
                    trans_parts.append(entry['trans'])
                entry['used'] = True
                
            trans = " ".join(trans_parts)
            
            final_entries[w.lower()] = {
                'word': w,
                'pos': data_91['pos'],
                'trans': trans,
                'level': data_91['level']
            }
            used_91_words.add(w)

    print(f"Total merged entries: {len(final_entries)}")

    import os
    def load_existing_sql_state(filepath):
        if not os.path.exists(filepath):
            return {}
        state = {}
        with open(filepath, 'r', encoding='utf-8') as f:
            for line in f:
                match = re.search(r"^\('([a-f0-9\-]{36})',\s*'((?:[^']|'')*)',\s*'((?:[^']|'')*)',\s*'((?:[^']|'')*)',", line)
                if match:
                    uuid_str = match.group(1)
                    word = match.group(2).replace("''", "'")
                    pos = match.group(3).replace("''", "'")
                    trans = match.group(4).replace("''", "'")
                    state[word] = {
                        'uuid': uuid_str,
                        'parts_of_speech': pos,
                        'chinese_translation': trans
                    }
        return state

    print("Loading existing SQL state to preserve UUIDs and manual translations...")
    existing_state = load_existing_sql_state(OUTPUT_FILE)

    sql_statements = []
    sql_statements.append("-- V1__Seed_Vocabulary_Senior_High.sql")
    sql_statements.append("/* Seed Data for SENIOR_HIGH Vocabulary */")
    sql_statements.append("INSERT INTO word_bank (id, word, parts_of_speech, chinese_translation, target_level, difficulty_level, exam_frequency) VALUES")
    
    values = []
    for key in sorted(final_entries.keys()):
        item = final_entries[key]
        w = item['word']
        pos = item['pos']
        lvl = item['level']
        trans = item['trans']
        
        w_id = existing_state.get(w, {}).get('uuid', str(uuid.uuid4()))
        
        # Preserve manual translations for FIXMEs
        if w in existing_state:
            if pos == '[FIXME]' and existing_state[w]['parts_of_speech'] != '[FIXME]':
                pos = existing_state[w]['parts_of_speech']
            if trans == '[FIXME]' and existing_state[w]['chinese_translation'] != '[FIXME]':
                trans = existing_state[w]['chinese_translation']
                
        w_escaped = w.replace("'", "''")
        pos_escaped = pos.replace("'", "''")
        trans_escaped = trans.replace("'", "''")
        freq = 0
        
        values.append(f"('{w_id}', '{w_escaped}', '{pos_escaped}', '{trans_escaped}', 'SENIOR_HIGH', {lvl}, {freq})")
        
    sql_statements.append(",\n".join(values) + ";")
    
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        f.write("\n".join(sql_statements))
        
    print(f"SQL file generated at {OUTPUT_FILE}")
    fixmes = [v for v in values if "[FIXME]" in v]
    print(f"Total FIXMEs: {len(fixmes)}")

if __name__ == '__main__':
    main()
