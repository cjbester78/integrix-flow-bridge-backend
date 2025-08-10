import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { GitBranch, ArrowRightLeft } from 'lucide-react';

export function CreateFlowSelection() {
  const navigate = useNavigate();

  return (
    <div className="container mx-auto p-6 max-w-4xl">
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight mb-2">Create Integration Flow</h1>
        <p className="text-muted-foreground">
          Choose the type of integration flow you want to create
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="cursor-pointer transition-all hover:shadow-lg border-2 hover:border-primary/20">
          <CardHeader className="text-center pb-4">
            <div className="mx-auto mb-4 p-3 rounded-full bg-primary/10">
              <GitBranch className="h-8 w-8 text-primary" />
            </div>
            <CardTitle className="text-xl">Create Orchestration Flow</CardTitle>
            <CardDescription className="text-sm">
              Design complex workflows with multiple systems, routing, and business logic
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-0">
            <div className="space-y-3 text-sm text-muted-foreground mb-6">
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Visual workflow designer</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Message routing & transformation</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Multi-system orchestration</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Error handling & compensation</span>
              </div>
            </div>
            <Button 
              className="w-full" 
              onClick={() => navigate('/create-orchestration-flow')}
            >
              Create Orchestration Flow
            </Button>
          </CardContent>
        </Card>

        <Card className="cursor-pointer transition-all hover:shadow-lg border-2 hover:border-primary/20">
          <CardHeader className="text-center pb-4">
            <div className="mx-auto mb-4 p-3 rounded-full bg-primary/10">
              <ArrowRightLeft className="h-8 w-8 text-primary" />
            </div>
            <CardTitle className="text-xl">Create Direct Mapping Flow</CardTitle>
            <CardDescription className="text-sm">
              Create simple point-to-point integration with field mapping
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-0">
            <div className="space-y-3 text-sm text-muted-foreground mb-6">
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Point-to-point integration</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Visual field mapping</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Transformation functions</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-primary"></div>
                <span>Quick setup & deployment</span>
              </div>
            </div>
            <Button 
              className="w-full"
              onClick={() => navigate('/create-direct-mapping-flow')}
            >
              Create Direct Mapping Flow
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}