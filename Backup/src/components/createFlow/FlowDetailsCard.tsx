import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';

interface FlowDetailsCardProps {
  flowName: string;
  description: string;
  onFlowNameChange: (value: string) => void;
  onDescriptionChange: (value: string) => void;
}

export const FlowDetailsCard = ({
  flowName,
  description,
  onFlowNameChange,
  onDescriptionChange,
}: FlowDetailsCardProps) => {
  return (
    <Card className="animate-scale-in">
      <CardHeader>
        <CardTitle>Flow Details</CardTitle>
        <CardDescription>Configure the basic information for your integration flow</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="flowName">Flow Name *</Label>
          <Input
            id="flowName"
            placeholder="e.g., Customer Data Sync"
            value={flowName}
            onChange={(e) => onFlowNameChange(e.target.value)}
            className="transition-all duration-300 focus:scale-[1.01]"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="description">Description</Label>
          <Textarea
            id="description"
            placeholder="Describe what this integration flow does..."
            value={description}
            onChange={(e) => onDescriptionChange(e.target.value)}
            className="transition-all duration-300 focus:scale-[1.01]"
            rows={3}
          />
        </div>
      </CardContent>
    </Card>
  );
};