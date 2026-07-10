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
      // If user is valid but id is null, they haven't been onboarded yet
      if (user.id === null && location.pathname !== '/onboarding') {
        navigate('/onboarding');
      } 
      // If user is onboarded but trying to access onboarding, redirect them out
      else if (user.id !== null && location.pathname === '/onboarding') {
        navigate('/learning-dashboard');
      }
    }
  }, [user, isLoading, isError, navigate, location.pathname]);

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

  // Render nothing while redirecting
  if (user?.id === null && location.pathname !== '/onboarding') return null;
  if (user?.id !== null && location.pathname === '/onboarding') return null;

  return <>{children}</>;
}
