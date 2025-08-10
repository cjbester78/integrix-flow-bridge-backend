import { ConnectionDriverSelection } from './ConnectionDriverSelection';
import { JarFile } from '@/types/admin';

interface JarFileManagementProps {
  jarFiles: JarFile[];
  isLoading?: boolean;
  onRefresh?: () => void;
  onJarFileAdded: (jarFile: JarFile) => void;
  onJarFileDeleted: (id: string) => void;
}

export const JarFileManagement = ({ jarFiles, isLoading, onRefresh, onJarFileAdded, onJarFileDeleted }: JarFileManagementProps) => {
  return (
    <ConnectionDriverSelection 
      jarFiles={jarFiles}
      isLoading={isLoading}
      onRefresh={onRefresh}
      onJarFileAdded={onJarFileAdded}
      onJarFileDeleted={onJarFileDeleted}
    />
  );
};