import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useWhoami } from '@/hooks/use-whoami';
import { store } from '@/store';

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const { data: user, isLoading, isError } = useWhoami();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (!isLoading && !isError && user) {
      store.set('/data/user', user);
      if (user.id === null) {
        store.set('/modals/onboarding-modal', true);
      } else {
        store.set('/modals/onboarding-modal', false);
      }
    }
  }, [user, isLoading, isError]);

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="animate-spin h-8 w-8 border-4 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex h-screen items-center justify-center text-red-500">
        Authentication Error. Ensure you are logged in via Google IAP.
      </div>
    );
  }

  return <>{children}</>;
}
