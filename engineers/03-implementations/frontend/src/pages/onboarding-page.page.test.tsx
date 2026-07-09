import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import OnboardingPagePage from './onboarding-page.page';

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
        <OnboardingPagePage />
      </JSONUIProvider>
    </QueryClientProvider>
  );
}

beforeEach(() => {
  store.set('/modals', {});
  store.set('/form', {});
  store.set('/data', {});
  vi.clearAllMocks();
});

describe('OnboardingPagePage', () => {
  it('renders page heading', async () => {
    renderPage();
    expect(await screen.findByRole('button', { name: /Start Learning/i })).toBeInTheDocument();
  });

  it('selects values and calls executeBehavior on submit', async () => {
    const user = userEvent.setup();
    renderPage();

    // submit
    await user.click(await screen.findByRole('button', { name: /Start Learning/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'onboardUser',
        payload: expect.objectContaining({})
      })
    );
  });
});
