import React, { useState, useEffect, useCallback } from 'react';
import { useStateStore, useStateValue } from '@json-render/react';
import { useGetWordleTarget } from '@/hooks/use-get-wordle-target';
import { useValidateWordleGuess } from '@/hooks/use-validate-wordle-guess';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { RotateCcw } from 'lucide-react';

type LetterState = 'correct' | 'present' | 'absent' | 'empty';

export default function WordleGame({ element }: any) {
  const store = useStateStore();
  const userId = store.get('/data/user/id') as string;
  
  const { data: targetData, isLoading: isTargetLoading, isError: isTargetError, refetch: refetchTarget } = useGetWordleTarget(userId);
  const { mutateAsync: validateGuess, isPending: isValidating } = useValidateWordleGuess();

  const guesses = (useStateValue('/state/wordle/guesses') || []) as string[];
  const currentGuess = (useStateValue('/state/wordle/currentGuess') || '') as string;
  const gameStatus = (useStateValue('/state/wordle/gameStatus') || 'playing') as 'playing' | 'won' | 'lost';
  const letterStates = (useStateValue('/state/wordle/letterStates') || {}) as Record<string, LetterState>;

  const setGuesses = (val: string[]) => store.set('/state/wordle/guesses', val);
  const setCurrentGuess = (val: string | ((prev: string) => string)) => {
    store.set('/state/wordle/currentGuess', typeof val === 'function' ? val(currentGuess) : val);
  };
  const setGameStatus = (val: 'playing' | 'won' | 'lost') => store.set('/state/wordle/gameStatus', val);
  const setLetterStates = (val: Record<string, LetterState>) => store.set('/state/wordle/letterStates', val);

  const [errorMsg, setErrorMsg] = useState('');

  const MAX_GUESSES = 6;
  const WORD_LENGTH = 5;
  const targetWord = targetData?.word.toLowerCase() || '';
  const translation = targetData?.chineseTranslation || '';
  const partOfSpeech = targetData?.partsOfSpeech || '';

  // Input handling
  const onKeyPress = useCallback(async (key: string) => {
    if (gameStatus !== 'playing' || isValidating) return;
    setErrorMsg('');

    if (key === 'Enter') {
      if (currentGuess.length !== WORD_LENGTH) {
        setErrorMsg('長度不夠');
        return;
      }
      
      try {
        const response = await validateGuess({ userId, guess: currentGuess });
        if (!response.valid) {
          setErrorMsg('單字卷軸中查無此字');
          setTimeout(() => setErrorMsg(''), 2500);
          return;
        }

        // Compute match states for keyboard
        const newLetterStates = { ...letterStates };
        const targetLetters = targetWord.split('');
        const guessLetters = currentGuess.split('');
        const rowStates: LetterState[] = Array(5).fill('absent');

        // Pass 1: exact matches
        guessLetters.forEach((char, i) => {
          if (char === targetLetters[i]) {
            rowStates[i] = 'correct';
            newLetterStates[char] = 'correct';
            targetLetters[i] = '' as any; // consume
          }
        });

        // Pass 2: present matches
        guessLetters.forEach((char, i) => {
          if (rowStates[i] === 'absent') {
            const targetIdx = targetLetters.indexOf(char);
            if (targetIdx !== -1) {
              rowStates[i] = 'present';
              if (newLetterStates[char] !== 'correct') {
                newLetterStates[char] = 'present';
              }
              targetLetters[targetIdx] = '' as any; // consume
            } else {
              if (newLetterStates[char] !== 'correct' && newLetterStates[char] !== 'present') {
                newLetterStates[char] = 'absent';
              }
            }
          }
        });

        const newGuesses = [...guesses, currentGuess];
        setGuesses(newGuesses);
        setCurrentGuess('');
        setLetterStates(newLetterStates);

        if (currentGuess === targetWord) {
          setGameStatus('won');
        } else if (newGuesses.length >= MAX_GUESSES) {
          setGameStatus('lost');
        }

      } catch (e) {
        setErrorMsg('驗證單字時發生錯誤');
        setTimeout(() => setErrorMsg(''), 2500);
      }
      return;
    }

    if (key === 'Backspace') {
      setCurrentGuess(prev => prev.slice(0, -1));
      return;
    }

    if (/^[a-zA-Z]$/.test(key)) {
      if (currentGuess.length < WORD_LENGTH) {
        setCurrentGuess(prev => (prev + key).toLowerCase());
      }
    }
  }, [currentGuess, gameStatus, guesses, isValidating, targetWord, userId, validateGuess, letterStates]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.metaKey || e.altKey) return;
      if (e.key === 'Enter' || e.key === 'Backspace' || /^[a-zA-Z]$/.test(e.key)) {
        onKeyPress(e.key);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onKeyPress]);

  // Restart
  const handleRestart = () => {
    setGuesses([]);
    setCurrentGuess('');
    setGameStatus('playing');
    setLetterStates({});
    setErrorMsg('');
    refetchTarget();
  };

  if (isTargetLoading) {
    return (
      <div className="flex flex-col items-center justify-center w-full max-w-lg mx-auto p-8 text-muted-foreground" id={element?.props?.id}>
        <RotateCcw className="w-8 h-8 animate-spin mb-4" />
        載入 Wordle 題目中...
      </div>
    );
  }
  
  if (isTargetError || !targetWord) {
    return <div className="p-8 text-center text-destructive font-bold" id={element?.props?.id}>無法載入題目，請稍後再試。</div>;
  }

  const emptyRows = Array.from({ length: Math.max(0, MAX_GUESSES - 1 - guesses.length) });

  return (
    <ErrorBoundary>
      <div className="flex flex-col lg:flex-row items-center lg:items-start justify-center w-full max-w-5xl mx-auto p-4 gap-8 lg:gap-16 select-none isolate" id={element?.props?.id}>
        
        {/* Win/Lose Modal Overlay */}
        {(gameStatus === 'won' || gameStatus === 'lost') && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center bg-background/80 backdrop-blur-sm animate-in fade-in">
            <div className="flex flex-col items-center p-10 bg-card border border-border shadow-2xl rounded-2xl animate-in zoom-in-95 max-w-md w-full mx-4">
              <h2 className="text-4xl font-extrabold mb-4 text-center">
                {gameStatus === 'won' ? '🎉 挑戰成功！' : 'Game Over'}
              </h2>
              <div className="text-2xl mb-2 text-muted-foreground">答案是: <span className="font-bold text-primary tracking-widest uppercase">{targetWord}</span></div>
              <div className="text-lg mb-8 font-medium text-foreground text-center">
                {partOfSpeech ? `[${partOfSpeech}] ` : ''}{translation}
              </div>
              
              {gameStatus === 'won' && (
                <p className="text-md mb-8 text-muted-foreground">你總共猜了 {guesses.length} 次</p>
              )}
              <button 
                onClick={handleRestart}
                className="px-8 py-3 bg-primary text-primary-foreground font-bold rounded-xl shadow-lg hover:opacity-90 transition-transform active:scale-95 flex items-center gap-2 text-lg"
              >
                <RotateCcw className="w-5 h-5" />
                再玩一次
              </button>
            </div>
          </div>
        )}

        {/* Left Side: Legend + Grid */}
        <div className="flex flex-col items-center relative">
          <div className="flex items-center justify-center gap-4 mb-6 text-sm font-medium text-muted-foreground w-full flex-wrap">
            <div className="flex items-center gap-1.5"><div className="w-4 h-4 bg-green-500 rounded shadow-sm"></div>位置正確</div>
            <div className="flex items-center gap-1.5"><div className="w-4 h-4 bg-yellow-500 rounded shadow-sm"></div>字母存在</div>
            <div className="flex items-center gap-1.5"><div className="w-4 h-4 bg-slate-500 rounded shadow-sm"></div>完全沒有</div>
          </div>

          <div className="grid grid-rows-6 gap-2 relative">
            {/* Error Message Toast */}
            {errorMsg && (
              <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50 px-6 py-4 bg-foreground text-background text-lg font-bold rounded-xl shadow-2xl animate-in fade-in zoom-in-90 whitespace-nowrap">
                {errorMsg}
              </div>
            )}

            {guesses.map((guess, i) => (
              <CompletedRow key={i} guess={guess} target={targetWord} />
            ))}
            {gameStatus === 'playing' && guesses.length < MAX_GUESSES && (
              <CurrentRow guess={currentGuess} />
            )}
            {emptyRows.map((_, i) => (
              <EmptyRow key={i} />
            ))}
          </div>
        </div>

        {/* Right Side: Keyboard */}
        <div className="flex flex-col w-full max-w-md lg:mt-14">
          <Keyboard letterStates={letterStates} onKeyPress={onKeyPress} disabled={gameStatus !== 'playing'} />
        </div>

      </div>
    </ErrorBoundary>
  );
}

// Subcomponents

function CompletedRow({ guess, target }: { guess: string, target: string }) {
  const targetLetters = target.split('');
  const guessLetters = guess.split('');
  const states: LetterState[] = Array(5).fill('absent');

  guessLetters.forEach((char, i) => {
    if (char === targetLetters[i]) {
      states[i] = 'correct';
      targetLetters[i] = '' as any;
    }
  });

  guessLetters.forEach((char, i) => {
    if (states[i] === 'absent') {
      const targetIdx = targetLetters.indexOf(char);
      if (targetIdx !== -1) {
        states[i] = 'present';
        targetLetters[targetIdx] = '' as any;
      }
    }
  });

  return (
    <div className="grid grid-cols-5 gap-2">
      {guessLetters.map((letter, i) => {
        const state = states[i];
        const bgClass = state === 'correct' ? 'bg-green-500 border-green-500 text-white' : 
                        state === 'present' ? 'bg-yellow-500 border-yellow-500 text-white' : 
                        'bg-slate-500 border-slate-500 text-white dark:bg-slate-700 dark:border-slate-700';
        return (
          <div key={i} className={`w-14 h-14 border-2 flex items-center justify-center text-3xl font-extrabold uppercase rounded-md shadow-sm transition-colors ${bgClass}`}>
            {letter}
          </div>
        );
      })}
    </div>
  );
}

function CurrentRow({ guess }: { guess: string }) {
  const letters = guess.split('');
  const emptyBoxes = Array.from({ length: 5 - letters.length });
  
  return (
    <div className="grid grid-cols-5 gap-2">
      {letters.map((letter, i) => (
        <div key={i} className="w-14 h-14 border-2 border-primary flex items-center justify-center text-3xl font-extrabold uppercase rounded-md shadow-sm text-foreground animate-in pop">
          {letter}
        </div>
      ))}
      {emptyBoxes.map((_, i) => (
        <div key={i + letters.length} className="w-14 h-14 border-2 border-border/60 flex items-center justify-center text-3xl font-extrabold uppercase rounded-md">
        </div>
      ))}
    </div>
  );
}

function EmptyRow() {
  return (
    <div className="grid grid-cols-5 gap-2">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={i} className="w-14 h-14 border-2 border-border/60 flex items-center justify-center text-3xl font-extrabold uppercase rounded-md">
        </div>
      ))}
    </div>
  );
}

function Keyboard({ letterStates, onKeyPress, disabled }: { letterStates: Record<string, LetterState>, onKeyPress: (key: string) => void, disabled: boolean }) {
  const keys = [
    ['q','w','e','r','t','y','u','i','o','p'],
    ['a','s','d','f','g','h','j','k','l'],
    ['z','x','c','v','b','n','m', 'Backspace', 'Enter']
  ];

  const getKeyClass = (key: string) => {
    const state = letterStates[key];
    if (state === 'correct') return 'bg-green-500 text-white border-green-600 hover:bg-green-600';
    if (state === 'present') return 'bg-yellow-500 text-white border-yellow-600 hover:bg-yellow-600';
    if (state === 'absent') return 'bg-slate-500 text-white border-slate-600 hover:bg-slate-600 dark:bg-slate-700 dark:border-slate-800 dark:hover:bg-slate-800';
    return 'bg-muted border-border hover:bg-accent text-foreground';
  };

  return (
    <div className={`flex flex-col gap-2 w-full mt-4 ${disabled ? 'opacity-50 pointer-events-none' : ''}`}>
      {keys.map((row, i) => (
        <div key={i} className="flex justify-center gap-1.5 w-full">
          {i === 1 && <div className="w-[5%]" />}
          {row.map((key) => {
            const isSpecial = key === 'Enter' || key === 'Backspace';
            return (
              <button
                key={key}
                onClick={() => onKeyPress(key)}
                className={`flex items-center justify-center font-bold rounded-md border-b-4 transition-all active:border-b-0 active:translate-y-1 uppercase ${
                  isSpecial ? 'px-3 py-4 text-xs min-w-[50px]' : 'flex-1 py-4 text-sm max-w-[40px]'
                } ${getKeyClass(key)}`}
              >
                {key === 'Backspace' ? '⌫' : key}
              </button>
            );
          })}
          {i === 1 && <div className="w-[5%]" />}
        </div>
      ))}
    </div>
  );
}
