import re
import sys

def run_tests():
    if len(sys.argv) < 2:
        print("Usage: python3 test_sql_format.py <sql_file>")
        return
        
    sql_file = sys.argv[1]
    
    with open(sql_file, 'r', encoding='utf-8') as f:
        content = f.read()
        
    # Extract all values tuples
    # Format: ('id', 'word', 'pos', 'trans', 'LEVEL_NAME', lvl, freq)
    matches = re.findall(r"\('(.*?)',\s*'(.*?)',\s*'(.*?)',\s*'(.*?)',\s*'(JUNIOR_HIGH|SENIOR_HIGH)',\s*(\d+),\s*(\d+)\)", content)
    
    print(f"Testing file: {sql_file}")
    print(f"Total entries found: {len(matches)}")
    if len(matches) == 0:
        print("Error: No entries found. Please check the regex or SQL file.")
        return
        
    errors = []
    fixme_count = 0
    
    for row in matches:
        uid, word, pos, trans, level_name, lvl, freq = row
        
        # 1. Empty words
        if not word.strip():
            errors.append(f"[Empty Word] Entry has empty word. ID: {uid}")
            
        # 2. Junk in words
        if not re.match(r"^[a-zA-Z0-9\-\' .é’]+$", word):
            errors.append(f"[Strange Word Char] '{word}' contains unusual characters.")
                
        # 3. POS format
        if '/' in pos:
            errors.append(f"[POS Slash] '{word}' has slash in POS: '{pos}'")
            
        if '.,' in pos or ',.' in pos:
            errors.append(f"[POS Comma Dot] '{word}' has weird comma in POS: '{pos}'")
            
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
