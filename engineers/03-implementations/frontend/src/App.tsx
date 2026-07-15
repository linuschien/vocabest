import React from 'react';
import { HashRouter, Routes, Route } from 'react-router-dom';
import AuthGuard from '@/components/auth/AuthGuard';
import BehaviorProvider from '@/json-render/BehaviorProvider';

import LearningDashboardPage from '@/pages/learning-dashboard.page';
import AdminDashboardPage from '@/pages/admin-dashboard.page';
import QuizBoardPage from '@/pages/quiz-board.page';
import ErrorReviewBoardPage from '@/pages/error-review-board.page';
import VocabularyDictionaryPage from '@/pages/vocabulary-dictionary.page';
import LearningStatisticsPage from '@/pages/learning-statistics.page';
import WordlePage from '@/pages/wordle.page';
import CrosswordPage from '@/pages/crossword.page';

import GlobalLayout from '@/components/layout/GlobalLayout';

function App() {
  return (
    <HashRouter>
      <AuthGuard>
        <BehaviorProvider>
          <Routes>
            <Route element={<GlobalLayout />}>
              <Route path="/" element={<LearningDashboardPage />} />
              <Route path="/learning-dashboard" element={<LearningDashboardPage />} />
              <Route path="/admin-dashboard" element={<AdminDashboardPage />} />
              <Route path="/quiz-board" element={<QuizBoardPage />} />
              <Route path="/error-review-board" element={<ErrorReviewBoardPage />} />
              <Route path="/vocabulary-dictionary" element={<VocabularyDictionaryPage />} />
              <Route path="/learning-statistics" element={<LearningStatisticsPage />} />
              <Route path="/wordle" element={<WordlePage />} />
              <Route path="/crossword" element={<CrosswordPage />} />
            </Route>
          </Routes>
        </BehaviorProvider>
      </AuthGuard>
    </HashRouter>
  );
}

export default App;
