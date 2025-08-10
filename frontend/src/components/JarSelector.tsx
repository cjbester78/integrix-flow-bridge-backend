import React from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { FileArchive } from 'lucide-react';

interface JarFile {
  id: string;
  name: string;
  version: string;
  fileName: string;
  driverType: string;
}

interface JarSelectorProps {
  selectedJarId?: string;
  onJarSelect: (jarId: string) => void;
  label?: string;
  placeholder?: string;
  driverTypeFilter?: string;
}

export const JarSelector: React.FC<JarSelectorProps> = ({
  selectedJarId,
  onJarSelect,
  label = "Select JAR File",
  placeholder = "Choose a driver JAR file...",
  driverTypeFilter
}) => {
  // Mock data - replace with actual API call to your Java backend
  const availableJars: JarFile[] = [
    {
      id: '1',
      name: 'MySQL JDBC Driver',
      version: '8.0.33',
      fileName: 'mysql-connector-java-8.0.33.jar',
      driverType: 'Database'
    },
    {
      id: '2',
      name: 'PostgreSQL JDBC Driver',
      version: '42.6.0',
      fileName: 'postgresql-42.6.0.jar',
      driverType: 'Database'
    },
    {
      id: '3',
      name: 'ActiveMQ Client',
      version: '5.18.3',
      fileName: 'activemq-client-5.18.3.jar',
      driverType: 'Message Queue'
    }
  ];

  const filteredJars = driverTypeFilter 
    ? availableJars.filter(jar => jar.driverType === driverTypeFilter)
    : availableJars;

  const selectedJar = availableJars.find(jar => jar.id === selectedJarId);

  return (
    <div className="space-y-2">
      <Label className="text-sm font-medium">{label}</Label>
      <Select value={selectedJarId} onValueChange={onJarSelect}>
        <SelectTrigger className="w-full">
          <SelectValue placeholder={placeholder}>
            {selectedJar && (
              <div className="flex items-center gap-2">
                <FileArchive className="h-4 w-4" />
                <span>{selectedJar.name}</span>
                <Badge variant="secondary" className="text-xs">
                  v{selectedJar.version}
                </Badge>
              </div>
            )}
          </SelectValue>
        </SelectTrigger>
        <SelectContent className="bg-card border-border shadow-lg z-50 max-h-60">
          {filteredJars.map((jar) => (
            <SelectItem key={jar.id} value={jar.id}>
              <div className="flex items-center gap-2 w-full">
                <FileArchive className="h-4 w-4" />
                <div className="flex-1">
                  <div className="font-medium">{jar.name}</div>
                  <div className="text-xs text-muted-foreground">{jar.fileName}</div>
                </div>
                <div className="flex gap-1">
                  <Badge variant="secondary" className="text-xs">
                    v{jar.version}
                  </Badge>
                  <Badge variant="outline" className="text-xs">
                    {jar.driverType}
                  </Badge>
                </div>
              </div>
            </SelectItem>
          ))}
          {filteredJars.length === 0 && (
            <SelectItem value="no-jars-available" disabled>
              No JAR files available
              {driverTypeFilter && ` for ${driverTypeFilter}`}
            </SelectItem>
          )}
        </SelectContent>
      </Select>
      {selectedJar && (
        <div className="text-xs text-muted-foreground">
          Selected: {selectedJar.fileName} ({selectedJar.driverType})
        </div>
      )}
    </div>
  );
};