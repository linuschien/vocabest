import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import { vi, describe, it, expect, beforeEach, beforeAll, afterAll, afterEach } from 'vitest';
import { setupServer } from 'msw/node';
import { graphql, HttpResponse } from 'msw';
import LearningStatisticsPage from './learning-statistics.page';

const mockProgresses = [
  {
    id: '1', userId: 'u1', date: '2026-07-15',
    targetQuestions: 10, answeredQuestions: 12, correctQuestions: 10, wrongQuestions: 2
  }
];

const server = setupServer(
  graphql.query('listDailyProgresses', () => {
    return HttpResponse.json({
      data: { listDailyProgresses: mockProgresses }
    });
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

const store = createStateStore({ modals: {}, form: {}, data: {}, state: {} });
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
          <LearningStatisticsPage />
        </JSONUIProvider>
      </QueryClientProvider>
    </MemoryRouter>
  );
}

beforeEach(() => {
  store.set('/modals', {});
  store.set('/form', {});
  store.set('/data', {
    user: { id: 'u1', email: 'dev@test.com' }
  });
  store.set('/state', {
    stats: { viewMode: 'week', currentDate: '2026-07-15T12:00:00Z' }
  });
  vi.clearAllMocks();
});

describe('LearningStatisticsPage', () => {
  it('renders successfully without crashing when data arrives', async () => {
    renderPage();
    await waitFor(() => {
      const card = document.getElementById('total-answered-card');
      expect(card).not.toBeNull();
      expect(card?.textContent).toContain('12');
    }, { timeout: 3000 });
  });
});
