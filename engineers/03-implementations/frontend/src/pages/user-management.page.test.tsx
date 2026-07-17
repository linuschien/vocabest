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
    expect(await screen.findByText('Users')).toBeInTheDocument();
  });

  it('renders empty table when no data', async () => {
    renderPage();
    const noDataCells = await screen.findAllByText('(沒有資料)');
    expect(noDataCells.length).toBeGreaterThan(0);
  });

  it('renders rows when data exists', async () => {
    store.set('/data/listUsers', [{ id: '1', email: 'test@example.com', role: 'USER', learningStreak: 5 }]);
    renderPage();
    expect(await screen.findByText('test@example.com')).toBeInTheDocument();
  });

  it('opens edit user modal and calls executeBehavior on save', async () => {
    store.set('/modals/edit-user-modal', true);
    store.set('/data/activeUserId', 'user-123');
    const user = userEvent.setup();
    renderPage();

    const saveBtn = await screen.findByRole('button', { name: /Save/i });
    await user.click(saveBtn);

    expect(executeBehavior).toHaveBeenCalledWith(
      expect.objectContaining({
        ref: 'updateUserRole'
      })
    );
  });
});
