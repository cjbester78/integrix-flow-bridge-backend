import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { DataStructure } from '@/types/dataStructures';

interface Adapter {
  id: string;
  name: string;
  icon: React.ComponentType<{ className?: string }>;
  category: string;
}

interface FlowSummaryCardProps {
  sourceAdapter: string;
  targetAdapter: string;
  sourceStructure: string;
  targetStructure: string;
  selectedTransformations: string[];
  adapters: Adapter[];
  sampleStructures: DataStructure[];
}

export const FlowSummaryCard = ({
  sourceAdapter,
  targetAdapter,
  sourceStructure,
  targetStructure,
  selectedTransformations,
  adapters,
  sampleStructures,
}: FlowSummaryCardProps) => {
  const getAdapterById = (id: string) => adapters.find(adapter => adapter.id === id);
  const getStructureById = (id: string) => sampleStructures.find(s => s.id === id);

  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.4s' }}>
      <CardHeader>
        <CardTitle>Flow Summary</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Status:</span>
            <Badge variant="secondary">Draft</Badge>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Source:</span>
            <span>{sourceAdapter ? getAdapterById(sourceAdapter)?.name : 'Not selected'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Source Structure:</span>
            <span>{sourceStructure ? getStructureById(sourceStructure)?.name : 'Not selected'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Target:</span>
            <span>{targetAdapter ? getAdapterById(targetAdapter)?.name : 'Not selected'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Target Structure:</span>
            <span>{targetStructure ? getStructureById(targetStructure)?.name : 'Not selected'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Transformations:</span>
            <span>{selectedTransformations.length}</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};