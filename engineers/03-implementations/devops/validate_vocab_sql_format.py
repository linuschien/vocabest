import re
import sys

def run_tests():
    if len(sys.argv) < 2:
        print("Usage: python3 validate_vocab_sql_format.py <sql_file>")
        return
        
    sql_file = sys.argv[1]
    
    with open(sql_file, 'r', encoding='utf-8') as f:
        content = f.read()
        
    lines = content.strip().split('\n')
    matches = []
    
    print(f"Testing file: {sql_file}")
    print(f"Total lines: {len(lines)}")
    
    for i, line in enumerate(lines):
        if not line.strip() or line.startswith('INSERT') or line.startswith('--') or line.startswith('/*'): continue
        
        # Format: ('id', 'word', 'pos', 'trans', 'LEVEL_NAME', lvl, freq),
        s = line.strip()
        if not s.startswith("('"):
            print(f"Error parsing line {i+1}: doesn't start with ('")
            print(line[:100])
            continue
            
        if s.endswith("),"):
            s = s[2:-3] # remove (' and ),
        elif s.endswith(");"):
            s = s[2:-3] # remove (' and );
        else:
            print(f"Error parsing line {i+1}: doesn't end with ), or );")
            print(line[:100])
            continue
        parts = s.split("', '")
        if len(parts) >= 5: # id, word, pos, trans, level...
            # The last part is LEVEL_NAME', lvl, freq
            # So parts[-1] looks like "SENIOR_HIGH', 2, 0"
            last_parts = parts[-1].split("', ")
            if len(last_parts) == 2:
                level_name = last_parts[0]
                nums = last_parts[1].split(", ")
                if len(nums) >= 2:
                    lvl, freq = nums[0], nums[1]
                    uid = parts[0]
                    word = parts[1]
                    pos = parts[2]
                    # Trans could contain "', '" so we join it back
                    trans = "', '".join(parts[3:-1])
                    matches.append((uid, word, pos, trans, level_name, lvl, freq))
                    continue
                    
        print(f"Error parsing line {i+1}: {line[:100]}...")
        return
        
    print(f"Total entries found: {len(matches)}")
    valid_lines = [l for l in lines if l.strip() and not l.startswith('INSERT') and not l.startswith('--') and not l.startswith('/*')]
    if len(matches) != len(valid_lines):
        print(f"Error: Parsed {len(matches)} entries but there are {len(valid_lines)} valid lines!")
        return
        
    errors = []
    fixme_count = 0
    
    for row in matches:
        uid, word, pos, trans, level_name, lvl, freq = row
        
        # 1. Empty words
        if not word.strip():
            errors.append(f"[Empty Word] Entry has empty word. ID: {uid}")
            
        # 2. Junk in words
        if re.search(r'\d', word):
            errors.append(f"[Digit in Word] '{word}' contains Arabic numerals.")
            
        if not re.match(r"^[a-zA-Z\-\' .é’\(\)]+$", word):
            errors.append(f"[Strange Word Char] '{word}' contains unusual characters.")
            
        if word.count('(') != word.count(')'):
            errors.append(f"[Unbalanced Parens] '{word}' has unmatched parentheses.")
                
        # 3. POS format
        if '/' in pos:
            errors.append(f"[POS Slash] '{word}' has slash in POS: '{pos}'")
            
        if ',.' in pos:
            errors.append(f"[POS Typo] '{word}' has typo in POS: '{pos}'")
            
        if pos.count('(') != pos.count(')'):
            errors.append(f"[POS Unbalanced Parens] '{word}' POS: '{pos}'")
            
        if '.(' in pos:
            errors.append(f"[POS Invalid Paren] '{word}' POS: '{pos}'")
            
        # 4. Translation format
        if '[FIXME]' in trans:
            fixme_count += 1
            
        # Check for unparenthesized POS at start of translation
        if re.match(r'^(n\.|v\.|adj\.|adv\.|prep\.|conj\.|pron\.|art\.|vi\.|vt\.|a\.)\s', trans):
            errors.append(f"[Trans Unparenthesized POS] '{word}' Translation: '{trans}'")
            
        # Check for nested parentheses like (vt, (vi.))
        if re.search(r'\([^)]*\([^)]*\)[^)]*\)', trans):
            errors.append(f"[Trans Nested Parens] '{word}' Translation: '{trans}'")
            
        # Check for phonetics in translation
        if re.search(r'[ˋ͵əɪɛæɑɔʊʌɝθʃʒŋ]', trans):
            errors.append(f"[Phonetics in Trans] '{word}' Translation contains phonetics: '{trans}'")
            
        # Check for header/footer junk
        if 'Back to Top' in trans or '高中英文參考' in trans or len(trans) > 150:
            errors.append(f"[Trans Junk Text] '{word}' Translation looks like junk: '{trans[:50]}...'")
            
        # 5. Missing POS
        if not pos.strip() and trans != '[FIXME]':
            errors.append(f"[Empty POS] '{word}' has no POS.")
            
    if errors:
        print(f"\n[FAILED] Found {len(errors)} format errors:")
        for e in errors[:50]:
            print(e)
        if len(errors) > 50:
            print(f"... and {len(errors) - 50} more errors.")
    else:
        print("\n[PASSED] All format checks passed successfully!")
        
    print(f"\nTotal FIXMEs remaining: {fixme_count}")

if __name__ == '__main__':
    run_tests()
