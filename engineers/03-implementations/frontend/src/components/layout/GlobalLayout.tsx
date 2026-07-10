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
      store.set('/modals/onboarding-modal', true);
    } else if (action === 'admin') {
      navigate('/admin-dashboard');
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      <header className="w-full bg-card shadow-sm border-b border-border px-8 py-4 flex justify-between items-center z-10 sticky top-0">
        <h1 
          className="text-xl font-bold text-primary cursor-pointer"
          onClick={() => navigate('/learning-dashboard')}
        >
          Vocabest
        </h1>
        <UserMenuDropdown emit={handleMenuEvent} />
      </header>
      
      <main className="flex-1 w-full relative">
        <Outlet />
      </main>

      <PersonalSettingsModal />
    </div>
  );
}
