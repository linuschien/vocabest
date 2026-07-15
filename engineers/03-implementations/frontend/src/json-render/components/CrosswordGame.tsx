import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useStateStore, useStateValue } from '@json-render/react';
import { useGetCrosswordTargets } from '@/hooks/use-get-crossword-targets';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { RotateCcw } from 'lucide-react';
// @ts-ignore
import clg from 'crossword-layout-generator';

interface WordResult {
  clue: string;
  answer: string;
  startx: number;
  starty: number;
  orientation: 'across' | 'down' | 'none';
  position: number;
}

interface Layout {
  table: string[][];
  result: WordResult[];
  rows: number;
  cols: number;
}

function isConnected(table: string[][]) {
  let startX = -1, startY = -1;
  let totalLetters = 0;
  for (let y = 0; y < table.length; y++) {
    for (let x = 0; x < table[y].length; x++) {
      if (table[y][x] !== '-') {
        if (startX === -1) { startX = x; startY = y; }
        totalLetters++;
      }
    }
  }
  if (totalLetters === 0) return true;
  
  const visited = new Set<string>();
  const queue = [[startX, startY]];
  visited.add(`${startX},${startY}`);
  
  let connectedLetters = 0;
  while (queue.length > 0) {
    const [x, y] = queue.shift()!;
    connectedLetters++;
    
    const dirs = [[0,1],[1,0],[0,-1],[-1,0]];
    for (const [dx, dy] of dirs) {
      const nx = x + dx, ny = y + dy;
      if (ny >= 0 && ny < table.length && nx >= 0 && nx < table[ny].length && table[ny][nx] !== '-') {
        if (!visited.has(`${nx},${ny}`)) {
          visited.add(`${nx},${ny}`);
          queue.push([nx, ny]);
        }
      }
    }
  }
  return connectedLetters === totalLetters;
}

function Keyboard({ onKeyPress, disabled }: { onKeyPress: (key: string) => void, disabled: boolean }) {
  const keys = [
    ['q','w','e','r','t','y','u','i','o','p'],
    ['a','s','d','f','g','h','j','k','l'],
    ['z','x','c','v','b','n','m', 'Backspace']
  ];

  return (
    <div className="flex flex-col gap-2 mt-8 max-w-full overflow-x-auto pb-4">
      {keys.map((row, i) => (
        <div key={i} className="flex justify-center gap-1.5">
          {row.map((key) => {
            const isBackspace = key === 'Backspace';
            return (
              <button
                key={key}
                disabled={disabled}
                onClick={() => onKeyPress(key)}
                className={`
                  flex items-center justify-center rounded-md font-bold text-sm uppercase transition-all
                  ${isBackspace ? 'px-4 bg-muted text-muted-foreground w-16' : 'w-10 bg-muted text-muted-foreground'}
                  h-14 hover:opacity-80 active:scale-95 disabled:opacity-50 disabled:pointer-events-none
                `}
              >
                {isBackspace ? '⌫' : key}
              </button>
            );
          })}
        </div>
      ))}
    </div>
  );
}

export default function CrosswordGame({ element }: any) {
  const store = useStateStore();
  const userId = store.get('/data/user/id') as string;
  
  const { data: targets, isLoading, isError, refetch } = useGetCrosswordTargets(userId);
  
  const [retryCount, setRetryCount] = useState(0);
  const MAX_RETRIES = 5;

  const layout = useStateValue('/state/crossword/layout') as Layout | null;
  const userGrid = (useStateValue('/state/crossword/userGrid') as string[][]) || [];

  const setLayout = useCallback((newLayout: Layout | null) => {
    store.set('/state/crossword/layout', newLayout);
  }, [store]);

  const setUserGrid = useCallback((newGrid: string[][]) => {
    store.set('/state/crossword/userGrid', newGrid);
  }, [store]);

  const [selectedCell, setSelectedCell] = useState<{x: number, y: number} | null>(null);
  const [selectedWordIndex, setSelectedWordIndex] = useState<number | null>(null);

  const resetGame = () => {
    setLayout(null);
    setUserGrid([]);
    setRetryCount(0);
    refetch();
  };

  useEffect(() => {
    if (layout) return;
    if (!targets || targets.length === 0) return;
    
    const input = targets.map(t => ({
      clue: `${t.chineseTranslation} ${t.partsOfSpeech ? `(${t.partsOfSpeech})` : ''}`,
      answer: t.word.toLowerCase()
    }));

    const newLayout = clg.generateLayout(input);
    const unplaced = newLayout.result.some((w: any) => w.orientation === 'none');
    const connected = isConnected(newLayout.table);

    if ((unplaced || !connected) && retryCount < MAX_RETRIES) {
      setRetryCount(c => c + 1);
      refetch();
    } else {
      setLayout(newLayout);
      const initialGrid = Array(newLayout.rows).fill(null).map(() => Array(newLayout.cols).fill(''));
      setUserGrid(initialGrid);
      setRetryCount(0);
    }
  }, [targets, retryCount, refetch, layout, setLayout, setUserGrid]);

  const isWordCorrect = useCallback((word: WordResult) => {
    if (!layout || word.orientation === 'none') return false;
    return Array.from(word.answer).every((char, i) => {
      const x = word.orientation === 'across' ? word.startx - 1 + i : word.startx - 1;
      const y = word.orientation === 'down' ? word.starty - 1 + i : word.starty - 1;
      return userGrid[y] && userGrid[y][x] === char;
    });
  }, [layout, userGrid]);

  const isCellLocked = useCallback((x: number, y: number) => {
    if (!layout) return false;
    const intersectingWords = layout.result.filter(w => {
      if (w.orientation === 'none') return false;
      const sx = w.startx - 1;
      const sy = w.starty - 1;
      const len = w.answer.length;
      if (w.orientation === 'across') {
        return y === sy && x >= sx && x < sx + len;
      } else {
        return x === sx && y >= sy && y < sy + len;
      }
    });
    return intersectingWords.some(w => isWordCorrect(w));
  }, [layout, isWordCorrect]);

  const handleCellClick = (x: number, y: number) => {
    if (!layout || layout.table[y][x] === '-') return;
    
    // Find words intersecting this cell
    const intersectingWords = layout.result.map((w, i) => ({w, i})).filter(({w}) => {
      if (w.orientation === 'none') return false;
      const sx = w.startx - 1;
      const sy = w.starty - 1;
      const len = w.answer.length;
      if (w.orientation === 'across') {
        return y === sy && x >= sx && x < sx + len;
      } else {
        return x === sx && y >= sy && y < sy + len;
      }
    });

    if (intersectingWords.length === 0) return;

    // Toggle orientation if clicking the same cell that has an intersection
    if (selectedCell?.x === x && selectedCell?.y === y && intersectingWords.length > 1) {
      const nextWord = intersectingWords.find(w => w.i !== selectedWordIndex);
      setSelectedWordIndex(nextWord?.i ?? intersectingWords[0].i);
    } else {
      setSelectedCell({x, y});
      // Try to keep same orientation if possible, otherwise just pick the first
      const currentWordObj = intersectingWords.find(w => w.i === selectedWordIndex);
      if (!currentWordObj) {
        setSelectedWordIndex(intersectingWords[0].i);
      }
    }
  };

  const advanceCell = useCallback((backward = false) => {
    if (!selectedCell || selectedWordIndex === null || !layout) return;
    const word = layout.result[selectedWordIndex];
    let { x, y } = selectedCell;
    const dx = word.orientation === 'across' ? (backward ? -1 : 1) : 0;
    const dy = word.orientation === 'down' ? (backward ? -1 : 1) : 0;
    
    const sx = word.startx - 1;
    const sy = word.starty - 1;
    const len = word.answer.length;
    
    while (true) {
      x += dx;
      y += dy;
      if (word.orientation === 'across') {
        if (x < sx || x >= sx + len) break;
      } else {
        if (y < sy || y >= sy + len) break;
      }
      
      if (!isCellLocked(x, y)) {
        setSelectedCell({x, y});
        return;
      }
    }
  }, [selectedCell, selectedWordIndex, layout, isCellLocked]);

  const handleKeyPress = useCallback((key: string) => {
    if (!layout || !selectedCell) return;
    const { x, y } = selectedCell;
    
    if (isCellLocked(x, y)) {
      advanceCell(key === 'Backspace');
      return;
    }
    
    if (key === 'Backspace') {
      if (userGrid[y][x] !== '') {
        const newGrid = [...userGrid];
        newGrid[y] = [...newGrid[y]];
        newGrid[y][x] = '';
        setUserGrid(newGrid);
      } else {
        advanceCell(true);
      }
    } else if (key.length === 1 && /[a-z]/i.test(key)) {
      const newGrid = [...userGrid];
      newGrid[y] = [...newGrid[y]];
      newGrid[y][x] = key.toLowerCase();
      setUserGrid(newGrid);
      advanceCell(false);
    }
  }, [layout, selectedCell, userGrid, advanceCell, isCellLocked, setUserGrid]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Backspace') {
        handleKeyPress('Backspace');
      } else if (e.key.length === 1 && /[a-zA-Z]/.test(e.key)) {
        handleKeyPress(e.key.toLowerCase());
      } else if (e.key === 'ArrowUp') {
        // Simple navigation could be added, but skipping for brevity
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyPress]);

  if (isLoading || !layout) {
    return (
      <div className="flex flex-col items-center justify-center w-full max-w-lg mx-auto p-8 text-muted-foreground" id={element?.props?.id}>
        <RotateCcw className="w-8 h-8 animate-spin mb-4" />
        {retryCount > 0 ? `重新生成網格中 (${retryCount}/${MAX_RETRIES})...` : '載入 Crossword 題目中...'}
      </div>
    );
  }
  
  if (isError) {
    return <div className="p-8 text-center text-destructive font-bold" id={element?.props?.id}>無法載入題目，請稍後再試。</div>;
  }

  // Check win condition
  const isWon = layout.result.every(word => {
    if (word.orientation === 'none') return true;
    return Array.from(word.answer).every((char, i) => {
      const x = word.orientation === 'across' ? word.startx - 1 + i : word.startx - 1;
      const y = word.orientation === 'down' ? word.starty - 1 + i : word.starty - 1;
      return userGrid[y] && userGrid[y][x] === char;
    });
  });

  const getCellClass = (x: number, y: number) => {
    if (layout.table[y][x] === '-') return 'w-10 h-10 shrink-0 bg-transparent border-transparent';
    
    const isSelected = selectedCell?.x === x && selectedCell?.y === y;
    
    let isWordHighlight = false;
    if (selectedWordIndex !== null) {
      const w = layout.result[selectedWordIndex];
      const sx = w.startx - 1;
      const sy = w.starty - 1;
      const len = w.answer.length;
      if (w.orientation === 'across') {
        isWordHighlight = y === sy && x >= sx && x < sx + len;
      } else if (w.orientation === 'down') {
        isWordHighlight = x === sx && y >= sy && y < sy + len;
      }
    }

    const isLocked = isCellLocked(x, y);

    let baseClass = 'w-10 h-10 shrink-0 border-2 flex items-center justify-center text-xl font-bold uppercase transition-colors cursor-pointer ';
    
    if (isWon || isLocked) {
      return baseClass + 'bg-green-500 text-white border-green-600';
    }

    if (isSelected) {
      baseClass += 'bg-yellow-200 border-yellow-400 text-black ';
    } else if (isWordHighlight) {
      baseClass += 'bg-yellow-50 border-yellow-200 text-black ';
    } else {
      baseClass += 'bg-background border-border text-foreground ';
    }

    // if (isFilled && isCorrect) {
    //   // Optionally highlight correct cells green immediately, but classic crossword doesn't
    // }

    return baseClass;
  };

  const activeWord = selectedWordIndex !== null ? layout.result[selectedWordIndex] : null;

  return (
    <ErrorBoundary>
      <div className="flex flex-col items-center w-full max-w-2xl mx-auto p-4 select-none" id={element?.props?.id}>
        
        {isWon && (
          <div className="mb-6 flex flex-col items-center gap-4 animate-in fade-in zoom-in">
            <div className="px-6 py-3 bg-green-500 text-white text-lg font-bold rounded-lg shadow-xl">
              🎉 恭喜完成！
            </div>
            <button
              onClick={resetGame}
              className="px-4 py-2 bg-primary text-primary-foreground font-bold rounded-md hover:opacity-90 shadow-sm"
            >
              再玩一次
            </button>
          </div>
        )}

        <div className="bg-card border border-border p-4 rounded-xl shadow-sm mb-6 max-w-full overflow-x-auto">
          <div className="flex flex-col" style={{ width: 'max-content' }}>
            {layout.table.map((row, y) => (
              <div key={y} className="flex">
                {row.map((cell, x) => (
                  <div 
                    key={`${x}-${y}`} 
                    className={getCellClass(x, y)}
                    onClick={() => handleCellClick(x, y)}
                  >
                    {cell !== '-' && (
                      <span className="relative">
                        {userGrid[y]?.[x] || ''}
                        {layout.result.find(w => w.startx - 1 === x && w.starty - 1 === y) && (
                          <span className="absolute -top-3 -left-3 text-[10px] font-normal text-muted-foreground">
                            {layout.result.find(w => w.startx - 1 === x && w.starty - 1 === y)?.position}
                          </span>
                        )}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>

        <div className="w-full grid grid-cols-1 md:grid-cols-2 gap-4 mb-6 text-sm">
          <div className="flex flex-col gap-2 p-4 bg-muted/30 rounded-lg">
            <h3 className="font-bold text-lg mb-2">橫向 (Across)</h3>
            {layout.result.filter(w => w.orientation === 'across').map(w => {
              const isCorrect = isWordCorrect(w);
              return (
                <div 
                  key={w.position} 
                  className={`flex gap-2 p-1 rounded cursor-pointer transition-colors
                    ${isCorrect ? 'opacity-50 line-through' : ''}
                    ${selectedWordIndex !== null && layout.result[selectedWordIndex] === w ? 'bg-yellow-100 text-yellow-900 font-medium' : 'hover:bg-muted'}
                  `}
                  onClick={() => {
                    setSelectedWordIndex(layout.result.indexOf(w));
                    let firstX = w.startx - 1;
                    let firstY = w.starty - 1;
                    let found = false;
                    for (let i = 0; i < w.answer.length; i++) {
                      const cx = w.orientation === 'across' ? firstX + i : firstX;
                      const cy = w.orientation === 'down' ? firstY + i : firstY;
                      if (!isCellLocked(cx, cy)) {
                        setSelectedCell({x: cx, y: cy});
                        found = true;
                        break;
                      }
                    }
                    if (!found) {
                      setSelectedCell({x: firstX, y: firstY});
                    }
                  }}
                >
                  <span className="font-bold min-w-[20px]">{w.position}.</span>
                  <span>{w.clue}</span>
                </div>
              );
            })}
          </div>
          <div className="flex flex-col gap-2 p-4 bg-muted/30 rounded-lg">
            <h3 className="font-bold text-lg mb-2">直向 (Down)</h3>
            {layout.result.filter(w => w.orientation === 'down').map(w => {
              const isCorrect = isWordCorrect(w);
              return (
                <div 
                  key={w.position} 
                  className={`flex gap-2 p-1 rounded cursor-pointer transition-colors
                    ${isCorrect ? 'opacity-50 line-through' : ''}
                    ${selectedWordIndex !== null && layout.result[selectedWordIndex] === w ? 'bg-yellow-100 text-yellow-900 font-medium' : 'hover:bg-muted'}
                  `}
                  onClick={() => {
                    setSelectedWordIndex(layout.result.indexOf(w));
                    let firstX = w.startx - 1;
                    let firstY = w.starty - 1;
                    let found = false;
                    for (let i = 0; i < w.answer.length; i++) {
                      const cx = w.orientation === 'across' ? firstX + i : firstX;
                      const cy = w.orientation === 'down' ? firstY + i : firstY;
                      if (!isCellLocked(cx, cy)) {
                        setSelectedCell({x: cx, y: cy});
                        found = true;
                        break;
                      }
                    }
                    if (!found) {
                      setSelectedCell({x: firstX, y: firstY});
                    }
                  }}
                >
                  <span className="font-bold min-w-[20px]">{w.position}.</span>
                  <span>{w.clue}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Floating Clue for mobile */}
        {activeWord && (
          <div className="md:hidden sticky top-4 w-full bg-primary text-primary-foreground p-3 rounded-lg shadow-lg font-medium text-center z-10 mb-4 animate-in slide-in-from-top-2">
            {activeWord.position}. {activeWord.orientation === 'across' ? '橫向' : '直向'}: {activeWord.clue}
          </div>
        )}

        <Keyboard onKeyPress={handleKeyPress} disabled={isWon} />
      </div>
    </ErrorBoundary>
  );
}
