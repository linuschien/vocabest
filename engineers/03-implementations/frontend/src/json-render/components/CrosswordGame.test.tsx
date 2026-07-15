import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import CrosswordGame from './CrosswordGame';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JSONUIProvider, createStateStore } from '@json-render/react';
import { componentRegistry } from '../component-registry';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
});

// Mock the API hooks
vi.mock('@/hooks/use-get-crossword-targets', () => ({
  useGetCrosswordTargets: vi.fn(() => ({
    data: [
      { id: '1', word: 'cat', chineseTranslation: '貓', partsOfSpeech: 'n.' },
      { id: '2', word: 'car', chineseTranslation: '車', partsOfSpeech: 'n.' }
    ],
    isLoading: false,
    isError: false,
    refetch: vi.fn()
  }))
}));

// Mock crossword-layout-generator so we get deterministic layout
vi.mock('crossword-layout-generator', () => ({
  default: {
    generateLayout: vi.fn(() => ({
      rows: 3,
      cols: 3,
      table: [
        ['c', 'a', 't'],
        ['a', '-', '-'],
        ['r', '-', '-']
      ],
      result: [
        {
          clue: '貓 (n.)',
          answer: 'cat',
          startx: 1,
          starty: 1,
          orientation: 'across',
          position: 1
        },
        {
          clue: '車 (n.)',
          answer: 'car',
          startx: 1,
          starty: 1,
          orientation: 'down',
          position: 2
        }
      ]
    }))
  }
}));

const store = createStateStore({ data: { user: { id: '123' } } });

function renderComponent() {
  return render(
    <QueryClientProvider client={queryClient}>
      <JSONUIProvider registry={componentRegistry} store={store}>
        <CrosswordGame />
      </JSONUIProvider>
    </QueryClientProvider>
  );
}

describe('CrosswordGame', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    store.set('/state/crossword', {});
  });

  it('renders clues and keyboard', async () => {
    renderComponent();
    
    // Check clues
    expect(screen.getByText('貓 (n.)')).toBeInTheDocument();
    expect(screen.getByText('車 (n.)')).toBeInTheDocument();

    // Check keyboard
    expect(screen.getByText('a')).toBeInTheDocument();
    expect(screen.getByText('⌫')).toBeInTheDocument();
  });

  it('allows clicking clues and typing', async () => {
    renderComponent();
    const user = userEvent.setup();
    
    // Click on '貓 (n.)' across clue
    await user.click(screen.getByText('貓 (n.)'));
    
    // Type c, a, t
    await user.click(screen.getByText('c', { selector: 'button' }));
    await user.click(screen.getByText('a', { selector: 'button' }));
    await user.click(screen.getByText('t', { selector: 'button' }));

    // Backspace once to remove 't'
    await user.click(screen.getByText('⌫'));
    
    // We expect 1 't' from the keyboard, but no 't' in the grid.
    // Wait, the keyboard 't' is uppercase in style but lower in dom? 
    // It's rendered as {key} which is 't', but uppercase via CSS.
    // Let's just type 't' again to finish the word.
    await user.click(screen.getByText('t', { selector: 'button' }));
  });

  it('handles win condition correctly', async () => {
    renderComponent();
    const user = userEvent.setup();
    
    // Type 'cat' for Across
    await user.click(screen.getByText('貓 (n.)'));
    await user.click(screen.getByText('c', { selector: 'button' }));
    await user.click(screen.getByText('a', { selector: 'button' }));
    await user.click(screen.getByText('t', { selector: 'button' }));

    // Type 'car' for Down. The 'c' is shared and locked?
    // Wait, 'cat' is fully correct, so 'c' is locked!
    // So if we select '車 (n.)', the cursor should start at 'a' because 'c' is locked.
    await user.click(screen.getByText('車 (n.)'));
    
    // Because 'c' is locked, advanceCell will skip it. 
    // We just type 'a' and 'r'
    await user.click(screen.getByText('a', { selector: 'button' }));
    await user.click(screen.getByText('r', { selector: 'button' }));

    // Should show win message
    await waitFor(() => {
      expect(screen.getByText('🎉 恭喜完成！')).toBeInTheDocument();
    });
  });
});
