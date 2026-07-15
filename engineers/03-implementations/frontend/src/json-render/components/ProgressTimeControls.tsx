import React, { useRef, useEffect, useState } from 'react';
import { useStateStore, useStateValue } from '@json-render/react';
import { DayPicker } from 'react-day-picker';
import { subWeeks, addWeeks, subMonths, addMonths, format } from 'date-fns';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon } from 'lucide-react';
import 'react-day-picker/dist/style.css';

export default function ProgressTimeControls({ element }: any) {
  const store = useStateStore();
  const viewMode = (useStateValue('/state/stats/viewMode') || 'week') as 'week' | 'month';
  const currentDateStr = useStateValue('/state/stats/currentDate');
  const now = React.useMemo(() => new Date(), []);
  const currentDate = currentDateStr ? new Date(currentDateStr as string) : now;
  
  const startDateStr = useStateValue('/state/stats/startDate') as string;
  const endDateStr = useStateValue('/state/stats/endDate') as string;

  const [isCalendarOpen, setIsCalendarOpen] = useState(false);
  const calendarRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (calendarRef.current && !calendarRef.current.contains(e.target as Node)) {
        setIsCalendarOpen(false);
      }
    }
    if (isCalendarOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isCalendarOpen]);

  const setViewMode = (mode: 'week' | 'month') => store.set('/state/stats/viewMode', mode);
  const setCurrentDate = (date: Date) => store.set('/state/stats/currentDate', date.toISOString());

  const handlePrev = () => setCurrentDate(viewMode === 'week' ? subWeeks(currentDate, 1) : subMonths(currentDate, 1));
  const handleNext = () => setCurrentDate(viewMode === 'week' ? addWeeks(currentDate, 1) : addMonths(currentDate, 1));

  return (
    <div className="flex flex-wrap items-center justify-between bg-card border border-border rounded-lg p-4 shadow-sm gap-4 w-full" id={element?.props?.id}>
      <div className="flex bg-muted rounded-md p-1">
        <button 
          className={`px-4 py-1.5 text-sm font-medium rounded-sm transition-colors ${viewMode === 'week' ? 'bg-background shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'}`}
          onClick={() => setViewMode('week')}
        >
          週檢視
        </button>
        <button 
          className={`px-4 py-1.5 text-sm font-medium rounded-sm transition-colors ${viewMode === 'month' ? 'bg-background shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'}`}
          onClick={() => setViewMode('month')}
        >
          月檢視
        </button>
      </div>

      <div className="flex items-center gap-2">
        <button onClick={handlePrev} className="p-2 hover:bg-accent rounded-full border border-border bg-background text-foreground">
          <ChevronLeft className="w-4 h-4" />
        </button>
        
        <div className="relative" ref={calendarRef}>
          <button 
            onClick={() => setIsCalendarOpen(!isCalendarOpen)}
            className="flex items-center gap-2 px-4 py-2 border border-border rounded-md bg-background text-foreground hover:bg-accent min-w-[200px] justify-center shadow-sm"
          >
            <CalendarIcon className="w-4 h-4 text-muted-foreground" />
            <span className="text-sm font-medium">
              {startDateStr ? format(new Date(startDateStr), 'yyyy/MM/dd') : ''} - {endDateStr ? format(new Date(endDateStr), 'yyyy/MM/dd') : ''}
            </span>
          </button>

          {isCalendarOpen && (
            <div className="absolute top-full mt-2 left-1/2 -translate-x-1/2 z-50 bg-popover text-popover-foreground border shadow-md rounded-md p-4">
              <DayPicker 
                mode="single"
                selected={currentDate}
                onSelect={(date) => {
                  if (date) {
                    setCurrentDate(date);
                    setIsCalendarOpen(false);
                  }
                }}
                className="bg-card text-card-foreground"
              />
            </div>
          )}
        </div>

        <button onClick={handleNext} className="p-2 hover:bg-accent rounded-full border border-border bg-background text-foreground">
          <ChevronRight className="w-4 h-4" />
        </button>

        <button 
          onClick={() => setCurrentDate(new Date())}
          className="ml-2 px-3 py-1.5 text-sm font-medium border border-border rounded-md bg-background text-foreground hover:bg-accent shadow-sm transition-colors"
        >
          今天
        </button>
      </div>
    </div>
  );
}
