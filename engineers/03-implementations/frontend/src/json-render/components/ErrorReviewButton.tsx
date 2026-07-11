import React from 'react';
import { useStateValue } from '@json-render/react';

/**
 * ErrorReviewButton — 弱點特訓入口按鈕
 * 讀取 /data/errorReviewCount 並動態顯示待複習題數 badge。
 *
 * 點擊永遠可以導航（由 error-review-board page 負責處理無錯題的空白狀態）。
 * 視覺上：count=0 顯示灰色提示文，count>0 顯示紅色 badge。
 */
export default function ErrorReviewButton({ element, emit }: any) {
  const { id } = element?.props ?? {};
  const rawCount = useStateValue('/data/errorReviewCount');
  const count = typeof rawCount === 'number' ? rawCount : (Number(rawCount) || 0);
  const hasErrors = count > 0;

  const handleClick = () => {
    if (emit) emit('press');
  };

  return (
    <button
      id={id}
      onClick={handleClick}
      className={[
        'h-20 w-full flex flex-col items-center justify-center rounded-lg border px-4 shadow-sm',
        'transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
        hasErrors
          ? 'border-border bg-card hover:bg-accent hover:text-accent-foreground cursor-pointer'
          : 'border-border bg-muted/40 hover:bg-muted/60 cursor-pointer',
      ].join(' ')}
    >
      <span className={`text-sm font-semibold ${!hasErrors ? 'text-muted-foreground' : ''}`}>
        🛡️ 弱點特訓
      </span>
      {hasErrors ? (
        <span className="mt-1 inline-flex items-center rounded-full bg-destructive/15 px-2.5 py-0.5 text-xs font-medium text-destructive">
          待複習: {count} 題
        </span>
      ) : (
        <span className="text-xs text-muted-foreground mt-1">今日無錯題，繼續保持！</span>
      )}
    </button>
  );
}
