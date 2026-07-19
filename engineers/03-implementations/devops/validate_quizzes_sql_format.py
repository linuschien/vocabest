import sys
import os

def run_tests():
    if len(sys.argv) < 2:
        print("Usage: python3 validate_quizzes_sql_format.py <sql_file1> [sql_file2 ...]")
        return
        
    sql_files = sys.argv[1:]
    total_errors = 0
    
    for sql_file in sql_files:
        if not os.path.exists(sql_file):
            print(f"File not found: {sql_file}")
            continue
            
        with open(sql_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
        lines = content.split('\n')
        rows = []
        buffer = []
        start_line = 1
        
        for i, line in enumerate(lines):
            # Check if this line is the start of a new SQL row tuple
            # Pattern: ('<uuid>', '<uuid>', ...
            if line.startswith("('") and len(line) >= 42 and line[38:42] == "', '":
                if buffer:
                    rows.append((start_line, '\n'.join(buffer)))
                buffer = [line]
                start_line = i + 1
            elif buffer:
                buffer.append(line)
                
        if buffer:
            rows.append((start_line, '\n'.join(buffer)))
            
        for line_num, row_str in rows:
            row_str = row_str.strip()
            if row_str.endswith("),") or row_str.endswith(");"):
                row_str = row_str[:-2]
            
            if row_str.startswith('('):
                row_str = row_str[1:]
                
            try:
                import csv
                reader = csv.reader([row_str], delimiter=',', quotechar="'", skipinitialspace=True, doublequote=True)
                parts = next(reader)
            except Exception as e:
                print(f"[{os.path.basename(sql_file)}:Line {line_num}] ERROR parsing row: {e}")
                total_errors += 1
                continue
                
            if len(parts) >= 12:
                q_id = parts[0]
                cloze = parts[2]
                translation = parts[3]
                
                if cloze.count('___') != 1:
                    print(f"[{os.path.basename(sql_file)}:Line {line_num}] ERROR (ID: {q_id}): '___' must appear exactly once in contextual_cloze (found {cloze.count('___')})")
                    print(f"  Cloze: {cloze}")
                    total_errors += 1
                    
                if '___' in translation:
                    print(f"[{os.path.basename(sql_file)}:Line {line_num}] ERROR (ID: {q_id}): '___' found in chinese_translation")
                    print(f"  Translation: {translation}")
                    total_errors += 1
                    
                if 'ightarrow' in row_str or 'rightarrow' in row_str:
                    print(f"[{os.path.basename(sql_file)}:Line {line_num}] ERROR (ID: {q_id}): 'rightarrow' or 'ightarrow' artifact found in row")
                    total_errors += 1
            else:
                print(f"[{os.path.basename(sql_file)}:Line {line_num}] ERROR: Not enough columns (found {len(parts)}).")
                total_errors += 1
                
    print(f"\nValidation complete. Total errors found across {len(sql_files)} files: {total_errors}")

if __name__ == '__main__':
    run_tests()
