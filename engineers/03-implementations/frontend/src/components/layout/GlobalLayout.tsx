import React from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import UserMenuDropdown from '@/json-render/components/UserMenuDropdown';
import PersonalSettingsModal from './PersonalSettingsModal';
import { useStateStore } from '@json-render/react';

export default function GlobalLayout() {
  const store = useStateStore();
  const navigate = useNavigate();

  const handleMenuEvent = (action: string) => {
    if (action === 'settings') {
      const user = store.get('/data/user') as any;
      if (user) {
        const reverseLevelMap: Record<string, string> = {
          "JUNIOR_HIGH": "國中2000單字",
          "SENIOR_HIGH": "高中7000單字"
        };
        const targetLevelLabel = reverseLevelMap[user.targetLevel] || user.targetLevel;
        store.set('/form/target-level-select', targetLevelLabel);
        store.set('/form/daily-target-questions-select', String(user.dailyTargetQuestions));
      }
      store.set('/modals/onboarding-modal', true);
    } else if (action === 'admin') {
      navigate('/admin-dashboard');
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      <header className="w-full bg-card shadow-sm border-b border-border px-8 py-4 flex justify-between items-center z-10 sticky top-0">
        <div 
          className="flex items-center cursor-pointer select-none group"
          onClick={() => navigate('/learning-dashboard')}
        >
          <svg 
            width="32" 
            height="32" 
            viewBox="0 0 32 32" 
            fill="none" 
            xmlns="http://www.w3.org/2000/svg"
            className="mr-3 transition-transform group-hover:scale-105 shadow-sm rounded-lg"
          >
            <rect width="32" height="32" rx="8" fill="url(#vocabest-gradient)" />
            <path 
              d="M10 10L16 22L22 10" 
              stroke="white" 
              strokeWidth="3.5" 
              strokeLinecap="round" 
              strokeLinejoin="round" 
            />
            <defs>
              <linearGradient id="vocabest-gradient" x1="0" y1="0" x2="32" y2="32" gradientUnits="userSpaceOnUse">
                <stop stopColor="#8B5CF6" />
                <stop offset="1" stopColor="#3B82F6" />
              </linearGradient>
            </defs>
          </svg>
          <h1 className="text-xl font-bold tracking-tight text-primary">
            Vocabest
          </h1>
        </div>
        <UserMenuDropdown emit={handleMenuEvent} />
      </header>
      
      <main className="flex-1 w-full relative">
        <Outlet />
      </main>

      <PersonalSettingsModal />
    </div>
  );
}
