import sys
import os
import re
import uuid

FILE_111 = '/home/linus/workspace/vocabest/docs/01-requirements/PRD/高中英文參考詞彙表(111學年度起適用).txt'
FILE_91 = '/home/linus/workspace/vocabest/docs/01-requirements/PRD/高中英文91參考詞彙表.txt'
FILE_108 = '/home/linus/workspace/vocabest/docs/01-requirements/PRD/高中英文參考詞彙表 — 依字母排序.txt'
OUTPUT_FILE = '/home/linus/workspace/vocabest/engineers/03-implementations/devops/output/V1__Seed_Vocabulary_Senior_High.sql'

def expand_word(word_str):
    m = re.match(r'^([\w\-]+)\s+\((.*?)\)$', word_str)
    if m:
        base = m.group(1).strip()
        others = m.group(2).split(',')
        res = [base] + [o.strip() for o in others if o.strip()]
        return [w for w in res if w]
        
    parts = re.split(r'\s*/\s*', word_str)
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
    if pos.startswith('(') and pos.endswith(')'):
        pos = pos[1:-1]
    pos = pos.replace('/', ', ')
    if '.' in pos and ', ' not in pos:
        parts = [p.strip() + '.' for p in pos.split('.') if p.strip()]
        pos = ', '.join(parts)
    pos = pos.replace('.,', ',')
    return pos.strip()

def clean_translation(trans):
    trans = re.sub(r'(?:\s+|^)(n\.|v\.|adj\.|adv\.|prep\.|conj\.|pron\.|art\.|vi\.|vt\.|a\.)\s*', r' (\1) ', trans)
    return trans.strip()

POS_MAP = {
    '名詞': 'n.',
    '動詞': 'v.',
    '形容詞': 'adj.',
    '副詞': 'adv.',
    '介系詞': 'prep.',
    '連接詞': 'conj.',
    '代名詞': 'pron.',
    '冠詞': 'art.',
    '助動詞': 'aux.',
    '感嘆詞': 'int.',
    '片語': 'phr.',
    '縮寫': 'abbr.',
    '縮短形': 'abbr.'
}

def parse_108_trans(raw_trans):
    if '[' not in raw_trans:
        return '', raw_trans.strip()
        
    pos_keys_pattern = '|'.join([re.escape(k) for k in POS_MAP.keys()])
    parts = re.split(fr'\[\s*({pos_keys_pattern})\s*\]', raw_trans)
    
    parsed = []
    # parts[0] is text before the first POS (usually empty)
    for i in range(1, len(parts), 2):
        zh_pos = parts[i].strip()
        meaning = parts[i+1].strip()
        meaning = re.sub(r'^[;；\s]+|[;；\s]+$', '', meaning)
        en_pos = POS_MAP.get(zh_pos, zh_pos)
        parsed.append((en_pos, meaning))
        
    if not parsed:
        return '', raw_trans.strip()
        
    if len(parsed) == 1:
        return parsed[0][0], parsed[0][1].replace('((', '(').replace('))', ')')
    else:
        pos_list = []
        trans_list = []
        for en_pos, meaning in parsed:
            pos_list.append(en_pos)
            trans_list.append(f"({en_pos}){meaning}")
        return ", ".join(pos_list), " ".join(trans_list).replace('((', '(').replace('))', ')')

def parse_108():
    dict_108 = {}
    with open(FILE_108, 'r', encoding='utf-8') as f:
        text = f.read()
        
    text = re.sub(r'(?:\d+\s+)?Back to Top', '', text)
    text = re.sub(r'高中英文參考詞彙表.*?考題', '', text, flags=re.DOTALL)
    text = re.sub(r'\n[A-Z]\s*\n', '\n', text)
        
    pattern = re.compile(r'^(第一級|第二級|第三級|第四級|第五級|第六級|附\s*錄)\s+([a-zA-Z\-\/\.\' ]+)\s+(.*?)(?=\n^(?:第一級|第二級|第三級|第四級|第五級|第六級|附\s*錄)|\Z)', re.MULTILINE | re.DOTALL)
    matches = pattern.finditer(text)
    
    for m in matches:
        lvl_str = m.group(1).strip().replace('　', '').replace(' ', '')
        word = m.group(2).strip()
        rest = m.group(3).strip()
        
        m_freq = re.search(r'\s+(\d+)$', rest)
        if m_freq:
            freq_str = m_freq.group(1)
            raw_trans = rest[:m_freq.start()].strip()
        else:
            freq_str = "0"
            raw_trans = rest
            
        raw_trans = re.sub(r'\s+', ' ', raw_trans)
        
        if lvl_str == '第一級': lvl = 1
        elif lvl_str == '第二級': lvl = 2
        elif lvl_str == '第三級': lvl = 3
        elif lvl_str == '第四級': lvl = 4
        elif lvl_str == '第五級': lvl = 5
        elif lvl_str == '第六級': lvl = 6
        elif lvl_str == '附錄': lvl = 0
        else: lvl = 0
        
        freq = int(freq_str)
        pos, trans = parse_108_trans(raw_trans)
        
        sub_words = [w.strip() for w in re.split(r'\s*/\s*', word) if w.strip()]
        for sw in sub_words:
            dict_108[sw.lower()] = {
                'word': sw,
                'pos': pos,
                'trans': trans,
                'level': lvl,
                'exam_frequency': freq
            }
    return dict_108

def parse_91():
    dict_91 = {}
    with open(FILE_91, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    current_level = 1
    for line in lines:
        line = line.strip()
        if not line: continue
        if line.startswith('大考中心字彙表 LEVEL'):
            m = re.search(r'LEVEL (\d+)', line)
            if m:
                current_level = int(m.group(1))
            continue
            
        m_en = re.match(r'^([a-zA-Z\-/\.\'\(\)\sé’]+?)(?=\s+\[|\s+(?:n\.|v\.|adj\.|adv\.|prep\.|conj\.|pron\.|art\.|vi\.|vt\.|a\.|int\.|aux\.|phr\.|abbr\.|n\b)|\s+[\u4e00-\u9fa5])', line)
        if m_en:
            raw_word = m_en.group(1).strip()
            rest = line[m_en.end():].strip()
        else:
            m_en2 = re.match(r'^([a-zA-Z\-/\.\'\(\)\sé’]+)', line)
            if m_en2:
                raw_word = m_en2.group(1).strip()
                rest = line[m_en2.end():].strip()
            else:
                raw_word = line
                rest = ''
                
        rest = re.sub(r'^\[[^\]]+\]\s*/*\s*', '', rest)
        rest = re.sub(r'\[[^A-Z\]]*\]\s*/*\s*', '', rest)
        rest = rest.strip()
        rest = re.sub(r'^\(\d+\)\s*', '', rest)
        
        m_pos = re.match(r'^(n\.|v\.|adj\.|adv\.|prep\.|conj\.|pron\.|art\.|vi\.|vt\.|a\.|int\.|aux\.|phr\.|abbr\.|n\b)(.*)', rest)
        if m_pos:
            pos = m_pos.group(1).strip()
            trans = m_pos.group(2).strip()
        else:
            pos = ''
            trans = rest
            
        trans = re.sub(r'^\s*/\s*', '', trans)
            
        clean_pos = format_pos(pos.replace('a.', 'adj.'))
        clean_trans = clean_translation(trans)
        clean_trans = clean_trans.replace('`midɪə', 'midɪə')
        
        tokens = expand_word(raw_word)
        for t in tokens:
            clean_t = t.lower()
            if not clean_t: continue
            
            # Since some words have multiple senses in 91 (e.g. seal1, seal2)
            # they are on separate lines now but the digits were removed
            # We want to keep all meanings.
            if clean_t not in dict_91:
                dict_91[clean_t] = []
                
            dict_91[clean_t].append({
                'word': t,
                'pos': clean_pos,
                'trans': clean_trans,
                'level': current_level
            })
            
    return dict_91

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
            
            valid_pos = {'n.', 'v.', 'adj.', 'adv.', 'prep.', 'conj.', 'pron.', 'art.', 'vi.', 'vt.', 'phr.', 'abbr.', 'aux.', 'n', 'v', 'adj', 'adv', 'prep', 'conj', 'pron', 'art', 'vi', 'vt', 'phr', 'abbr', 'aux'}
            while len(parts) > 1:
                chunks = re.split(r'[/()\s]+', parts[-1].lower())
                chunks = [c for c in chunks if c]
                if all(c in valid_pos for c in chunks) and chunks:
                    parts = parts[:-1]
                else:
                    break
                    
            word_str = " ".join(parts)
            
            expanded_words = expand_word(word_str)
            for w in expanded_words:
                if not w: continue
                dict_111[w.lower()] = {
                    'word': w,
                    'level': level,
                }
            buffer = []
            
    # Now parse the appendix
    appendix_start = -1
    for i, line in enumerate(lines):
        if "附錄" in line and i > 0 and "104" in lines[i-1]:
            appendix_start = i
            break
            
    if appendix_start != -1:
        appendix_text = ""
        for line in lines[appendix_start:]:
            line = line.strip()
            if not line or line in ('附錄', '104'): continue
            
            # Skip headers (no commas and starts with uppercase)
            if ',' not in line and line[0].isupper():
                continue
            else:
                if appendix_text and not appendix_text.strip().endswith(','):
                    appendix_text += ","
                appendix_text += " " + line
                
        items = [item.strip() for item in appendix_text.split(',')]
        # Filter out empty items
        items = [item for item in items if item]
        
        for item in items:
            expanded = expand_word(item)
            for w in expanded:
                if not w: continue
                # Only add if it's not already in the main list, or if we want to overwrite?
                # Actually, appendix words are level 0. If they are already in the main list (e.g. Monday), we shouldn't overwrite their level!
                if w.lower() not in dict_111:
                    dict_111[w.lower()] = {
                        'word': w,
                        'level': 0,
                    }
                    
    return dict_111

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

def main():
    print("Parsing 108 list...")
    dict_108 = parse_108()
    
    print("Parsing 91 list...")
    dict_91 = parse_91()
    
    print("Parsing 111 list...")
    dict_111 = parse_111()
    
    print(f"108 list items: {len(dict_108)}")
    print(f"91 list items: {len(dict_91)}")
    print(f"111 list items: {len(dict_111)}")

    final_entries = {}
    
    # Base list is 91
    for token_91, entries_91 in dict_91.items():
        w = entries_91[0]['word']
        lookup_key = token_91
        
        if lookup_key in dict_108:
            data_108 = dict_108[lookup_key]
            pos = data_108['pos']
            trans = data_108['trans']
            lvl = dict_111[lookup_key]['level'] if lookup_key in dict_111 else data_108['level']
            freq = data_108['exam_frequency']
        else:
            # Fallback to 91
            # Combine if multiple entries
            pos_set = []
            trans_parts = []
            for entry in entries_91:
                entry_pos = entry['pos']
                if entry_pos and entry_pos not in pos_set:
                    pos_set.append(entry_pos)
                
                # if multiple entries, prefix with pos
                if entry_pos and (len(entries_91) > 1 or ',' in entry_pos):
                    trans_parts.append(f"({entry_pos}){entry['trans']}")
                else:
                    trans_parts.append(entry['trans'])
                    
            pos = ", ".join(pos_set)
            trans = " ".join(trans_parts)
            lvl = dict_111[lookup_key]['level'] if lookup_key in dict_111 else entries_91[0]['level']
            freq = 0
            
            if not pos.strip():
                wl = w.lower()
                if wl == 'according to': pos = 'phr.'
                elif wl in ('good-bye', 'bye-bye', 'cell-phone', 'cellular phone', 'hairstyle', 'hi-fi', 'motion picture', 'wed.', 'wednesday'): pos = 'n.'
                elif wl == 'pm': pos = 'adv.'
            
        final_entries[w.lower()] = {
            'word': w,
            'pos': pos,
            'trans': trans,
            'level': lvl,
            'exam_frequency': freq
        }
        
    # Add missing words from 111
    for token_111, data_111 in dict_111.items():
        w = data_111['word']
        if w.lower() not in final_entries:
            if w.lower() in dict_108:
                data_108 = dict_108[w.lower()]
                pos = data_108['pos']
                trans = data_108['trans']
                lvl = data_111['level']
                freq = data_108['exam_frequency']
            else:
                pos = "[FIXME]"
                trans = "[FIXME]"
                lvl = data_111['level']
                freq = 0
                
            final_entries[w.lower()] = {
                'word': w,
                'pos': pos,
                'trans': trans,
                'level': lvl,
                'exam_frequency': freq
            }
            
    print(f"Total merged entries: {len(final_entries)}")
    
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
        freq = item['exam_frequency']
        
        w_id = existing_state.get(w, {}).get('uuid', str(uuid.uuid4()))
        
        if w in existing_state:
            if pos == '[FIXME]' and existing_state[w]['parts_of_speech'] != '[FIXME]':
                pos = existing_state[w]['parts_of_speech']
            if trans == '[FIXME]' and existing_state[w]['chinese_translation'] != '[FIXME]':
                trans = existing_state[w]['chinese_translation']
                
        w_escaped = w.replace("'", "''")
        pos_escaped = pos.replace("'", "''")
        trans_escaped = trans.replace("'", "''")
        
        values.append(f"('{w_id}', '{w_escaped}', '{pos_escaped}', '{trans_escaped}', 'SENIOR_HIGH', {lvl}, {freq})")
        
    sql_statements.append(",\n".join(values) + ";")
    
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        f.write("\n".join(sql_statements))

if __name__ == '__main__':
    main()
