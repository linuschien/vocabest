import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useStateStore } from '@json-render/react';
import { useGetMemoryGameTargets } from '@/hooks/use-get-memory-game-targets';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { RotateCcw, Brain, Clock, Zap } from 'lucide-react';

type Difficulty = 'easy' | 'medium' | 'hard' | 'hell';

interface DifficultyConfig {
  pairs: number;
  delay: number;
  isHell: boolean;
}

const DIFFICULTIES: Record<Difficulty, DifficultyConfig> = {
  easy: { pairs: 4, delay: 1500, isHell: false },
  medium: { pairs: 6, delay: 1000, isHell: false },
  hard: { pairs: 8, delay: 500, isHell: false },
  hell: { pairs: 10, delay: 500, isHell: true },
};

interface Card {
  id: string; // unique UUID for the physical card
  wordId: string; // the ID of the WordBankResponse
  text: string; // The english word or chinese translation
  type: 'word' | 'translation';
  isFlipped: boolean;
  isMatched: boolean;
  isShaking: boolean; // For Hell mode shake animation
}

function generateUUID() {
  return Math.random().toString(36).substring(2) + Date.now().toString(36);
}

function shuffleArray<T>(array: T[]): T[] {
  const newArr = [...array];
  for (let i = newArr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [newArr[i], newArr[j]] = [newArr[j], newArr[i]];
  }
  return newArr;
}

export default function MemoryGame({ element }: any) {
  const store = useStateStore();
  const userId = store.get('/data/user/id') as string;
  
  const [gameState, setGameState] = useState<'setup' | 'playing' | 'won'>('setup');
  const [difficulty, setDifficulty] = useState<Difficulty>('medium');
  
  const config = DIFFICULTIES[difficulty];
  
  const { data: targets, isLoading, isError, refetch } = useGetMemoryGameTargets(userId, config.pairs);

  const [cards, setCards] = useState<Card[]>([]);
  const [flippedIndices, setFlippedIndices] = useState<number[]>([]);
  const [moves, setMoves] = useState(0);
  const [consecutiveFails, setConsecutiveFails] = useState(0);
  const [timeElapsed, setTimeElapsed] = useState(0);
  const [timerIntervalId, setTimerIntervalId] = useState<number | null>(null);
  
  const [toastMsg, setToastMsg] = useState('');

  // Setup Game Board
  useEffect(() => {
    if (gameState === 'playing' && targets && targets.length === config.pairs) {
      const newCards: Card[] = [];
      targets.forEach(t => {
        newCards.push({
          id: generateUUID(),
          wordId: t.id,
          text: t.word,
          type: 'word',
          isFlipped: false,
          isMatched: false,
          isShaking: false,
        });
        newCards.push({
          id: generateUUID(),
          wordId: t.id,
          text: t.chineseTranslation || t.word,
          type: 'translation',
          isFlipped: false,
          isMatched: false,
          isShaking: false,
        });
      });
      setCards(shuffleArray(newCards));
      setFlippedIndices([]);
      setMoves(0);
      setConsecutiveFails(0);
      setTimeElapsed(0);
      
      const id = window.setInterval(() => {
        setTimeElapsed(prev => prev + 1);
      }, 1000);
      setTimerIntervalId(id);
      
      return () => clearInterval(id);
    }
  }, [gameState, targets, config.pairs]);

  // Clean up timer on unmount or win
  useEffect(() => {
    if (gameState !== 'playing' && timerIntervalId) {
      clearInterval(timerIntervalId);
    }
  }, [gameState, timerIntervalId]);

  const startGame = (diff: Difficulty) => {
    setDifficulty(diff);
    setGameState('playing');
    refetch();
  };

  const showToast = (msg: string) => {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(''), 3000);
  };

  const handleCardClick = (index: number) => {
    if (flippedIndices.length === 2) return; // Prevent clicking while 2 are flipped
    if (cards[index].isFlipped || cards[index].isMatched) return; // Ignore already flipped

    const newCards = [...cards];
    newCards[index].isFlipped = true;
    setCards(newCards);

    const newFlipped = [...flippedIndices, index];
    setFlippedIndices(newFlipped);

    if (newFlipped.length === 2) {
      setMoves(m => m + 1);
      const [firstIndex, secondIndex] = newFlipped;
      const firstCard = newCards[firstIndex];
      const secondCard = newCards[secondIndex];

      if (firstCard.wordId === secondCard.wordId && firstCard.type !== secondCard.type) {
        // Match!
        setTimeout(() => {
          setCards(prev => {
            const next = [...prev];
            next[firstIndex].isMatched = true;
            next[secondIndex].isMatched = true;
            return next;
          });
          setFlippedIndices([]);
          setConsecutiveFails(0);
          
          // Check win condition
          const allMatched = newCards.every((c, i) => i === firstIndex || i === secondIndex || c.isMatched);
          if (allMatched) {
            setGameState('won');
          }
        }, 500);

        // Speak the English word
        const englishCard = firstCard.type === 'word' ? firstCard : secondCard;
        try {
          const utterance = new SpeechSynthesisUtterance(englishCard.text);
          utterance.lang = 'en-US';
          window.speechSynthesis.speak(utterance);
        } catch (e) {
          console.error("Speech Synthesis failed", e);
        }

      } else {
        // No match
        const newFails = consecutiveFails + 1;
        setConsecutiveFails(newFails);

        setTimeout(() => {
          setCards(prev => {
            const next = [...prev];
            next[firstIndex].isFlipped = false;
            next[secondIndex].isFlipped = false;
            return next;
          });
          setFlippedIndices([]);

          // Trigger Hell mode dynamic shuffle
          if (config.isHell && newFails >= 3) {
            setConsecutiveFails(0);
            showToast('連續失誤 3 次，卡牌已重新洗牌 😈！');
            
            setCards(prev => {
              const matchedCards = prev.filter(c => c.isMatched);
              let unmatchedCards = prev.filter(c => !c.isMatched);
              
              // Trigger shake animation for unmatched cards
              unmatchedCards = unmatchedCards.map(c => ({ ...c, isShaking: true }));
              unmatchedCards = shuffleArray(unmatchedCards);
              
              const next = [];
              let mIdx = 0;
              let uIdx = 0;
              for (let i = 0; i < prev.length; i++) {
                if (prev[i].isMatched) {
                  next.push(matchedCards[mIdx++]);
                } else {
                  next.push(unmatchedCards[uIdx++]);
                }
              }
              
              // Remove shaking after animation completes
              setTimeout(() => {
                setCards(currentCards => currentCards.map(c => ({ ...c, isShaking: false })));
              }, 600);
              
              return next;
            });
          }
        }, config.delay);
      }
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  if (gameState === 'setup') {
    return (
      <div className="flex flex-col items-center justify-center w-full max-w-2xl mx-auto p-8 bg-card rounded-2xl shadow-sm border border-border" id={element?.props?.id}>
        <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mb-6">
          <Brain className="w-8 h-8 text-primary" />
        </div>
        <h2 className="text-3xl font-extrabold mb-2 text-center">翻牌對對碰</h2>
        <p className="text-muted-foreground mb-8 text-center max-w-md">選擇一個難度來開始挑戰！找出所有的「英文 ↔ 中文」配對，地獄模式中連續失誤會觸發動態洗牌！</p>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full">
          {(Object.entries(DIFFICULTIES) as [Difficulty, DifficultyConfig][]).map(([diff, conf]) => (
            <button
              key={diff}
              onClick={() => startGame(diff)}
              className={`p-6 border-2 rounded-xl flex flex-col items-center justify-center gap-2 transition-all hover:-translate-y-1 ${
                diff === 'hell' ? 'border-destructive/30 hover:border-destructive hover:bg-destructive/5 text-destructive' : 'border-border hover:border-primary hover:bg-primary/5'
              }`}
            >
              <div className="text-xl font-bold uppercase">{diff}</div>
              <div className="text-sm opacity-80">{conf.pairs} 組單字</div>
              {conf.isHell && <div className="text-xs font-bold mt-2 px-2 py-1 bg-destructive/10 rounded-full">動態洗牌 😈</div>}
            </button>
          ))}
        </div>
      </div>
    );
  }

  if (isLoading || !targets || targets.length !== config.pairs) {
    return (
      <div className="flex flex-col items-center justify-center w-full max-w-lg mx-auto p-8 text-muted-foreground" id={element?.props?.id}>
        <RotateCcw className="w-8 h-8 animate-spin mb-4" />
        載入卡牌中...
      </div>
    );
  }
  
  if (isError) {
    return <div className="p-8 text-center text-destructive font-bold" id={element?.props?.id}>無法載入單字，請稍後再試。</div>;
  }

  // Calculate grid columns based on card count
  const totalCards = config.pairs * 2;
  const gridCols = totalCards <= 8 ? 'grid-cols-4' : totalCards <= 12 ? 'grid-cols-4 sm:grid-cols-6' : 'grid-cols-4 sm:grid-cols-5';

  return (
    <ErrorBoundary>
      <div className="flex flex-col items-center w-full max-w-5xl mx-auto p-4 select-none relative" id={element?.props?.id}>
        
        {/* Toast */}
        {toastMsg && (
          <div className="fixed top-24 left-1/2 -translate-x-1/2 z-[200] px-6 py-3 bg-destructive text-destructive-foreground font-bold rounded-full shadow-2xl animate-in slide-in-from-top-4 fade-in duration-300 flex items-center gap-2">
            <Zap className="w-5 h-5 fill-current" />
            {toastMsg}
          </div>
        )}

        {/* Header Stats */}
        <div className="flex items-center justify-between w-full max-w-3xl mb-8 bg-card px-6 py-4 rounded-2xl shadow-sm border border-border">
          <div className="flex items-center gap-6">
            <div className="flex flex-col">
              <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">難度</span>
              <span className={`text-lg font-extrabold uppercase ${config.isHell ? 'text-destructive' : 'text-primary'}`}>{difficulty}</span>
            </div>
            <div className="flex flex-col">
              <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">配對進度</span>
              <span className="text-lg font-bold">{cards.filter(c => c.isMatched).length / 2} / {config.pairs}</span>
            </div>
          </div>
          <div className="flex items-center gap-6">
            <div className="flex flex-col items-end">
              <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">步數</span>
              <span className="text-lg font-bold">{moves}</span>
            </div>
            <div className="flex flex-col items-end">
              <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">時間</span>
              <span className="text-lg font-bold flex items-center gap-1.5 font-mono"><Clock className="w-4 h-4 opacity-50" /> {formatTime(timeElapsed)}</span>
            </div>
          </div>
        </div>

        {/* Board */}
        <div className={`grid ${gridCols} gap-3 sm:gap-4 w-full max-w-3xl perspective-1000`}>
          {cards.map((card, index) => {
            const isVisible = card.isFlipped || card.isMatched;
            
            return (
              <div 
                key={card.id}
                className={`relative aspect-[3/4] cursor-pointer group transform-style-3d transition-transform duration-500 ${card.isShaking ? 'animate-shake' : ''}`}
                onClick={() => handleCardClick(index)}
                style={{ transform: isVisible ? 'rotateY(180deg)' : 'rotateY(0deg)' }}
              >
                {/* Back of card (visible when faced down) */}
                <div className="absolute inset-0 backface-hidden bg-primary/10 border-2 border-primary/20 rounded-xl shadow-sm flex items-center justify-center group-hover:bg-primary/20 transition-colors">
                  <Brain className="w-8 h-8 text-primary/30" />
                </div>
                
                {/* Front of card (visible when flipped) */}
                <div className={`absolute inset-0 backface-hidden rounded-xl shadow-md border-2 flex flex-col items-center justify-center p-2 text-center transform rotate-y-180 transition-colors ${
                  card.isMatched ? 'bg-green-100 border-green-400 dark:bg-green-900/30 dark:border-green-700' : 'bg-card border-primary/40'
                }`}>
                  <span className={`font-bold ${card.type === 'word' ? 'text-lg sm:text-xl text-primary' : 'text-base sm:text-lg text-foreground'}`}>
                    {card.text}
                  </span>
                  {card.isMatched && (
                    <div className="absolute top-2 right-2 w-3 h-3 bg-green-500 rounded-full animate-ping shadow-sm"></div>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Win Screen Modal */}
        {gameState === 'won' && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center bg-background/80 backdrop-blur-sm animate-in fade-in">
            <div className="flex flex-col items-center p-10 bg-card border border-border shadow-2xl rounded-2xl animate-in zoom-in-95 max-w-md w-full mx-4">
              <div className="w-20 h-20 bg-green-500/20 text-green-500 rounded-full flex items-center justify-center mb-6">
                <Brain className="w-10 h-10" />
              </div>
              <h2 className="text-4xl font-extrabold mb-2 text-center text-foreground">
                恭喜通關！
              </h2>
              <p className="text-muted-foreground mb-8 text-center text-lg">你完成了 {difficulty.toUpperCase()} 難度的挑戰。</p>
              
              <div className="grid grid-cols-2 gap-4 w-full mb-8">
                <div className="flex flex-col items-center p-4 bg-muted rounded-xl">
                  <span className="text-sm font-bold text-muted-foreground mb-1">總花費時間</span>
                  <span className="text-2xl font-mono font-bold text-primary">{formatTime(timeElapsed)}</span>
                </div>
                <div className="flex flex-col items-center p-4 bg-muted rounded-xl">
                  <span className="text-sm font-bold text-muted-foreground mb-1">翻牌次數</span>
                  <span className="text-2xl font-mono font-bold text-primary">{moves}</span>
                </div>
              </div>
              
              <button 
                onClick={() => {
                  setGameState('setup');
                }}
                className="w-full py-4 bg-primary text-primary-foreground font-bold rounded-xl shadow-lg hover:opacity-90 transition-transform active:scale-95 flex items-center justify-center gap-2 text-lg"
              >
                <RotateCcw className="w-5 h-5" />
                再玩一次
              </button>
            </div>
          </div>
        )}

      </div>
    </ErrorBoundary>
  );
}
