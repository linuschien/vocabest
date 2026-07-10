import { render, screen, waitFor } from '@testing-library/react';
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
    <QueryClientProvider client={qc}>
      <JSONUIProvider registry={componentRegistry} store={store} handlers={testHandlers as any}>
        <LearningDashboardPage />
      </JSONUIProvider>
    </QueryClientProvider>
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
    expect(await screen.findByRole('button', { name: /Start Today's Task/i })).toBeInTheDocument();
  });

  it('navigates to quiz board', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /Start Today's Task/i }));
    expect(navigate).toHaveBeenCalledWith(expect.objectContaining({ path: '/quiz-board' }));
  });

  it('navigates to error review board', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /Review Errors/i }));
    expect(navigate).toHaveBeenCalledWith(expect.objectContaining({ path: '/error-review-board' }));
  });

  it('navigates to vocabulary dictionary', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /Vocabulary Dictionary/i }));
    expect(navigate).toHaveBeenCalledWith(expect.objectContaining({ path: '/vocabulary-dictionary' }));
  });

  it('opens user menu and navigates to admin dashboard', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /dev@test.com/i }));
    expect(openModal).toHaveBeenCalledWith(expect.objectContaining({ id: 'avatar-dropdown-modal' }));

    await user.click(await screen.findByRole('button', { name: /Admin Dashboard/i }));
    expect(navigate).toHaveBeenCalledWith(expect.objectContaining({ path: '/admin-dashboard' }));
  });

  it('submits onboarding form', async () => {
    store.set('/modals/onboarding-modal', true);
    store.set('/form/target-level-select', '高中7000單字');
    store.set('/form/daily-target-questions-select', '20');
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /Start Learning/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'onboardUser',
        payload: {
          targetLevel: '高中7000單字',
          dailyTargetQuestions: '20'
        }
      })
    );
  });
});
