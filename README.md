# Vocabest (智慧英文詞彙學習系統)

## 專案概述 (Project Overview)
**Vocabest** 的命名源自於 **"Vocab" (Vocabulary, 詞彙)** 與 **"Best" (最佳)** 的完美結合，期許能為學習者帶來最頂尖的英文單字學習體驗。

Vocabest 是一套專為中學生設計的「智慧英文詞彙學習系統」。透過內建的官方大考詞彙表（國中會考 2000 字、高中學測 7000 字），結合「情境克漏字測驗」、「即時深度解析」與「個人化錯題追蹤演算法」，幫助學習者以最有效率的方式建立長期記憶。

本系統摒棄傳統死背單字本的模式，採用「無限制刷題」與「沉浸式上下文推敲」的測驗循環，並自動為答錯的題目計算錯誤權重 (Error Weight) 與安排複習計畫。

---

## 核心特色 (Core Features)

### 👨‍🎓 學習者端 (Learner)
- **動態難度分級**：支援國中會考與高中學測級別，依據自身程度一鍵啟動學習。
- **無限制情境測驗**：拋棄選擇題的中英對照，採用「情境克漏字 (Contextual Cloze)」與相似誘答項，真正考驗上下文理解能力。
- **即時深度回饋**：答錯時立即彈出預先備妥的字根字首拆解與聯想記憶法解析。
- **錯題特訓模式**：精準追蹤每一個曾答錯的單字，根據錯誤次數自動排入每日待複習清單。
- **單字總覽與檢索**：完整的線上字典功能，支援 A-Z 與難度過濾、多重詞性顯示與 Web Speech API 發音朗讀。

### 🛠️ 管理員端 (Admin & Data Engineering)
- **自動化資料管線 (Data Pipeline)**：透過離線腳本 (Offline Scripts) 將官方 PDF 詞表正規化並萃取為系統 Seed Data。
- **AI 批次題庫生成**：利用 AI 腳本針對單字庫批次生成包含詞類變化、誘答項與詳解的高品質靜態題目。
- **全站題庫管理**：管理員可透過後台介面進行題庫檢視、單字模糊檢索與特定單字的 Drill-down 管理。

---

## 文件導覽 (Documentation Navigation)

系統的完整需求與規格皆收錄於 `docs/` 目錄下：

### 1. 需求與規格 (Requirements)
作為系統開發的「單一事實來源 (Single Source of Truth)」：
- 📖 **[領域術語表 (Domain Glossary)](./docs/01-requirements/glossary.md)**：系統所有核心物件與功能術語的權威定義。
- 📝 **[使用者故事總覽 (User Stories Index)](./docs/01-requirements/user-stories/README.md)**：包含 Epic 0 到 Epic 5 的完整敏捷需求清單與驗收條件。
  - `Epic 0`：系統冷啟動與資料管線 (Onboarding & Data Pipeline)
  - `Epic 1`：學習儀表板與首頁 (Learning Dashboard)
  - `Epic 2`：核心測驗循環 (Core Quiz Loop)
  - `Epic 3`：即時解析回饋 (Instant Feedback)
  - `Epic 4`：個人化錯題追蹤 (Personalized Error Tracking)
  - `Epic 5`：權限控制與後台管理 (Access Control & Admin Management)
- 🗃️ **[產品參考文件 (PRD)](./docs/01-requirements/PRD/)**：存放教育部官方釋出的詞彙表來源 PDF 與轉換後的 Markdown 文件。

### 2. 系統設計 (Design Specs)
- 🗄️ **[資料庫結構 (DBML)](./docs/02-design-specs/db-schemas/schema.dbml)**：系統關聯式資料庫架構定義。

---

## 系統架構概念 (Architecture Concept)

- **前端 (Frontend)**：React (Web Application)
- **後端 (Backend)**：Spring Boot / Java
- **資料庫 (Database)**：Relational Database (具備自動化 Seeding 機制)
- **非同步資料流**：離線 Data Engineering 腳本負責 AI 題庫生成與官方詞表解析，保持 Web 伺服器輕量高效。
