import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { Upload } from 'lucide-react';
import { JarFile } from '@/types/admin';

interface JdbcDriverModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onDriverAdded: (jarFile: JarFile) => void;
}

export const JdbcDriverModal = ({ open, onOpenChange, onDriverAdded }: JdbcDriverModalProps) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [formData, setFormData] = useState({
    databaseType: '',
    name: '',
    version: ''
  });

  const { toast } = useToast();

  const databaseTypes = [
    { value: 'mariadb', label: 'MariaDB' },
    { value: 'oracle', label: 'Oracle' },
    { value: 'db2', label: 'DB2' },
    { value: 'sqlserver', label: 'Microsoft SQL Server' },
    { value: 'mysql', label: 'MySQL' },
    { value: 'postgresql', label: 'PostgreSQL' }
  ];

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      if (!file.name.endsWith('.jar')) {
        toast({
          title: "Invalid File Type",
          description: "Please select a valid JDBC driver file (.jar).",
          variant: "destructive"
        });
        return;
      }
      setSelectedFile(file);
      
      // Auto-populate name from filename
      const fileName = file.name.replace('.jar', '');
      setFormData(prev => ({
        ...prev,
        name: fileName,
        version: prev.version || '1.0.0'
      }));
    }
  };

  const handleUpload = async () => {
    if (!selectedFile || !formData.databaseType) {
      toast({
        title: "Missing Information",
        description: "Please select a database type and driver file.",
        variant: "destructive"
      });
      return;
    }

    setUploading(true);

    try {
      // Simulate upload
      await new Promise(resolve => setTimeout(resolve, 2000));

      const newJarFile: JarFile = {
        id: Date.now().toString(),
        name: formData.name || selectedFile.name.replace('.jar', ''),
        version: formData.version || '1.0.0',
        description: `JDBC driver for ${databaseTypes.find(db => db.value === formData.databaseType)?.label}`,
        file_name: selectedFile.name,
        size_bytes: selectedFile.size,
        upload_date: new Date().toISOString().split('T')[0],
        driver_type: 'JDBC',
        is_active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      };

      onDriverAdded(newJarFile);
      
      // Reset form
      setSelectedFile(null);
      setFormData({ databaseType: '', name: '', version: '' });
      onOpenChange(false);
      
      toast({
        title: "Upload Successful",
        description: `JDBC driver for ${databaseTypes.find(db => db.value === formData.databaseType)?.label} has been uploaded.`
      });

    } catch (error) {
      toast({
        title: "Upload Failed",
        description: "Failed to upload JDBC driver. Please try again.",
        variant: "destructive"
      });
    } finally {
      setUploading(false);
    }
  };

  const resetForm = () => {
    setSelectedFile(null);
    setFormData({ databaseType: '', name: '', version: '' });
  };

  return (
    <Dialog open={open} onOpenChange={(newOpen) => {
      onOpenChange(newOpen);
      if (!newOpen) resetForm();
    }}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Add JDBC Driver</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="database-type">Database Type *</Label>
            <Select value={formData.databaseType} onValueChange={(value) => 
              setFormData(prev => ({ ...prev, databaseType: value }))
            }>
              <SelectTrigger>
                <SelectValue placeholder="Select database type" />
              </SelectTrigger>
              <SelectContent>
                {databaseTypes.map((db) => (
                  <SelectItem key={db.value} value={db.value}>
                    {db.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="jdbc-file">JDBC Driver File *</Label>
            <div className="flex items-center gap-2">
              <Input
                id="jdbc-file"
                type="file"
                accept=".jar"
                onChange={handleFileSelect}
                className="cursor-pointer"
              />
              <Button variant="outline" size="sm" disabled>
                Browse...
              </Button>
            </div>
            {selectedFile && (
              <p className="text-sm text-muted-foreground">
                Selected: {selectedFile.name} ({Math.round(selectedFile.size / 1024)} KB)
              </p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="driver-name">Driver Name</Label>
              <Input
                id="driver-name"
                placeholder="Auto-generated"
                value={formData.name}
                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="driver-version">Version</Label>
              <Input
                id="driver-version"
                placeholder="1.0.0"
                value={formData.version}
                onChange={(e) => setFormData(prev => ({ ...prev, version: e.target.value }))}
              />
            </div>
          </div>
        </div>

        <div className="flex gap-2 justify-end">
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button 
            onClick={handleUpload} 
            disabled={uploading || !selectedFile || !formData.databaseType}
            className="bg-gradient-primary hover:opacity-90"
          >
            <Upload className="h-4 w-4 mr-2" />
            {uploading ? 'Uploading...' : 'Upload Driver'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};