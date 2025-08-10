
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

interface AdapterFiltersProps {
  selectedStatus: string | null;
  onStatusChange: (status: string | null) => void;
}

export function AdapterFilters({ selectedStatus, onStatusChange }: AdapterFiltersProps) {
  const statuses = [
    { value: null, label: 'All' },
    { value: 'active', label: 'Active' },
    { value: 'running', label: 'Running' },
    { value: 'stopped', label: 'Stopped' },
    { value: 'error', label: 'Error' },
  ];

  return (
    <Card className="p-4">
      <h3 className="text-sm font-semibold mb-3">Filter by Status</h3>
      <div className="flex gap-2 flex-wrap">
        {statuses.map((status) => (
          <Button
            key={status.value || 'all'}
            variant={selectedStatus === status.value ? 'default' : 'secondary'}
            size="sm"
            onClick={() => onStatusChange(status.value)}
          >
            {status.label}
          </Button>
        ))}
      </div>
    </Card>
  );
}