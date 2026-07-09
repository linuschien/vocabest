import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';

import './index.css';
import App from './App.tsx';

// Initialize React Query
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

// Initialize JSON UI Store
const store = createStateStore({
  modals: {},
  data: {},
  form: {},
});

// Application navigation and behavior handlers for JSON UI triggers
const appHandlers = {
  navigate: (path: string) => {
    // Basic fallback navigation. For SPA routing, this could be refactored 
    // into a wrapper inside BrowserRouter to use useNavigate()
    window.location.href = path; 
  },
  openModal: ({ id }: { id: string }) => {
    if (id) store.set(`/modals/${id}`, true);
  },
  executeBehavior: (params: any) => {
    console.log('Execute behavior triggered from UI Manifest:', params);
  }
};

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <JSONUIProvider registry={componentRegistry} store={store} handlers={appHandlers as any}>
        <App />
      </JSONUIProvider>
    </QueryClientProvider>
  </StrictMode>
);
