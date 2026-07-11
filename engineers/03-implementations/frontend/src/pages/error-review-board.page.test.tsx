import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ErrorReviewBoardPage from './error-review-board.page';

// Mock hook so tests don't need real API
vi.mock('@/hooks/use-get-next-error-question', () => ({
  useGetNextErrorQuestion: () => ({
    data: {
      id: 'err-q-id-123',
      contextualCloze: 'She ___ the book.',
      chineseTranslation: '她閱讀了那本書。',
      correctAnswer: 'read',
      distractor1: 'run',
      distractor2: 'sing',
      distractor3: 'jump',
      explanationRootAffix: 'read (past tense)',
      explanationMnemonic: 'Irregular verb',
    },
    isLoading: false,
    refetch: vi.fn(),
  }),
}));

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
  store.set('/data', { user: { id: 'user-1' } });
  vi.clearAllMocks();
});

describe('ErrorReviewBoardPage', () => {
  it('renders page heading', async () => {
    renderPage();
    expect(await screen.findByText('🛡️ 弱點特訓')).toBeInTheDocument();
  });

  it('submits answer when option is clicked', async () => {
    const user = userEvent.setup();
    renderPage();

    // Wait for shuffledOptions to be set in store
    await waitFor(() => {
      const val = store.get('/data/shuffledOptions') as any[];
      expect(val && val.length > 0).toBe(true);
    });

    const shuffled = store.get('/data/shuffledOptions') as string[];
    const btn = await screen.findByRole('button', { name: shuffled[0] });
    await user.click(btn);

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'submitAnswer',
        payload: expect.objectContaining({
          questionId: 'err-q-id-123',
          selectedDistractor: shuffled[0],
        }),
      })
    );
  });
});
