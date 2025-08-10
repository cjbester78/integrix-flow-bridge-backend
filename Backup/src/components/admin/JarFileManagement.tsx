import { ConnectionDriverSelection } from './ConnectionDriverSelection';
import { JarFile } from '@/types/admin';

interface JarFileManagementProps {
  jarFiles: JarFile[];
  onJarFileAdded: (jarFile: JarFile) => void;
  onJarFileDeleted: (id: string) => void;
}

export const JarFileManagement = ({ jarFiles, onJarFileAdded, onJarFileDeleted }: JarFileManagementProps) => {
  return (
    <ConnectionDriverSelection 
      jarFiles={jarFiles}
      onJarFileAdded={onJarFileAdded}
      onJarFileDeleted={onJarFileDeleted}
    />
  );
};