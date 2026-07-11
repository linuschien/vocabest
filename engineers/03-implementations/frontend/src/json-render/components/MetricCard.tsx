import React from 'react';
import { useStateValue } from '@json-render/react';

export default function MetricCard({ element }: any) {
  const { id, label, variant, value: valueProp, current: currentProp, goal: goalProp } = element?.props ?? {};

  // Resolve bound state values (library auto-resolves $state bindings to actual values)
  const value = typeof valueProp === 'number' ? valueProp : (Number(valueProp) || 0);
  const current = typeof currentProp === 'number' ? currentProp : (Number(currentProp) || 0);
  const goal = typeof goalProp === 'number' ? goalProp : (Number(goalProp) || 1);

  if (variant === 'progress') {
    const pct = goal > 0 ? Math.min(100, Math.round((current / goal) * 100)) : 0;
    
    // 計算 HSL 值：從藍色 (Hue ~220) 經過綠、黃、橙，漸變到紅色 (Hue 0)
    // 0% = 藍色 (220), 50% = 綠色 (110), 100% = 紅色 (0)
    const hue = Math.round(220 - (pct / 100) * 220);

    return (
      <div className="bg-card text-card-foreground rounded-lg border shadow-sm p-6 flex flex-col gap-3" id={id}>
        <h3 className="text-sm font-medium text-muted-foreground">{label}</h3>
        <div className="flex items-end gap-2">
          <span className="text-3xl font-bold">{current}</span>
          <span className="text-sm text-muted-foreground mb-1">/ {goal} 題</span>
        </div>
        <div className="w-full bg-muted rounded-full h-2.5">
          <div
            className="h-2.5 rounded-full transition-all duration-500"
            style={{ 
              width: `${pct}%`,
              backgroundColor: `hsl(${hue}, 85%, 55%)`
            }}
            role="progressbar"
            aria-valuenow={current}
            aria-valuemin={0}
            aria-valuemax={goal}
            aria-label={`${current} of ${goal} questions completed`}
          />
        </div>
        <p className="text-xs text-muted-foreground">{pct}% 完成</p>
      </div>
    );
  }

  if (variant === 'streak') {
    // 依據冪函數 (Power 0.35) 計算火焰，讓區間更平緩適中：
    // 1~6天=1把, 7~50天=2把, 51~159天=3把, 160~364天=4把, 365天以上=5把
    const fireCount = value > 0 
      ? Math.min(5, Math.floor(Math.pow(value / 365, 0.35) * 4) + 1)
      : 0;

    const fires = fireCount > 0 
      ? Array.from({ length: fireCount }).map((_, i) => (
          <span key={i} className="text-3xl" role="img" aria-label="streak fire">🔥</span>
        ))
      : <span className="text-3xl opacity-50" role="img" aria-label="streak cold">🧊</span>;

    return (
      <div className="bg-card text-card-foreground rounded-lg border shadow-sm p-6 flex flex-col justify-center" id={id}>
        <h3 className="text-sm font-medium text-muted-foreground mb-1">{label}</h3>
        <div className="flex items-center gap-2">
          <div className="flex">{fires}</div>
          <span className="text-3xl font-bold ml-1">{value}</span>
          <span className="text-sm text-muted-foreground self-end mb-1">天</span>
        </div>
        {value === 0 && (
          <p className="text-xs text-muted-foreground mt-1">今日完成一題即可開始連勝！</p>
        )}
      </div>
    );
  }

  // Default: plain number card
  return (
    <div className="bg-card text-card-foreground rounded-lg border shadow-sm p-6 flex flex-col justify-center" id={id}>
      <h3 className="text-sm font-medium text-muted-foreground mb-1">{label}</h3>
      <div className="text-3xl font-bold">{value}</div>
    </div>
  );
}
