import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { CalendarIcon, X } from 'lucide-react';
import { format } from 'date-fns';
import { cn } from '@/lib/utils';

interface LogFiltersProps {
  onFilterChange: (filters: {
    level?: string;
    dateRange?: { start?: string; end?: string };
  }) => void;
}

export const LogFilters = ({ onFilterChange }: LogFiltersProps) => {
  const [startDate, setStartDate] = useState<Date>();
  const [endDate, setEndDate] = useState<Date>();

  const handleDateRangeChange = () => {
    onFilterChange({
      dateRange: {
        start: startDate?.toISOString(),
        end: endDate?.toISOString(),
      },
    });
  };

  const clearDateRange = () => {
    setStartDate(undefined);
    setEndDate(undefined);
    onFilterChange({
      dateRange: {},
    });
  };

  const setQuickRange = (days: number) => {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - days);
    
    setStartDate(start);
    setEndDate(end);
    
    onFilterChange({
      dateRange: {
        start: start.toISOString(),
        end: end.toISOString(),
      },
    });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2">
        <Label className="text-sm font-medium">Date Range:</Label>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={() => setQuickRange(1)}>
            Last 24h
          </Button>
          <Button variant="outline" size="sm" onClick={() => setQuickRange(7)}>
            Last 7d
          </Button>
          <Button variant="outline" size="sm" onClick={() => setQuickRange(30)}>
            Last 30d
          </Button>
          {(startDate || endDate) && (
            <Button variant="ghost" size="sm" onClick={clearDateRange}>
              <X className="h-4 w-4" />
            </Button>
          )}
        </div>
      </div>

      <div className="flex gap-4">
        <div className="space-y-2">
          <Label className="text-sm">Start Date</Label>
          <Popover>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                size="sm"
                className={cn(
                  'w-[200px] justify-start text-left font-normal',
                  !startDate && 'text-muted-foreground'
                )}
              >
                <CalendarIcon className="mr-2 h-4 w-4" />
                {startDate ? format(startDate, 'PPP') : 'Pick start date'}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0" align="start">
              <Calendar
                mode="single"
                selected={startDate}
                onSelect={(date) => {
                  setStartDate(date);
                  if (date && endDate) handleDateRangeChange();
                }}
                initialFocus
                className="p-3 pointer-events-auto"
              />
            </PopoverContent>
          </Popover>
        </div>

        <div className="space-y-2">
          <Label className="text-sm">End Date</Label>
          <Popover>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                size="sm"
                className={cn(
                  'w-[200px] justify-start text-left font-normal',
                  !endDate && 'text-muted-foreground'
                )}
              >
                <CalendarIcon className="mr-2 h-4 w-4" />
                {endDate ? format(endDate, 'PPP') : 'Pick end date'}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0" align="start">
              <Calendar
                mode="single"
                selected={endDate}
                onSelect={(date) => {
                  setEndDate(date);
                  if (startDate && date) handleDateRangeChange();
                }}
                initialFocus
                className="p-3 pointer-events-auto"
              />
            </PopoverContent>
          </Popover>
        </div>
      </div>
    </div>
  );
};