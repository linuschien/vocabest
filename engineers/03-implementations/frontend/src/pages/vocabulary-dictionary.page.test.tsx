import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import VocabularyDictionaryPage from './vocabulary-dictionary.page';

vi.mock('@/hooks/use-list-word-banks', () => ({
  useListWordBanks: vi.fn(() => ({ data: undefined, refetch: vi.fn() }))
}));

const store = createStateStore({ modals: {}, form: {}, data: {} });
const executeBehavior = vi.fn();
const openModal = vi.fn((p: any) => { if (p?.id) store.set(`/modals/${p.id}`, true); });
const navigate = vi.fn();
const testHandlers = { navigate, openModal, executeBehavior };

import { MemoryRouter } from 'react-router-dom';

const mockUseNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual: any = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockUseNavigate,
  };
});

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <MemoryRouter>
      <QueryClientProvider client={qc}>
        <JSONUIProvider registry={componentRegistry} store={store} handlers={testHandlers as any}>
          <VocabularyDictionaryPage />
        </JSONUIProvider>
      </QueryClientProvider>
    </MemoryRouter>
  );
}

beforeEach(() => {
  store.set('/modals', {});
  store.set('/form', {});
  store.set('/data', {});
  vi.clearAllMocks();
});

describe('VocabularyDictionaryPage', () => {
  it('renders page correctly', async () => {
    renderPage();
    expect(await screen.findByPlaceholderText(/Enter keyword.../i)).toBeInTheDocument();
  });

  it('renders table rows from store', async () => {
    store.set('/data/listWordBanks', [{ id: '1', word: 'apple', chineseTranslation: '蘋果', difficultyLevel: '1', partsOfSpeech: 'noun' }]);
    renderPage();
    expect(await screen.findByText('apple')).toBeInTheDocument();
    expect(await screen.findByText('蘋果')).toBeInTheDocument();
  });

  it('navigates to drill-down', async () => {
    store.set('/data/user', { role: 'ADMIN' });
    store.set('/data/listWordBanks', [{ id: '1', word: 'apple', chineseTranslation: '蘋果', difficultyLevel: '1', partsOfSpeech: 'noun' }]);
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /Manage Questions/i }));
    expect(mockUseNavigate).toHaveBeenCalledWith('/admin-dashboard');
  });

  it('submits create word bank form', async () => {
    store.set('/modals/create-wordbank-modal', true);
    const user = userEvent.setup();
    renderPage();

    await screen.findByRole('button', { name: /Save/i });
    const textboxes = await screen.findAllByRole('textbox');
    // The last three textboxes will be in the modal
    await user.type(textboxes[textboxes.length - 3], 'banana');
    await user.type(textboxes[textboxes.length - 2], 'noun');
    await user.type(textboxes[textboxes.length - 1], '香蕉');

    await user.click(await screen.findByRole('button', { name: /Save/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'createWordBank',
        payload: expect.objectContaining({
          word: 'banana',
          partsOfSpeech: 'noun',
          chineseTranslation: '香蕉'
        })
      })
    );
  });

  it('submits delete confirm', async () => {
    store.set('/modals/confirm-delete-modal', true);
    store.set('/data/activeWordBankId', 'wb-123');
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /Confirm/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'deleteWordBank',
        payload: { id: 'wb-123' }
      })
    );
  });

  it('triggers search when search button is clicked', async () => {
    const user = userEvent.setup();
    renderPage();
    
    await user.click(await screen.findByRole('button', { name: /Search/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'triggerSearch'
      })
    );
  });
});
