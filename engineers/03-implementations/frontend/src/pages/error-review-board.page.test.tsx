import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ErrorReviewBoardPage from './error-review-board.page';

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
        <ErrorReviewBoardPage />
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

describe('ErrorReviewBoardPage', () => {
  it('renders page heading', async () => {
    renderPage();
    expect(await screen.findByRole('button', { name: /Submit Answer/i })).toBeInTheDocument();
  });

  it('submits answer', async () => {
    const user = userEvent.setup();
    renderPage();

    store.set('/data/activeQuestionId', 'err-q-id-123');

    await user.type(await screen.findByPlaceholderText(/Type your answer here/i), 'orange');
    await user.click(await screen.findByRole('button', { name: /Submit Answer/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'submitAnswer',
        payload: {
          questionId: 'err-q-id-123',
          answerWord: 'orange'
        }
      })
    );
  });
});
