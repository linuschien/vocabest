import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { StateProvider } from '@json-render/react';
import CriticalMissionLogViewer from './CriticalMissionLogViewer';

// Mock hook
const mockErrorEvents = [
  {
    id: 'evt-1',
    userId: 'user-123',
    selectedDistractor: 'banana',
    quizQuestion: {
      id: 'q-1',
      wordBankId: 'wb-1',
      contextualCloze: 'An ___ a day keeps the doctor away.',
      chineseTranslation: '一天一蘋果，醫生遠離我。',
      correctAnswer: 'apple',
      explanationRootAffix: 'ap- (to) + ple',
      explanationMnemonic: 'Apple is red',
      wordBank: {
        id: 'wb-1',
        word: 'apple',
        partsOfSpeech: 'noun',
        chineseTranslation: '蘋果'
      }
    }
  },
  {
    id: 'evt-2',
    userId: 'user-123',
    selectedDistractor: 'cat',
    quizQuestion: {
      id: 'q-2',
      wordBankId: 'wb-2',
      contextualCloze: 'The ___ barked loudly.',
      chineseTranslation: '那隻狗大聲吠叫。',
      correctAnswer: 'dog',
      explanationRootAffix: '',
      explanationMnemonic: '',
      wordBank: {
        id: 'wb-2',
        word: 'dog',
        partsOfSpeech: 'noun',
        chineseTranslation: '狗'
      }
    }
  }
];

vi.mock('@/hooks/use-list-error-events', () => ({
  useListErrorEvents: vi.fn(() => ({
    data: {
      content: mockErrorEvents,
      totalElements: 2
    },
    isLoading: false,
    isError: false,
    isFetching: false
  }))
}));

// Mock window.speechSynthesis
Object.defineProperty(window, 'speechSynthesis', {
  value: {
    speak: vi.fn(),
    cancel: vi.fn(),
  },
  writable: true,
});
(globalThis as any).SpeechSynthesisUtterance = vi.fn() as any;

describe('CriticalMissionLogViewer', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = () => {
    return render(
      <StateProvider initialState={{ '/data/user/id': 'user-123' }}>
        <CriticalMissionLogViewer />
      </StateProvider>
    );
  };

  it('renders first card correctly', () => {
    renderComponent();
    
    // Check total and current page text
    expect(screen.getByTestId('pagination-status')).toHaveTextContent('1/2');

    // Check specific fields requested by user
    expect(screen.getByText('An ___ a day keeps the doctor away.')).toBeInTheDocument();
    expect(screen.getAllByText('apple').length).toBeGreaterThan(0); // Word & Correct Answer
    expect(screen.getByText('banana')).toBeInTheDocument(); // Distractor
    expect(screen.getByText('noun')).toBeInTheDocument();
    expect(screen.getByText('蘋果')).toBeInTheDocument();
  });

  it('navigates to next card', () => {
    renderComponent();
    
    // Should show apple first
    expect(screen.getByText('An ___ a day keeps the doctor away.')).toBeInTheDocument();

    // Click Next
    const nextBtn = screen.getByTestId('next-card-btn');
    fireEvent.click(nextBtn!);

    // Should show dog next
    expect(screen.getByTestId('pagination-status')).toHaveTextContent('2/2');
    expect(screen.getByText('The ___ barked loudly.')).toBeInTheDocument();
    expect(screen.getAllByText('dog').length).toBeGreaterThan(0);
    expect(screen.getByText('cat')).toBeInTheDocument();
  });

  it('plays TTS when button clicked', () => {
    renderComponent();
    
    const speakBtn = screen.getByTitle('朗讀單字');
    fireEvent.click(speakBtn);

    expect(window.speechSynthesis.cancel).toHaveBeenCalled();
    expect(window.speechSynthesis.speak).toHaveBeenCalled();
  });
});
