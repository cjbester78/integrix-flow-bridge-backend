import React from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Download, Layers } from 'lucide-react';
import { FrontendApplicationGraph } from './FrontendApplicationGraph';
import { MainPagesGraph } from './MainPagesGraph';
import { CoreComponentsGraph } from './CoreComponentsGraph';
import { LoggingArchitectureGraph } from './LoggingArchitectureGraph';
import { ServicesLayerGraph } from './ServicesLayerGraph';
import { DataLayerGraph } from './DataLayerGraph';

export const ComponentizedSystemGraph: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold flex items-center gap-2">
            <Layers className="h-6 w-6" />
            System Architecture Components
          </h2>
          <p className="text-muted-foreground">
            Modular view of the system architecture broken down by functional areas
          </p>
        </div>
        <div className="flex gap-2">
          <Badge variant="secondary">Componentized</Badge>
          <Button variant="outline" size="sm">
            <Download className="h-4 w-4 mr-2" />
            Export All Components
          </Button>
        </div>
      </div>

      <Tabs defaultValue="frontend">
        <TabsList className="grid w-full grid-cols-6">
          <TabsTrigger value="frontend">Frontend Core</TabsTrigger>
          <TabsTrigger value="pages">Main Pages</TabsTrigger>
          <TabsTrigger value="components">Core Components</TabsTrigger>
          <TabsTrigger value="logging">Logging</TabsTrigger>
          <TabsTrigger value="services">Services</TabsTrigger>
          <TabsTrigger value="data">Data Layer</TabsTrigger>
        </TabsList>

        <TabsContent value="frontend">
          <FrontendApplicationGraph />
        </TabsContent>

        <TabsContent value="pages">
          <MainPagesGraph />
        </TabsContent>

        <TabsContent value="components">
          <CoreComponentsGraph />
        </TabsContent>

        <TabsContent value="logging">
          <LoggingArchitectureGraph />
        </TabsContent>

        <TabsContent value="services">
          <ServicesLayerGraph />
        </TabsContent>

        <TabsContent value="data">
          <DataLayerGraph />
        </TabsContent>
      </Tabs>
    </div>
  );
};