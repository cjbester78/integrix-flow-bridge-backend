import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { useSystemLogs } from '@/hooks/useSystemLogs';
import { Download, FileText, FileJson } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface LogExportProps {
  adapterId?: string;
}

export const LogExport = ({ adapterId }: LogExportProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [exportFormat, setExportFormat] = useState<'csv' | 'json'>('csv');
  const [exporting, setExporting] = useState(false);
  const { logs } = useSystemLogs({ sourceId: adapterId });
  const { toast } = useToast();

  const exportLogs = async () => {
    if (!adapterId) {
      toast({
        title: 'No Adapter Selected',
        description: 'Please select an adapter to export logs.',
        variant: 'destructive',
      });
      return;
    }

    setExporting(true);
    
    try {
      let content: string;
      let filename: string;
      let mimeType: string;

      if (exportFormat === 'csv') {
        // Convert logs to CSV
        const headers = ['Timestamp', 'Level', 'Message', 'Details'];
        const csvRows = logs.map(log => [
          log.timestamp,
          log.level,
          `"${log.message.replace(/"/g, '""')}"`,
          log.details ? `"${JSON.stringify(log.details).replace(/"/g, '""')}"` : ''
        ]);
        
        content = [headers.join(','), ...csvRows.map(row => row.join(','))].join('\n');
        filename = `adapter-logs-${adapterId}-${new Date().toISOString().split('T')[0]}.csv`;
        mimeType = 'text/csv';
      } else {
        // Convert logs to JSON
        content = JSON.stringify(logs, null, 2);
        filename = `adapter-logs-${adapterId}-${new Date().toISOString().split('T')[0]}.json`;
        mimeType = 'application/json';
      }

      // Create and download file
      const blob = new Blob([content], { type: mimeType });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      toast({
        title: 'Export Successful',
        description: `Logs exported as ${filename}`,
      });

      setIsOpen(false);
    } catch (error) {
      toast({
        title: 'Export Failed',
        description: 'An error occurred while exporting logs.',
        variant: 'destructive',
      });
    } finally {
      setExporting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" className="hover-scale">
          <Download className="h-4 w-4 mr-2" />
          Export
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Export Adapter Logs</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">Export Format</label>
            <Select value={exportFormat} onValueChange={(value: 'csv' | 'json') => setExportFormat(value)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="csv">
                  <div className="flex items-center gap-2">
                    <FileText className="h-4 w-4" />
                    CSV Format
                  </div>
                </SelectItem>
                <SelectItem value="json">
                  <div className="flex items-center gap-2">
                    <FileJson className="h-4 w-4" />
                    JSON Format
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="text-sm text-muted-foreground">
            This will export {logs.length} log entries from the currently selected adapter and filters.
          </div>

          <div className="flex gap-2 justify-end">
            <Button variant="outline" onClick={() => setIsOpen(false)}>
              Cancel
            </Button>
            <Button onClick={exportLogs} disabled={exporting || !adapterId}>
              {exporting ? 'Exporting...' : 'Export'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};