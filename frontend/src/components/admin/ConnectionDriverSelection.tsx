import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { EmptyState } from '@/components/ui/empty-state';
import { Database, MessageSquare, FileArchive, RefreshCw } from 'lucide-react';
import { JdbcDriverModal } from './JdbcDriverModal';
import { JmsDriverModal } from './JmsDriverModal';
import { JarFile } from '@/types/admin';
import { Skeleton } from '@/components/ui/skeleton';

interface ConnectionDriverSelectionProps {
  jarFiles: JarFile[];
  isLoading?: boolean;
  onRefresh?: () => void;
  onJarFileAdded: (jarFile: JarFile) => void;
  onJarFileDeleted: (id: string) => void;
}

export const ConnectionDriverSelection = ({ jarFiles, isLoading = false, onRefresh, onJarFileAdded, onJarFileDeleted }: ConnectionDriverSelectionProps) => {
  const [showJdbcModal, setShowJdbcModal] = useState(false);
  const [showJmsModal, setShowJmsModal] = useState(false);

  return (
    <div className="space-y-6">
      {/* Driver Type Selection */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-4xl mx-auto">
        <Card 
          className="cursor-pointer transition-all duration-200 hover:shadow-lg hover:scale-105 border-2 hover:border-primary/20"
          onClick={() => setShowJdbcModal(true)}
        >
          <CardHeader className="text-center pb-4">
            <div className="mx-auto w-16 h-16 bg-blue-100 dark:bg-blue-900/20 rounded-full flex items-center justify-center mb-4">
              <Database className="h-8 w-8 text-info dark:text-info" />
            </div>
            <CardTitle className="text-xl">JDBC Driver</CardTitle>
            <CardDescription>
              Upload database connection drivers for MySQL, Oracle, PostgreSQL, and more
            </CardDescription>
          </CardHeader>
          <CardContent className="text-center pt-0">
            <Button variant="outline" className="w-full">
              Upload JDBC Driver
            </Button>
          </CardContent>
        </Card>

        <Card 
          className="cursor-pointer transition-all duration-200 hover:shadow-lg hover:scale-105 border-2 hover:border-primary/20"
          onClick={() => setShowJmsModal(true)}
        >
          <CardHeader className="text-center pb-4">
            <div className="mx-auto w-16 h-16 bg-green-100 dark:bg-green-900/20 rounded-full flex items-center justify-center mb-4">
              <MessageSquare className="h-8 w-8 text-success dark:text-success" />
            </div>
            <CardTitle className="text-xl">JMS Driver</CardTitle>
            <CardDescription>
              Upload Java Message Service drivers for ActiveMQ, RabbitMQ, and other message brokers
            </CardDescription>
          </CardHeader>
          <CardContent className="text-center pt-0">
            <Button variant="outline" className="w-full">
              Upload JMS Driver
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Driver Files List */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">Uploaded Connection Drivers</h3>
          <div className="flex items-center gap-2">
            <div className="text-sm text-muted-foreground">
              {jarFiles.length} driver{jarFiles.length !== 1 ? 's' : ''} uploaded
            </div>
            {onRefresh && (
              <Button variant="outline" size="sm" onClick={onRefresh} disabled={isLoading}>
                <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
              </Button>
            )}
          </div>
        </div>
        
        {isLoading ? (
          // Loading skeletons
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, index) => (
              <Card key={index}>
                <CardHeader className="pb-2">
                  <div className="flex items-center gap-2">
                    <Skeleton className="h-5 w-5 rounded" />
                    <Skeleton className="h-5 w-32" />
                  </div>
                  <Skeleton className="h-4 w-48 mt-1" />
                </CardHeader>
                <CardContent className="pt-0">
                  <div className="space-y-2">
                    {Array.from({ length: 3 }).map((_, i) => (
                      <div key={i} className="flex justify-between">
                        <Skeleton className="h-4 w-16" />
                        <Skeleton className="h-4 w-20" />
                      </div>
                    ))}
                  </div>
                  <Skeleton className="h-8 w-full mt-3" />
                </CardContent>
              </Card>
            ))}
          </div>
        ) : jarFiles.length === 0 ? (
          <EmptyState
            icon={FileArchive}
            title="No connection drivers uploaded"
            description="Upload JDBC database drivers or JMS client libraries to enable connections to external systems."
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {jarFiles.map((jarFile) => (
              <Card key={jarFile.id}>
                <CardHeader className="pb-2">
                  <div className="flex items-center gap-2">
                    {jarFile.driver_type === 'JDBC' ? (
                      <Database className="h-5 w-5 text-info" />
                    ) : (
                      <MessageSquare className="h-5 w-5 text-success" />
                    )}
                    <CardTitle className="text-base">{jarFile.name}</CardTitle>
                  </div>
                  <CardDescription className="text-xs">
                    {jarFile.file_name}
                  </CardDescription>
                </CardHeader>
                <CardContent className="pt-0">
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Version:</span>
                      <span>{jarFile.version}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Type:</span>
                      <span>{jarFile.driver_type}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Size:</span>
                      <span>{jarFile.size_bytes ? `${Math.round(jarFile.size_bytes / 1024)} KB` : 'N/A'}</span>
                    </div>
                  </div>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="w-full mt-3"
                    onClick={() => onJarFileDeleted(jarFile.id)}
                  >
                    Remove
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Modals */}
      <JdbcDriverModal 
        open={showJdbcModal}
        onOpenChange={setShowJdbcModal}
        onDriverAdded={onJarFileAdded}
      />
      
      <JmsDriverModal 
        open={showJmsModal}
        onOpenChange={setShowJmsModal}
        onDriverAdded={onJarFileAdded}
      />
    </div>
  );
};