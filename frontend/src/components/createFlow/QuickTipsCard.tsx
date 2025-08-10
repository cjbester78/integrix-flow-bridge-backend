import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export const QuickTipsCard = () => {
  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.5s' }}>
      <CardHeader>
        <CardTitle>Quick Tips</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-2 text-sm text-muted-foreground">
          <div className="flex items-start gap-2">
            <div className="h-1.5 w-1.5 rounded-full bg-primary mt-2 flex-shrink-0" />
            <span>Test your flow before saving to ensure connectivity</span>
          </div>
          <div className="flex items-start gap-2">
            <div className="h-1.5 w-1.5 rounded-full bg-primary mt-2 flex-shrink-0" />
            <span>Add transformations to modify data between systems</span>
          </div>
          <div className="flex items-start gap-2">
            <div className="h-1.5 w-1.5 rounded-full bg-primary mt-2 flex-shrink-0" />
            <span>Use descriptive names for easier management</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};