import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { AdapterTestSuite } from '@/components/adapter/AdapterTestSuite';

export const AdapterTestSuitePage = () => {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Adapter Test Suite</h2>
        <p className="text-muted-foreground">
          Comprehensive testing and validation for communication adapters
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Adapter Validation Testing</CardTitle>
        </CardHeader>
        <CardContent>
          <AdapterTestSuite />
        </CardContent>
      </Card>
    </div>
  );
};

export default AdapterTestSuitePage;