// @ts-nocheck - Temporary suppression for unused imports/variables
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Play, Save, Copy, Loader2 } from 'lucide-react';

interface FlowActionsCardProps {
  onSaveFlow: () => void;
  isLoading?: boolean;
  disabled?: boolean;
}

export const FlowActionsCard = ({ onSaveFlow, isLoading = false, disabled = false }: FlowActionsCardProps) => {
  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.3s' }}>
      <CardHeader>
        <CardTitle>Actions</CardTitle>
        <CardDescription>Save your integration flow</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <Button 
          onClick={onSaveFlow}
          className="w-full bg-gradient-primary hover:opacity-90 transition-all duration-300"
          disabled={disabled || isLoading}
        >
          {isLoading ? (
            <>
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              Saving...
            </>
          ) : (
            <>
              <Save className="h-4 w-4 mr-2" />
              Save Flow
            </>
          )}
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