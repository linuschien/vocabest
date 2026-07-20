import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { StateProvider, useStateStore } from '@json-render/react';
import MemoryGame from './MemoryGame';

const mockTargets = [
  { id: '1', word: 'apple', chineseTranslation: '蘋果', targetLevel: 'easy' },
  { id: '2', word: 'banana', chineseTranslation: '香蕉', targetLevel: 'easy' },
  { id: '3', word: 'cat', chineseTranslation: '貓', targetLevel: 'easy' },
  { id: '4', word: 'dog', chineseTranslation: '狗', targetLevel: 'easy' },
];

vi.mock('@/hooks/use-get-memory-game-targets', () => ({
  useGetMemoryGameTargets: vi.fn(() => ({
    data: mockTargets,
    isLoading: false,
    isError: false,
    refetch: vi.fn()
  }))
}));

// Mock window.speechSynthesis
Object.defineProperty(window, 'speechSynthesis', {
  value: {
    speak: vi.fn(),
  },
  writable: true,
});
(globalThis as any).SpeechSynthesisUtterance = vi.fn() as any;

describe('MemoryGame', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  const renderGame = () => {
    return render(
      <StateProvider>
        <MemoryGame />
      </StateProvider>
    );
  };

  it('renders setup screen initially', () => {
    renderGame();
    expect(screen.getByText('翻牌對對碰')).toBeInTheDocument();
    expect(screen.getByText(/easy/i)).toBeInTheDocument();
  });

  it('starts game and shows cards on difficulty selection', () => {
    renderGame();
    fireEvent.click(screen.getByText(/easy/i));
    
    // We mocked 4 pairs (8 cards total)
    expect(screen.getByText('apple')).toBeInTheDocument();
    expect(screen.getByText('蘋果')).toBeInTheDocument();
  });

  it('flips cards and checks match', async () => {
    renderGame();
    fireEvent.click(screen.getByText(/easy/i));

    const apple = screen.getByText('apple');
    const appleTrans = screen.getByText('蘋果');
    
    fireEvent.click(apple.parentElement!);
    fireEvent.click(appleTrans.parentElement!);
    
    // Fast-forward timeout for match (500ms)
    act(() => {
      vi.advanceTimersByTime(500);
    });
    
    // Verify Web Speech API called
    expect(window.speechSynthesis.speak).toHaveBeenCalled();
  });
});
