# 單字庫儲存設計 (Vocabulary Storage Design)

## 背景
我們透過 PDF 匯入了國中基礎1200字、進階800字，以及高中7000字。由於這些檔案經過轉換後，呈現的是先「一連串英文單字」再「一連串中文解釋」的結構，因此我們需要一個穩固的儲存結構與資料管線 (ETL Pipeline) 才能將其清洗並轉入資料庫。

## 1. 資料庫結構設計 (Domain Model)

為了與系統的測驗核心脫鉤並保持資料乾淨，我們新增了 `VocabularyWord` 實體，作為靜態單字庫的來源。測驗題目 (`QuizQuestion`) 與錯題本 (`ErrorLog`) 均透過關聯來參照此單字庫。

### VocabularyWord 實體結構：
* **`id`** (UUID, PK): 單字唯一識別碼
* **`word`** (String): 單字本身（例如：`ability`）
* **`partOfSpeech`** (String): 詞性，可存放縮寫（例如：`n.`, `v.`, `adj.`）
* **`translation`** (String): 中文解釋
* **`level`** (VocabularyLevel Enum): 所屬級別，包含：
  * `JUNIOR_1200`
  * `JUNIOR_800`
  * `SENIOR_7000`

### 與其他物件的關聯：
* **QuizQuestion (測驗題目)**: `QuizQuestion` 擁有 `wordId`，多個題目（不同情境句）可以測試同一個單字。
* **ErrorLog (錯題本)**: 記錄使用者答錯的單字，也是透過 `wordId` 參照，以利統整使用者的錯誤權重。

*(已同步更新於 `VocabestCore_domain_model.puml`)*

---

## 2. 資料清洗與匯入流程 (ETL Pipeline)

因為轉換的純文字檔有版面排列導致的順序問題（英文與中文分離），我們將透過以下流程將其轉換為資料庫能接受的格式：

1. **萃取 (Extract)**：
   撰寫 Python 腳本讀取轉換後的 Markdown/Text 檔。利用關鍵字（如「單字」、「解釋」）將文字拆分為兩個陣列：`words_array` 與 `meanings_array`。
2. **轉換 (Transform)**：
   * 透過陣列索引 (Index) 將單字與解釋一對一匹配 (Zip)。
   * 利用正規表達式 (Regex) 從「解釋」欄位中提取詞性。例如 `(n)能力` -> 詞性為 `n`，解釋為 `能力`。
   * 將配對好的資料轉換為 JSON 或 CSV 格式（依據對應的 `VocabularyLevel` 加入標籤）。
3. **載入 (Load)**：
   * 將最終的 JSON/CSV 寫入關聯式資料庫 (如 PostgreSQL) 的 `VocabularyWord` 資料表中。
   * 這部分可以寫成系統的 Seed Script，確保每次部署或重置時都能擁有完整且乾淨的靜態題庫。

> [!TIP]
> 靜態單字庫匯入後不會頻繁更動，可以考慮在系統啟動時載入 Redis 快取，以滿足 US-00-02 要求的「毫秒級題庫載入體驗」。
