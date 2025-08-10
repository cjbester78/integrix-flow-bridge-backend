import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Play, Save, Copy } from 'lucide-react';

interface FlowActionsCardProps {
  onTestFlow: () => void;
  onSaveFlow: () => void;
}

export const FlowActionsCard = ({ onTestFlow, onSaveFlow }: FlowActionsCardProps) => {
  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.3s' }}>
      <CardHeader>
        <CardTitle>Actions</CardTitle>
        <CardDescription>Test and save your integration flow</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <Button 
          onClick={onTestFlow}
          className="w-full bg-gradient-accent hover:opacity-90 transition-all duration-300"
          variant="outline"
        >
          <Play className="h-4 w-4 mr-2" />
          Test Flow
        </Button>
        <Button 
          onClick={onSaveFlow}
          className="w-full bg-gradient-primary hover:opacity-90 transition-all duration-300"
        >
          <Save className="h-4 w-4 mr-2" />
          Save Flow
        </Button>
        <Separator />
        <Button variant="outline" className="w-full">
          <Copy className="h-4 w-4 mr-2" />
          Duplicate Flow
        </Button>
      </CardContent>
    </Card>
  );
};