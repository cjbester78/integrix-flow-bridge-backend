// @ts-nocheck - Temporary suppression for unused imports/variables
import { Card, CardContent } from '@/components/ui/card';
import { EmptyState } from '@/components/ui/empty-state';
import { AdapterCard } from './AdapterCard';
import { AdapterMonitoring } from '@/services/adapterMonitoringService';
import { Radio } from 'lucide-react';

interface AdapterListProps {
  adapters: AdapterMonitoring[];
  onUpdate?: (adapterId: string, updates: Partial<AdapterMonitoring>) => void;
  onDelete?: (adapterId: string) => void;
}

export const AdapterList = ({ adapters, onUpdate, onDelete }: AdapterListProps) => {
  return (
    <div className="space-y-4">
      {adapters.length > 0 ? (
        adapters.map((adapter) => (
          <AdapterCard 
            key={adapter.id} 
            adapter={adapter}
            onUpdate={(updates) => onUpdate?.(adapter.id, updates)}
            onDelete={() => onDelete?.(adapter.id)} 
          />
        ))
      ) : (
        <EmptyState
          icon={Radio}
          title="No adapters found"
          description="Adapters will appear here once your communication adapters are configured and deployed."
        />
      )}
    </div>
  );
};