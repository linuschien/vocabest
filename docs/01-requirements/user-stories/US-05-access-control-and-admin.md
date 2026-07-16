# US-05 權限控制與後台管理 (Access Control & Admin Management)

## 背景 (Background)
為了確保使用者資料的隱私性與系統核心內容（字庫、題庫）的安全性，系統需實作基於角色的存取控制 (RBAC)。嚴格區分一般使用者 (Learner) 與系統管理員 (Admin) 之權限邊界。

---

## US-05-01：一般使用者個人資料存取限制

**身份**：一般使用者 (Learner)

> **As a** 一般使用者，  
> **I want to** 僅能檢視與操作我自己的資料（包含個人資料、設定、每日進度及錯題本），  
> **So that** 我的個人學習歷程具備隱私，且不會被其他使用者看見或竄改。

### 驗收條件 (Acceptance Criteria)
- **AC1**：一般使用者登入後，系統僅允許其讀取及修改關聯至自身帳號的資料 (Profile, Settings, DailyProgress, ErrorLog)。
- **AC2**：若一般使用者嘗試請求其他使用者的資料，系統須阻擋請求並回傳 HTTP `403 Forbidden`。
- **AC3**：一般使用者的介面中，不應呈現任何字庫管理、題庫管理或全站數據等管理員專屬功能之入口與操作按鈕。

---

## US-05-02：系統管理員全站資料存取權限

**身份**：系統管理員 (Admin)

> **As a** 系統管理員，  
> **I want to** 擁有字庫、題庫與所有使用者資料的存取權限，  
> **So that** 我能夠維護系統的核心學習內容並進行營運管理。

### 驗收條件 (Acceptance Criteria)
- **AC1 (字庫管理與檢索)**：系統管理員可以檢視、新增、修改、刪除字庫 (WordBank) 的所有內容。列表檢視頁面必須實作分頁 (Pagination) 機制，並支援與前台一致的檢索條件：「單字模糊查詢」、「開頭字母 (A-Z)」、「難度等級」。單字欄位需支援多重詞性設定。
- **AC2 (題庫 Drill-down)**：在字庫列表介面中，系統管理員可點擊特定單字進行「下鑽 (Drill-down)」，進入專屬於該單字的題庫檢視頁面，並在此管理關聯的測驗題目 (StaticQuestionBank)。
- **AC3 (使用者管理)**：系統管理員可以檢視全站所有使用者 (User) 的資料與學習進度，進行總體數據的追蹤。列表或明細中需呈現以下資訊：
  - **使用者基本資訊與設定**：包含帳號 (`email`)、角色 (`role`)、目標等級 (`target_level`)、每日目標題數 (`daily_target_questions`)、目前連續學習天數 (`learning_streak`)、最高連續學習天數 (`max_learning_streak`)、最高單日答題數 (`max_daily_questions`)。
  - **累計學習數據**：透過彙整該使用者的 `daily_progress` 紀錄，顯示其「總答題數 (Total Questions Answered)」、「總答對數 (Total Correct Answers)」，以及計算出的「總正確率 (Overall Accuracy)」。
- **AC4 (權限阻擋)**：一般使用者若嘗試呼叫管理員專屬 API 端點，系統須阻擋請求並回傳 HTTP `403 Forbidden`。
- **AC5 (預設管理員)**：系統預設應將 `linus.chien@gmail.com` 綁定為系統管理員 (Admin) 角色。
