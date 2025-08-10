import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { 
  Play,
  Square,
  Circle,
  Diamond,
  Hexagon,
  ArrowRightLeft,
  Database,
  Globe,
  Mail,
  FileText,
  FolderOpen,
  Code,
  Zap,
  Shield,
  Route,
  GitBranch,
  Filter,
  Timer,
  RefreshCw,
  AlertTriangle,
  Lock,
  Key,
  FileCheck,
  Workflow,
  Settings,
  Archive,
  Search,
  MessageSquare,
  Users,
  Target
} from 'lucide-react';

interface OrchestrationNodePaletteProps {
  onAddNode: (type: string, category: string) => void;
}

export const OrchestrationNodePalette: React.FC<OrchestrationNodePaletteProps> = ({ onAddNode }) => {
  const nodeCategories = [
    {
      category: 'BPMN Events',
      icon: Circle,
      nodes: [
        { type: 'start-process', label: 'Start Process', icon: Play },
        { type: 'end-process', label: 'End Process', icon: Square },
        { type: 'start-event', label: 'Start Event', icon: Circle },
        { type: 'end-event', label: 'End Event', icon: Circle },
        { type: 'intermediate-event', label: 'Intermediate Event', icon: Circle },
        { type: 'timer-event', label: 'Timer Event', icon: Timer },
        { type: 'message-event', label: 'Message Event', icon: MessageSquare },
        { type: 'error-event', label: 'Error Event', icon: AlertTriangle },
      ]
    },
    {
      category: 'BPMN Activities',
      icon: Settings,
      nodes: [
        { type: 'task', label: 'Task', icon: Settings },
        { type: 'service-task', label: 'Service Task', icon: Zap },
        { type: 'user-task', label: 'User Task', icon: Users },
        { type: 'script-task', label: 'Script Task', icon: Code },
        { type: 'manual-task', label: 'Manual Task', icon: Target },
        { type: 'business-rule-task', label: 'Business Rule Task', icon: FileCheck },
        { type: 'receive-task', label: 'Receive Task', icon: MessageSquare },
        { type: 'send-task', label: 'Send Task', icon: MessageSquare },
      ]
    },
    {
      category: 'BPMN Gateways',
      icon: Diamond,
      nodes: [
        { type: 'exclusive-gateway', label: 'Exclusive Gateway', icon: Diamond },
        { type: 'parallel-gateway', label: 'Parallel Gateway', icon: Diamond },
        { type: 'inclusive-gateway', label: 'Inclusive Gateway', icon: Diamond },
        { type: 'event-gateway', label: 'Event Gateway', icon: Diamond },
        { type: 'complex-gateway', label: 'Complex Gateway', icon: Diamond },
      ]
    },
    {
      category: 'Mapping & Transformation',
      icon: ArrowRightLeft,
      nodes: [
        { type: 'field-mapping', label: 'Field Mapping', icon: ArrowRightLeft },
        { type: 'data-transformation', label: 'Data Transformation', icon: RefreshCw },
        { type: 'format-conversion', label: 'Format Conversion', icon: ArrowRightLeft },
        { type: 'value-mapping', label: 'Value Mapping', icon: Search },
        { type: 'structure-mapping', label: 'Structure Mapping', icon: Workflow },
        { type: 'conditional-mapping', label: 'Conditional Mapping', icon: GitBranch },
        { type: 'aggregation', label: 'Aggregation', icon: Archive },
        { type: 'split-mapping', label: 'Split Mapping', icon: GitBranch },
      ]
    },
    {
      category: 'File Transformations',
      icon: FileText,
      nodes: [
        { type: 'csv-to-xml', label: 'CSV to XML', icon: FileText },
        { type: 'json-to-xml', label: 'JSON to XML', icon: FileText },
        { type: 'xml-to-json', label: 'XML to JSON', icon: FileText },
        { type: 'excel-to-csv', label: 'Excel to CSV', icon: FileText },
        { type: 'pdf-extractor', label: 'PDF Extractor', icon: FileText },
        { type: 'file-splitter', label: 'File Splitter', icon: GitBranch },
        { type: 'file-merger', label: 'File Merger', icon: Archive },
        { type: 'file-validator', label: 'File Validator', icon: Shield },
      ]
    },
    {
      category: 'Scripts & Functions',
      icon: Code,
      nodes: [
        { type: 'javascript-script', label: 'JavaScript Script', icon: Code },
        { type: 'groovy-script', label: 'Groovy Script', icon: Code },
        { type: 'xslt-transformation', label: 'XSLT Transformation', icon: Code },
        { type: 'custom-function', label: 'Custom Function', icon: Zap },
        { type: 'lookup-function', label: 'Lookup Function', icon: Search },
        { type: 'calculation-function', label: 'Calculation Function', icon: Target },
        { type: 'validation-function', label: 'Validation Function', icon: Shield },
        { type: 'enrichment-function', label: 'Enrichment Function', icon: Zap },
      ]
    },
    {
      category: 'Flow Routing',
      icon: Route,
      nodes: [
        { type: 'content-router', label: 'Content Router', icon: Route },
        { type: 'recipient-list', label: 'Recipient List', icon: Users },
        { type: 'message-filter', label: 'Message Filter', icon: Filter },
        { type: 'dynamic-router', label: 'Dynamic Router', icon: Route },
        { type: 'load-balancer', label: 'Load Balancer', icon: GitBranch },
        { type: 'failover-router', label: 'Failover Router', icon: RefreshCw },
        { type: 'multicast', label: 'Multicast', icon: GitBranch },
        { type: 'wire-tap', label: 'Wire Tap', icon: Search },
      ]
    },
    {
      category: 'Security',
      icon: Shield,
      nodes: [
        { type: 'encryption', label: 'Encryption', icon: Lock },
        { type: 'decryption', label: 'Decryption', icon: Key },
        { type: 'digital-signature', label: 'Digital Signature', icon: FileCheck },
        { type: 'authentication', label: 'Authentication', icon: Users },
        { type: 'authorization', label: 'Authorization', icon: Shield },
        { type: 'certificate-validation', label: 'Certificate Validation', icon: FileCheck },
        { type: 'secure-transport', label: 'Secure Transport', icon: Lock },
        { type: 'access-control', label: 'Access Control', icon: Key },
      ]
    },
    {
      category: 'Adapters & Connectors',
      icon: Globe,
      nodes: [
        { type: 'http-adapter', label: 'HTTP Adapter', icon: Globe },
        { type: 'ftp-adapter', label: 'FTP/SFTP Adapter', icon: FolderOpen },
        { type: 'database-adapter', label: 'Database Adapter', icon: Database },
        { type: 'mail-adapter', label: 'Mail Adapter', icon: Mail },
        { type: 'file-adapter', label: 'File Adapter', icon: FileText },
        { type: 'jms-adapter', label: 'JMS Adapter', icon: MessageSquare },
        { type: 'soap-adapter', label: 'SOAP Adapter', icon: Globe },
        { type: 'rest-adapter', label: 'REST Adapter', icon: Globe },
      ]
    }
  ];

  return (
    <Card className="w-64 h-full">
      <CardHeader className="pb-3">
        <CardTitle className="text-sm">Process Steps</CardTitle>
      </CardHeader>
      
      <CardContent className="space-y-4 overflow-y-auto max-h-[calc(100vh-200px)]">
        {nodeCategories.map((category) => (
          <div key={category.category} className="space-y-2">
            <div className="flex items-center gap-2 text-xs font-medium text-muted-foreground">
              <category.icon className="h-3 w-3" />
              {category.category}
            </div>
            
            <div className="grid gap-1">
              {category.nodes.map((node) => (
                <Button
                  key={node.type}
                  variant="ghost"
                  size="sm"
                  className="justify-start h-8 text-xs"
                  onClick={() => onAddNode(node.type, category.category)}
                >
                  <node.icon className="h-3 w-3 mr-2" />
                  {node.label}
                </Button>
              ))}
            </div>
            
            <Separator />
          </div>
        ))}
      </CardContent>
    </Card>
  );
};