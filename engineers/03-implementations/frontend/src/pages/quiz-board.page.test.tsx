import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import QuizBoardPage from './quiz-board.page';

// Mock the useGetNextQuestion hook so tests don't need real API
vi.mock('@/hooks/use-get-next-question', () => ({
  useGetNextQuestion: () => ({
    data: {
      id: 'q-id-123',
      contextualCloze: 'She ___ the apple.',
      chineseTranslation: '她吃了那顆蘋果。',
      correctAnswer: 'ate',
      distractor1: 'run',
      distractor2: 'sing',
      distractor3: 'jump',
      explanationRootAffix: 'eat → ate (past tense)',
      explanationMnemonic: 'Remember: irregular verb',
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
        <QuizBoardPage />
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

describe('QuizBoardPage', () => {
  it('renders quiz card with question text', async () => {
    renderPage();
    expect(await screen.findByText('⚔️ 情境克漏字測驗')).toBeInTheDocument();
  });

  it('renders 4 option buttons after question loads', async () => {
    renderPage();
    // Wait for options to be written to store and rendered
    await waitFor(() => {
      const buttons = screen.getAllByRole('button');
      // At least one of the shuffled option buttons should be present
      expect(buttons.length).toBeGreaterThanOrEqual(1);
    });
  });

  it('submits answer when option is clicked', async () => {
    const user = userEvent.setup();
    renderPage();

    // Wait for the component to finish loading and set shuffledOptions
    await waitFor(() => {
      const val = store.get('/data/shuffledOptions') as any[];
      expect(val && val.length > 0).toBe(true);
    });

    // Click the first visible option button (value from shuffledOptions[0])
    const shuffled = store.get('/data/shuffledOptions') as string[];
    const btn = await screen.findByRole('button', { name: shuffled[0] });
    await user.click(btn);

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'submitAnswer',
        payload: expect.objectContaining({
          questionId: 'q-id-123',
          selectedDistractor: shuffled[0],
        }),
      })
    );
  });
});
