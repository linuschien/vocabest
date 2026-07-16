# Vocabest 需求文件清單 (Requirements Index)

本目錄包含 Vocabest 系統的所有 User Stories，作為 MVP 開發的唯一規格來源 (Source of Truth)。

## 系統角色 (System Actors)
- **使用者 (Learner)**：進行單字學習與測驗的主要使用者。
- **系統 (System)**：負責派送題目、驗證答案、計算間隔重複演算法與追蹤進度的後台機制。
- **系統管理員 (Admin)**：負責維護字庫、題庫並能檢視全站使用者數據的角色。

## Epic 與模組清單 (Epic & Module List)
- **[Epic 0: 系統冷啟動與資料管線 (Onboarding & Data Pipeline)](./US-00-onboarding-and-data-pipeline.md)**
  - US 0.1 首次登入與初始設定 (First Login & Onboarding)
  - US 0.2 官方詞彙表匯入與正規化
  - US 0.3 AI 批次生成靜態題庫
- **[Epic 1: 學習儀表板與首頁 (Learning Dashboard)](./US-01-learning-dashboard.md)**
  - US 1.1 一鍵啟動測驗
  - US 1.2 進度與連勝追蹤 (微型成就感)
  - US 1.3 錯題複習專區入口
  - US 1.4 單字總覽與檢索 (Vocabulary Dictionary)
- **[Epic 2: 核心測驗循環 (Core Quiz Loop)](./US-02-core-quiz-loop.md)**
  - US 2.1 情境克漏字渲染
  - US 2.2 無限制刷題機制
- **[Epic 3: 即時解析回饋 (Instant Feedback)](./US-03-instant-feedback.md)**
  - US 3.1 答錯時的深度解析與記憶法
  - US 3.2 答對時的正向增強
- **[Epic 4: 個人化錯題追蹤 (Personalized Error Tracking)](./US-04-personalized-error-tracking.md)**
  - US 4.1 錯題自動收錄與演算法排程
- **[Epic 5: 權限控制與後台管理 (Access Control & Admin Management)](./US-05-access-control-and-admin.md)**
  - US 5.1 一般使用者個人資料存取限制
  - US 5.2 系統管理員全站資料存取權限
- **[Epic 6: 支線任務與遊戲化 (Gamified Side Quests)](./US-06-side-quests.md)**
  - US 6.1 Wordle 拼字猜謎
  - US 6.2 Crossword 填字遊戲
