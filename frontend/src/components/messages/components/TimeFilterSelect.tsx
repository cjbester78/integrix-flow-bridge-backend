import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { TimeFilter } from '../types/timeFilter';

interface TimeFilterSelectProps {
  value: TimeFilter;
  onValueChange: (value: TimeFilter) => void;
}

export const TimeFilterSelect = ({ value, onValueChange }: TimeFilterSelectProps) => {
  return (
    <Select value={value} onValueChange={onValueChange}>
      <SelectTrigger className="w-48">
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="today">Today</SelectItem>
        <SelectItem value="today-00">Today 00:00 - 00:59</SelectItem>
        <SelectItem value="today-01">Today 01:00 - 01:59</SelectItem>
        <SelectItem value="today-02">Today 02:00 - 02:59</SelectItem>
        <SelectItem value="today-03">Today 03:00 - 03:59</SelectItem>
        <SelectItem value="today-04">Today 04:00 - 04:59</SelectItem>
        <SelectItem value="today-05">Today 05:00 - 05:59</SelectItem>
        <SelectItem value="today-06">Today 06:00 - 06:59</SelectItem>
        <SelectItem value="today-07">Today 07:00 - 07:59</SelectItem>
        <SelectItem value="today-08">Today 08:00 - 08:59</SelectItem>
        <SelectItem value="today-09">Today 09:00 - 09:59</SelectItem>
        <SelectItem value="today-10">Today 10:00 - 10:59</SelectItem>
        <SelectItem value="today-11">Today 11:00 - 11:59</SelectItem>
        <SelectItem value="today-12">Today 12:00 - 12:59</SelectItem>
        <SelectItem value="today-13">Today 13:00 - 13:59</SelectItem>
        <SelectItem value="today-14">Today 14:00 - 14:59</SelectItem>
        <SelectItem value="today-15">Today 15:00 - 15:59</SelectItem>
        <SelectItem value="today-16">Today 16:00 - 16:59</SelectItem>
        <SelectItem value="today-17">Today 17:00 - 17:59</SelectItem>
        <SelectItem value="today-18">Today 18:00 - 18:59</SelectItem>
        <SelectItem value="today-19">Today 19:00 - 19:59</SelectItem>
        <SelectItem value="today-20">Today 20:00 - 20:59</SelectItem>
        <SelectItem value="today-21">Today 21:00 - 21:59</SelectItem>
        <SelectItem value="today-22">Today 22:00 - 22:59</SelectItem>
        <SelectItem value="today-23">Today 23:00 - 23:59</SelectItem>
        <SelectItem value="yesterday">Yesterday</SelectItem>
        <SelectItem value="last-7-days">Past 7 Days</SelectItem>
        <SelectItem value="last-30-days">Past 30 Days</SelectItem>
        <SelectItem value="all">All Messages</SelectItem>
      </SelectContent>
    </Select>
  );
};