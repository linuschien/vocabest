import React, { useEffect } from 'react';
import { Renderer, useStateStore, useStateValue } from '@json-render/react';
import { componentRegistry } from '@/json-render/component-registry';
import spec from '@/schemas/learning-statistics.render-schema.json';
import { useListDailyProgresses } from '@/hooks/use-list-daily-progresses';
import { startOfWeek, endOfWeek, startOfMonth, endOfMonth, format } from 'date-fns';
import { ErrorBoundary } from '@/components/ErrorBoundary';

export default function LearningStatisticsPage() {
  const store = useStateStore();
  const userId = store.get('/data/user/id') as string;
  const userDailyTarget = store.get('/data/user/dailyTargetQuestions') as number | undefined;

  // Read viewMode and currentDate from store (default week and today)
  const viewMode = useStateValue('/state/stats/viewMode') || 'week';
  const currentDateStr = useStateValue('/state/stats/currentDate');
  // Ensure currentDate is stable. We use useMemo with no dependencies to only initialize once.
  const now = React.useMemo(() => new Date(), []);
  const currentDate = currentDateStr ? new Date(currentDateStr as string) : now;

  // Calculate startDate and endDate
  const { startDate, endDate } = React.useMemo(() => {
    if (viewMode === 'week') {
      return {
        startDate: startOfWeek(currentDate, { weekStartsOn: 1 }), // Monday
        endDate: endOfWeek(currentDate, { weekStartsOn: 1 }),
      };
    } else {
      return {
        startDate: startOfMonth(currentDate),
        endDate: endOfMonth(currentDate),
      };
    }
  }, [currentDate.getTime(), viewMode]);

  const startDateStr = format(startDate, 'yyyy-MM-dd');
  const endDateStr = format(endDate, 'yyyy-MM-dd');

  // Fetch data
  const { data, isLoading } = useListDailyProgresses({
    userId,
    startDate: startDateStr,
    endDate: endDateStr
  });
  
  const progresses = data || [];
  const progressesJson = JSON.stringify(progresses);

  // Calculate summaries and update store
  useEffect(() => {
    // Only update if changed
    const currentIsLoading = store.get('/state/stats/isLoading');
    const currentStartDate = store.get('/state/stats/startDate');
    const currentEndDate = store.get('/state/stats/endDate');
    
    let stateChanged = false;
    if (currentIsLoading !== isLoading || currentStartDate !== startDateStr || currentEndDate !== endDateStr) {
      store.set('/state/stats', {
        ...((store.get('/state/stats') || {}) as any),
        startDate: startDateStr,
        endDate: endDateStr,
        isLoading
      });
      stateChanged = true;
    }
    
    if (!isLoading) {
      // Summaries
      let totalAnswered = 0;
      let totalCorrect = 0;
      let targetMetDays = 0;
      
      const data = [];
      let current = new Date(startDate);
      while (current <= endDate) {
        const dStr = format(current, 'yyyy-MM-dd');
        const p = progresses.find(x => x.date === dStr);
        const answered = p ? p.answeredQuestions : 0;
        const correct = p ? p.correctQuestions : 0;
        const target = p ? p.targetQuestions : (userDailyTarget ?? 10);
        
        totalAnswered += answered;
        totalCorrect += correct;
        if (answered >= target && target > 0) targetMetDays++;
        
        data.push({
          date: dStr,
          displayDate: format(current, 'MM/dd'),
          answered,
          correct,
          wrong: answered - correct,
          target
        });
        current.setDate(current.getDate() + 1);
      }

      const avgAccuracy = totalAnswered > 0 ? Math.round((totalCorrect / totalAnswered) * 100) : 0;
      
      const currentStats = store.get('/data/stats') as any;
      // Simple shallow check on primitive stats to avoid unnecessary updates
      if (
        !currentStats ||
        currentStats.totalAnswered !== totalAnswered ||
        currentStats.avgAccuracy !== avgAccuracy ||
        currentStats.targetMetDays !== targetMetDays ||
        JSON.stringify(currentStats.chartData) !== JSON.stringify(data)
      ) {
        store.set('/data/stats', {
          progresses,
          chartData: data,
          totalAnswered,
          avgAccuracy,
          targetMetDays
        });
      }
    }
  }, [startDateStr, endDateStr, progressesJson, isLoading, store, startDate, endDate]);

  return (
    <ErrorBoundary>
      <Renderer spec={spec as any} registry={componentRegistry} />
    </ErrorBoundary>
  );
}
