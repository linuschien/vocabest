import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import LearningDashboardPage from './learning-dashboard.page';

const store = createStateStore({ modals: {}, form: {}, data: {} });
const executeBehavior = vi.fn();
const openModal = vi.fn((p: any) => { if (p?.id) store.set(`/modals/${p.id}`, true); });
const navigate = vi.fn();
const testHandlers = { navigate, openModal, executeBehavior };

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <MemoryRouter>
      <QueryClientProvider client={qc}>
        <JSONUIProvider registry={componentRegistry} store={store} handlers={testHandlers as any}>
          <LearningDashboardPage />
        </JSONUIProvider>
      </QueryClientProvider>
    </MemoryRouter>
  );
}

beforeEach(() => {
  store.set('/modals', {});
  store.set('/form', {});
  store.set('/data', {
    user: { email: 'dev@test.com' },
    isAdmin: true
  });
  vi.clearAllMocks();
});

describe('LearningDashboardPage', () => {
  it('renders page heading', async () => {
    renderPage();
    expect(await screen.findByRole('button', { name: /開始今日任務/i })).toBeInTheDocument();
  });

  it('navigates to quiz board', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /開始今日任務/i }));
    expect(navigate).toHaveBeenCalledWith(expect.objectContaining({ path: '/quiz-board' }));
  });

  it('navigates to error review board', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /弱點特訓/i }));
    expect(navigate).toHaveBeenCalledWith(expect.objectContaining({ path: '/error-review-board' }));
  });

  it('navigates to vocabulary dictionary', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /單字卷軸/i }));
    expect(navigate).toHaveBeenCalledWith(expect.objectContaining({ path: '/vocabulary-dictionary' }));
  });

});
