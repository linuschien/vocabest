import React from 'react';
import { useStateValue } from '@json-render/react';
import {
  ComposedChart, Bar, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend
} from 'recharts';
import { ErrorBoundary } from '@/components/ErrorBoundary';

export default function ProgressChart({ element }: any) {
  const chartData = useStateValue('/data/stats/chartData') as any[];
  const isLoading = useStateValue('/state/stats/isLoading') as boolean;

  if (isLoading) {
    return (
      <div className="bg-card border border-border rounded-lg p-6 shadow-sm h-[500px] w-full flex items-center justify-center" id={element?.props?.id}>
        <div className="text-muted-foreground">載入中...</div>
      </div>
    );
  }

  if (!chartData || chartData.length === 0) return null;

  return (
    <div className="bg-card border border-border rounded-lg p-6 shadow-sm h-[500px] w-full" id={element?.props?.id}>
      <ErrorBoundary>
        <ComposedChart width={800} height={400} data={chartData} margin={{ top: 20, right: 20, bottom: 20, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="hsl(var(--border))" />
          <XAxis dataKey="displayDate" tick={{ fill: 'hsl(var(--muted-foreground))' }} axisLine={false} tickLine={false} dy={10} />
          <YAxis tick={{ fill: 'hsl(var(--muted-foreground))' }} axisLine={false} tickLine={false} dx={-10} />
          <Tooltip 
            contentStyle={{ backgroundColor: 'hsl(var(--popover))', borderColor: 'hsl(var(--border))', borderRadius: '8px', color: 'hsl(var(--popover-foreground))' }}
            itemStyle={{ color: 'hsl(var(--foreground))' }}
            formatter={(value: any, name: any) => [value, name === 'correct' ? '答對數' : name === 'wrong' ? '答錯數' : '每日目標']}
            labelFormatter={(label) => `日期: ${label}`}
          />
          <Legend verticalAlign="top" height={36} formatter={(value) => value === 'correct' ? '答對' : value === 'wrong' ? '答錯' : '每日目標'} />
          
          <Bar dataKey="correct" stackId="a" fill="hsl(var(--primary))" radius={[0, 0, 4, 4]} />
          <Bar dataKey="wrong" stackId="a" fill="hsl(var(--muted-foreground))" opacity={0.5} radius={[4, 4, 0, 0]} />
          <Line type="monotone" dataKey="target" stroke="hsl(var(--destructive))" strokeWidth={2} strokeDasharray="5 5" dot={{ r: 4, fill: 'hsl(var(--destructive))' }} />
        </ComposedChart>
      </ErrorBoundary>
    </div>
  );
}
