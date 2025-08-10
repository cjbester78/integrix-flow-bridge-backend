import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { DataStructure } from '@/types/dataStructures';
import { 
  FileText,
  FileJson,
  FileCode,
  Database,
  Copy,
  Trash2
} from 'lucide-react';

interface StructureLibraryProps {
  structures: DataStructure[];
  selectedStructure: DataStructure | null;
  onSelectStructure: (structure: DataStructure) => void;
  onDuplicateStructure: (structure: DataStructure) => void;
  onDeleteStructure: (id: string) => void;
}

const getTypeIcon = (type: string) => {
  switch (type) {
    case 'json': return FileJson;
    case 'xsd': case 'soap': case 'wsdl': return FileCode;
    case 'custom': return Database;
    default: return FileText;
  }
};

export const StructureLibrary: React.FC<StructureLibraryProps> = ({
  structures,
  selectedStructure,
  onSelectStructure,
  onDuplicateStructure,
  onDeleteStructure
}) => {
  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.2s' }}>
      <CardHeader>
        <CardTitle>Structure Library</CardTitle>
        <CardDescription>Existing data structures</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {structures.map((structure) => {
          const Icon = getTypeIcon(structure.type);
          return (
            <div
              key={structure.id}
              className={`p-3 border rounded-lg cursor-pointer transition-all duration-300 hover:shadow-soft ${
                selectedStructure?.id === structure.id ? 'border-primary bg-primary/10' : 'border-border'
              }`}
              onClick={() => onSelectStructure(structure)}
            >
              <div className="flex items-start justify-between">
                <div className="flex items-start gap-3 flex-1">
                  <Icon className="h-5 w-5 mt-0.5 text-primary" />
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-sm">{structure.name}</span>
                      <Badge variant="outline" className="text-xs">{structure.type.toUpperCase()}</Badge>
                    </div>
                    {structure.description && (
                      <p className="text-xs text-muted-foreground mt-1">{structure.description}</p>
                    )}
                    <div className="flex items-center gap-2 mt-2">
                      <Badge variant={structure.usage === 'source' ? 'default' : structure.usage === 'target' ? 'secondary' : 'outline'} className="text-xs">
                        {structure.usage}
                      </Badge>
                      <span className="text-xs text-muted-foreground">{structure.createdAt}</span>
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-1">
                  <Button
                    onClick={(e) => { e.stopPropagation(); onDuplicateStructure(structure); }}
                    size="sm"
                    variant="ghost"
                    className="h-6 w-6 p-0"
                  >
                    <Copy className="h-3 w-3" />
                  </Button>
                  <Button
                    onClick={(e) => { e.stopPropagation(); onDeleteStructure(structure.id); }}
                    size="sm"
                    variant="ghost"
                    className="h-6 w-6 p-0 text-destructive hover:text-destructive"
                  >
                    <Trash2 className="h-3 w-3" />
                  </Button>
                </div>
              </div>
            </div>
          );
        })}
        
        {structures.length === 0 && (
          <div className="text-center py-8 text-muted-foreground">
            <FileText className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>No data structures created yet. Create your first data structure using the tabs above.</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};