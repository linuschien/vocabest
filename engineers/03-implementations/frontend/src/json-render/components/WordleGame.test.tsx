import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import WordleGame from './WordleGame';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '../component-registry';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
});

// Mock the API hooks
vi.mock('@/hooks/use-get-wordle-target', () => ({
  useGetWordleTarget: vi.fn(() => ({
    data: { word: 'apple', translation: '蘋果' },
    isLoading: false,
    isError: false,
    refetch: vi.fn()
  }))
}));

vi.mock('@/hooks/use-validate-wordle-guess', () => ({
  useValidateWordleGuess: vi.fn(() => ({
    mutateAsync: vi.fn().mockResolvedValue({ valid: true }),
    isPending: false
  }))
}));

const store = createStateStore({ data: { user: { id: '123' } } });

function renderComponent() {
  return render(
    <QueryClientProvider client={queryClient}>
      <JSONUIProvider registry={componentRegistry} store={store}>
        <WordleGame />
      </JSONUIProvider>
    </QueryClientProvider>
  );
}

describe('WordleGame', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the game board and keyboard', async () => {
    renderComponent();
    expect(screen.getByText('q')).toBeInTheDocument();
    expect(screen.getByText('Enter')).toBeInTheDocument();
  });

  it('allows typing and backspacing', async () => {
    renderComponent();
    const user = userEvent.setup();
    
    // Type H E L L O
    await user.click(screen.getByText('h'));
    await user.click(screen.getByText('e'));
    await user.click(screen.getByText('l', { selector: 'button' }));
    await user.click(screen.getByText('l', { selector: 'button' }));
    await user.click(screen.getByText('o'));

    // Check if they are rendered
    const hElements = screen.queryAllByText('h', { exact: false });
    expect(hElements.length).toBeGreaterThan(0);
    
    // Backspace
    await user.click(screen.getByText('⌫'));
    
    // Ensure 'o' is gone from the grid (only the keyboard key remains)
    const oKeys = screen.queryAllByText('o', { exact: true });
    expect(oKeys.length).toBe(1); // Only keyboard key
  });

  it('handles a winning guess correctly', async () => {
    renderComponent();
    const user = userEvent.setup();
    
    // Type A P P L E
    await user.click(screen.getByText('a'));
    await user.click(screen.getByText('p', { selector: 'button' }));
    await user.click(screen.getByText('p', { selector: 'button' }));
    await user.click(screen.getByText('l', { selector: 'button' }));
    await user.click(screen.getByText('e'));
    
    // Enter
    await user.click(screen.getByText('Enter'));

    await waitFor(() => {
      expect(screen.getByText('🎉 挑戰成功！')).toBeInTheDocument();
    });
  });
});
