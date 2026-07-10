import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import AdminDashboardPage from './admin-dashboard.page';

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
          <AdminDashboardPage />
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

describe('AdminDashboardPage', () => {
  it('renders page correctly', async () => {
    renderPage();
    expect(await screen.findByRole('button', { name: /Add Question/i })).toBeInTheDocument();
  });

  it('renders empty table when no data', async () => {
    renderPage();
    const noDataCells = await screen.findAllByText('(沒有資料)');
    expect(noDataCells.length).toBeGreaterThan(0);
  });

  it('renders rows when data exists', async () => {
    store.set('/data/listQuizQuestions', [{ id: '1', contextualCloze: 'The apple is ___.', difficultyLevel: '1' }]);
    renderPage();
    expect(await screen.findByText('The apple is ___.')).toBeInTheDocument();
  });

  it('opens create question modal and calls executeBehavior on save', async () => {
    store.set('/modals/create-question-modal', true);
    const user = userEvent.setup();
    renderPage();

    await screen.findByRole('button', { name: /Save/i });
    const textboxes = await screen.findAllByRole('textbox');
    // The last two textboxes in the DOM will be the ones in the modal
    await user.type(textboxes[textboxes.length - 2], 'uuid-1234');
    await user.type(textboxes[textboxes.length - 1], 'Testing ___');
    
    // submit
    await user.click(await screen.findByRole('button', { name: /Save/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'createQuizQuestion',
        payload: expect.objectContaining({
          wordBankId: 'uuid-1234',
          contextualCloze: 'Testing ___'
        })
      })
    );
  });

  it('calls deleteQuizQuestion', async () => {
    store.set('/modals/confirm-delete-question-modal', true);
    store.set('/data/activeQuizQuestionId', 'test-q-id');
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /Confirm/i }));

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'deleteQuizQuestion',
        payload: {
          id: 'test-q-id'
        }
      })
    );
  });
});
