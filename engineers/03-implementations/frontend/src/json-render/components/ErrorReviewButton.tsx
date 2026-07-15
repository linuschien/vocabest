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
        'h-9 w-full flex flex-row gap-2 items-center justify-center rounded-md border px-4 py-2 shadow-sm text-sm font-medium',
        'transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
        hasErrors
          ? 'border-primary bg-primary hover:bg-primary/90 text-primary-foreground shadow-md cursor-pointer'
          : 'border-border bg-muted/40 hover:bg-muted/60 cursor-pointer',
      ].join(' ')}
    >
      <span className={`whitespace-nowrap ${!hasErrors ? 'text-muted-foreground' : ''}`}>
        🛡️ 弱點特訓
      </span>
      {hasErrors ? (
        <span className="inline-flex items-center rounded-full bg-destructive text-destructive-foreground px-2 py-0.5 text-xs font-medium shadow-sm whitespace-nowrap">
          待複習: {count} 題
        </span>
      ) : (
        <span className="text-xs text-muted-foreground whitespace-nowrap">今日無錯題</span>
      )}
    </button>
  );
}
