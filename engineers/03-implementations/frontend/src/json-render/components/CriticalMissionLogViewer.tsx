import React, { useState, useMemo, useEffect } from 'react';
import { useStateStore } from '@json-render/react';
import { useListErrorEvents, ErrorEvent } from '@/hooks/use-list-error-events';
import { ChevronLeft, ChevronRight, Volume2, CheckCircle2, XCircle, RotateCcw, AlertTriangle } from 'lucide-react';
import { ErrorBoundary } from '@/components/ErrorBoundary';

type TimeRange = 'TODAY' | 'PAST_WEEK' | 'PAST_MONTH' | 'ALL_TIME';

export default function CriticalMissionLogViewer({ element }: any) {
  const store = useStateStore();
  const userId = store.get('/data/user/id') as string;
  
  const [timeRange, setTimeRange] = useState<TimeRange>('TODAY');
  const [absoluteIndex, setAbsoluteIndex] = useState(0);

  // When time range changes, reset index to 0
  useEffect(() => {
    setAbsoluteIndex(0);
  }, [timeRange]);

  const { startDate, endDate } = useMemo(() => {
    const end = new Date();
    let start = new Date();
    start.setHours(0, 0, 0, 0);

    if (timeRange === 'TODAY') {
      // start is already start of today
    } else if (timeRange === 'PAST_WEEK') {
      start.setDate(start.getDate() - 7);
    } else if (timeRange === 'PAST_MONTH') {
      start.setDate(start.getDate() - 30);
    } else {
      return { startDate: undefined, endDate: undefined };
    }
    
    return { 
      startDate: start.toISOString(), 
      endDate: end.toISOString() 
    };
  }, [timeRange]);

  const pageSize = 10;
  const currentPage = Math.floor(absoluteIndex / pageSize);
  const cardIndex = absoluteIndex % pageSize;

  const { data, isLoading, isError, isFetching } = useListErrorEvents({
    userId,
    startDate,
    endDate,
    page: currentPage,
    size: pageSize
  });

  const totalElements = data?.totalElements || 0;
  const currentEvent: ErrorEvent | undefined = data?.content?.[cardIndex];

  const handleNext = () => {
    if (absoluteIndex < totalElements - 1) {
      setAbsoluteIndex(prev => prev + 1);
    }
  };

  const handlePrev = () => {
    if (absoluteIndex > 0) {
      setAbsoluteIndex(prev => prev - 1);
    }
  };

  const playTTS = (text: string) => {
    if (text && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utter = new SpeechSynthesisUtterance(text);
      utter.lang = 'en-US';
      utter.rate = 0.9;
      window.speechSynthesis.speak(utter);
    }
  };

  return (
    <ErrorBoundary>
      <div className="flex flex-col w-full max-w-4xl mx-auto px-4 md:px-8 pb-8 pt-2" id={element?.props?.id}>
        
        {/* Time Range Selector */}
        <div className="flex flex-wrap items-center justify-center gap-2 mb-8 bg-card p-2 rounded-xl border border-border shadow-sm">
          {[
            { value: 'TODAY', label: '今日' },
            { value: 'PAST_WEEK', label: '過去一週' },
            { value: 'PAST_MONTH', label: '過去一個月' },
            { value: 'ALL_TIME', label: '所有時間' }
          ].map(opt => (
            <button
              key={opt.value}
              onClick={() => setTimeRange(opt.value as TimeRange)}
              className={`px-4 py-2 text-sm font-bold rounded-lg transition-colors ${
                timeRange === opt.value 
                  ? 'bg-primary text-primary-foreground shadow-sm' 
                  : 'hover:bg-muted text-muted-foreground'
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>

        {/* Content Area */}
        <div className="relative min-h-[500px] flex flex-col items-center justify-center">
          
          {isLoading ? (
            <div className="flex flex-col items-center text-muted-foreground animate-pulse">
              <RotateCcw className="w-8 h-8 animate-spin mb-4" />
              載入歷史紀錄中...
            </div>
          ) : isError ? (
            <div className="text-destructive font-bold flex flex-col items-center">
              <AlertTriangle className="w-12 h-12 mb-4 opacity-50" />
              無法載入紀錄，請稍後再試。
            </div>
          ) : totalElements === 0 ? (
            <div className="flex flex-col items-center text-muted-foreground p-12 bg-card rounded-3xl border border-border border-dashed w-full">
              <CheckCircle2 className="w-16 h-16 text-green-500/50 mb-4" />
              <h3 className="text-xl font-bold text-foreground mb-2">太棒了！</h3>
              <p>這個時間範圍內沒有任何錯題紀錄。</p>
            </div>
          ) : currentEvent ? (
            <div className="w-full flex flex-col gap-6">
              
              {/* Pagination Status Pill */}
              <div className="flex justify-center mb-2">
                <div className="font-mono font-bold text-sm bg-muted px-4 py-1.5 rounded-full text-muted-foreground flex items-center gap-2 border border-border shadow-sm">
                  <span>第 {absoluteIndex + 1} 筆</span>
                  <span className="opacity-50">/</span>
                  <span>共 {totalElements} 筆</span>
                  {isFetching && <RotateCcw className="w-3 h-3 animate-spin ml-2" />}
                </div>
              </div>

              {/* Flashcard Container (Carousel) */}
              <div className="w-full relative group">
                
                {/* Left Button */}
                <button 
                  onClick={handlePrev} 
                  disabled={absoluteIndex === 0 || isFetching}
                  className="absolute -left-3 md:-left-6 top-1/2 -translate-y-1/2 z-10 p-2 md:p-3 bg-background border-2 border-border shadow-xl rounded-full hover:bg-muted disabled:opacity-0 disabled:pointer-events-none transition-all"
                  data-testid="prev-card-btn"
                >
                  <ChevronLeft className="w-6 h-6 md:w-8 md:h-8 text-foreground" />
                </button>

                {/* Right Button */}
                <button 
                  onClick={handleNext} 
                  disabled={absoluteIndex === totalElements - 1 || isFetching}
                  className="absolute -right-3 md:-right-6 top-1/2 -translate-y-1/2 z-10 p-2 md:p-3 bg-background border-2 border-border shadow-xl rounded-full hover:bg-muted disabled:opacity-0 disabled:pointer-events-none transition-all"
                  data-testid="next-card-btn"
                >
                  <ChevronRight className="w-6 h-6 md:w-8 md:h-8 text-foreground" />
                </button>

              {/* Flashcard */}
              <div className="bg-card border-2 border-border shadow-xl rounded-3xl overflow-hidden flex flex-col transition-all">
                
                {/* 1. 情境回放 */}
                <div className="bg-muted/30 p-6 md:p-8 pb-4">
                  <div className="text-sm font-bold text-muted-foreground mb-3 flex items-center gap-2 uppercase tracking-wider">
                    <RotateCcw className="w-4 h-4" /> 情境回放 (Contextual Cloze)
                  </div>
                  <div className="text-lg md:text-xl font-medium leading-relaxed mb-3 text-foreground">
                    {currentEvent.quizQuestion.contextualCloze}
                  </div>
                  <div className="text-sm md:text-base text-muted-foreground">
                    {currentEvent.quizQuestion.chineseTranslation}
                  </div>
                </div>

                {/* 2. 案發現場對比 */}
                <div className="px-6 md:px-8 pb-6 md:pb-8 grid grid-cols-2 gap-4 border-b border-border bg-muted/30">
                  <div className="flex items-center justify-between p-3 rounded-xl border border-green-200 bg-green-50 dark:border-green-900 dark:bg-green-950/20 gap-2">
                    <span className="text-xs font-bold text-green-600 dark:text-green-400 flex items-center gap-1 whitespace-nowrap shrink-0">
                      <CheckCircle2 className="w-3 h-3" /> 正確答案
                    </span>
                    <span className="text-sm md:text-base font-bold text-green-700 dark:text-green-300 truncate text-right" title={currentEvent.quizQuestion.correctAnswer}>
                      {currentEvent.quizQuestion.correctAnswer}
                    </span>
                  </div>
                  <div className="flex items-center justify-between p-3 rounded-xl border border-red-200 bg-red-50 dark:border-red-900 dark:bg-red-950/20 gap-2">
                    <span className="text-xs font-bold text-red-600 dark:text-red-400 flex items-center gap-1 whitespace-nowrap shrink-0">
                      <XCircle className="w-3 h-3" /> 你當時選了
                    </span>
                    <span className="text-sm md:text-base font-bold text-red-700 dark:text-red-300 line-through opacity-80 truncate text-right" title={currentEvent.selectedDistractor}>
                      {currentEvent.selectedDistractor}
                    </span>
                  </div>
                </div>

                {/* 3. 單字與發音 & 4. 詞性與翻譯 */}
                <div className="p-6 md:p-10 flex flex-col items-center justify-center text-center border-b border-border bg-background">
                  <div className="flex items-center justify-center gap-4 mb-4 relative group">
                    <h1 className="text-5xl md:text-6xl font-extrabold text-primary tracking-tight">
                      {currentEvent.quizQuestion.wordBank.word}
                    </h1>
                    <button 
                      onClick={() => playTTS(currentEvent.quizQuestion.wordBank.word)}
                      className="w-12 h-12 rounded-full bg-primary/10 hover:bg-primary/20 text-primary flex items-center justify-center transition-colors active:scale-95 absolute -right-16 opacity-50 group-hover:opacity-100"
                      title="朗讀單字"
                    >
                      <Volume2 className="w-6 h-6" />
                    </button>
                  </div>
                  <div className="flex items-center justify-center gap-3">
                    <span className="px-3 py-1 bg-muted text-muted-foreground text-sm font-mono font-bold rounded-full">
                      {currentEvent.quizQuestion.wordBank.partsOfSpeech || 'unknown'}
                    </span>
                    <span className="text-xl font-bold text-foreground">
                      {currentEvent.quizQuestion.wordBank.chineseTranslation}
                    </span>
                  </div>
                </div>

                {/* 5. 記憶輔助解析 */}
                <div className="p-6 md:p-8 bg-muted/10 grid grid-cols-1 md:grid-cols-2 gap-6">
                  {currentEvent.quizQuestion.explanationRootAffix && (
                    <div className="flex flex-col gap-2">
                      <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">字根字首解析</span>
                      <p className="text-sm leading-relaxed">{currentEvent.quizQuestion.explanationRootAffix}</p>
                    </div>
                  )}
                  {currentEvent.quizQuestion.explanationMnemonic && (
                    <div className="flex flex-col gap-2">
                      <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider">聯想記憶法</span>
                      <p className="text-sm leading-relaxed">{currentEvent.quizQuestion.explanationMnemonic}</p>
                    </div>
                  )}
                  {!currentEvent.quizQuestion.explanationRootAffix && !currentEvent.quizQuestion.explanationMnemonic && (
                    <div className="col-span-full text-center text-sm text-muted-foreground opacity-50 py-4">
                      無記憶輔助解析資料
                    </div>
                  )}
                </div>

              </div>
            </div>
            </div>
          ) : (
            <div className="flex flex-col items-center text-muted-foreground">
              找不到該筆資料...
            </div>
          )}
        </div>
      </div>
    </ErrorBoundary>
  );
}
