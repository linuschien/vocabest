import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AuthGuard from '@/components/auth/AuthGuard';

import LearningDashboardPage from '@/pages/learning-dashboard.page';
import AdminDashboardPage from '@/pages/admin-dashboard.page';
import QuizBoardPage from '@/pages/quiz-board.page';
import ErrorReviewBoardPage from '@/pages/error-review-board.page';
import VocabularyDictionaryPage from '@/pages/vocabulary-dictionary.page';
import OnboardingPagePage from '@/pages/onboarding-page.page';

function App() {
  return (
    <BrowserRouter>
      <AuthGuard>
        <Routes>
          <Route path="/" element={<Navigate to="/learning-dashboard" replace />} />
          <Route path="/onboarding" element={<OnboardingPagePage />} />
          <Route path="/learning-dashboard" element={<LearningDashboardPage />} />
          <Route path="/admin-dashboard" element={<AdminDashboardPage />} />
          <Route path="/quiz-board" element={<QuizBoardPage />} />
          <Route path="/error-review-board" element={<ErrorReviewBoardPage />} />
          <Route path="/vocabulary-dictionary" element={<VocabularyDictionaryPage />} />
        </Routes>
      </AuthGuard>
    </BrowserRouter>
  );
}

export default App;
