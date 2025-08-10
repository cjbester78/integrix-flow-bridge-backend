import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { DataStructure } from '@/types/dataStructures';
import { Eye, Edit, Download } from 'lucide-react';

interface StructurePreviewProps {
  selectedStructure: DataStructure;
}

const renderStructurePreview = (structure: any, depth = 0) => {
  if (!structure) return null;
  
  return (
    <div className={`ml-${depth * 4} space-y-1`}>
      {typeof structure === 'object' && !Array.isArray(structure) ? (
        Object.entries(structure).map(([key, value]) => (
          <div key={key} className="text-sm">
            <span className="font-medium text-primary">{key}</span>
            <span className="text-muted-foreground">: </span>
            {typeof value === 'string' ? (
              <Badge variant="outline" className="text-xs">{value}</Badge>
            ) : (
              <div className="mt-1">
                {renderStructurePreview(value, depth + 1)}
              </div>
            )}
          </div>
        ))
      ) : (
        <Badge variant="outline" className="text-xs">{String(structure)}</Badge>
      )}
    </div>
  );
};

export const StructurePreview: React.FC<StructurePreviewProps> = ({ selectedStructure }) => {
  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.3s' }}>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Eye className="h-5 w-5" />
          Structure Preview
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div>
            <h4 className="font-medium">{selectedStructure.name}</h4>
            <p className="text-sm text-muted-foreground">{selectedStructure.description}</p>
            {selectedStructure.namespace && (
              <div className="mt-2 text-xs text-muted-foreground">
                <span className="font-medium">Namespace:</span> {selectedStructure.namespace.uri}
                {selectedStructure.namespace.prefix && (
                  <span className="ml-2">({selectedStructure.namespace.prefix})</span>
                )}
              </div>
            )}
          </div>
          
          <Separator />
          
          <div className="bg-muted/50 p-3 rounded-lg">
            <Label className="text-xs font-medium">Structure Definition</Label>
            <div className="mt-2 max-h-40 overflow-y-auto">
              {renderStructurePreview(selectedStructure.structure)}
            </div>
          </div>
          
          <div className="flex gap-2">
            <Button size="sm" variant="outline" className="flex-1">
              <Edit className="h-4 w-4 mr-2" />
              Edit
            </Button>
            <Button size="sm" variant="outline" className="flex-1">
              <Download className="h-4 w-4 mr-2" />
              Export
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};