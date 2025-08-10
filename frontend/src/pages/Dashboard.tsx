import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { PageContainer } from '@/components/ui/page-container';
import { PageHeader } from '@/components/common/PageHeader';
import { BusinessComponent } from '@/types/businessComponent';
import { dashboardService, DashboardMetric, RecentMessage, AdapterStatus } from '@/services/dashboardService';
import { businessComponentService } from '@/services/businessComponentService';
import { 
  Activity, 
  MessageSquare, 
  CheckCircle, 
  XCircle, 
  Clock,
  TrendingUp,
  Server,
  Zap,
  Users,
  Loader2,
  LayoutDashboard
} from 'lucide-react';
import { useDocumentTitle } from '@/hooks/useDocumentTitle';
import { useMetaDescription } from '@/hooks/useMetaDescription';
import { ErrorAlert } from '@/components/ui/error-alert';

// Icon mapping for dynamic icon rendering
const iconMap: Record<string, any> = {
  Activity,
  MessageSquare,
  CheckCircle,
  XCircle,
  Clock,
  TrendingUp,
  Server,
  Zap,
  Users
};

export const Dashboard = () => {
  useDocumentTitle('Dashboard');
  useMetaDescription('Monitor your integration flows, adapter status, and system performance in real-time with the Integrix Flow Bridge dashboard.');
  console.log('Dashboard component loading...');
  const [selectedBusinessComponent, setSelectedBusinessComponent] = useState<BusinessComponent | null>(null);
  const [metrics, setMetrics] = useState<DashboardMetric[]>([]);
  const [recentMessages, setRecentMessages] = useState<RecentMessage[]>([]);
  const [adapterStatuses, setAdapterStatuses] = useState<AdapterStatus[]>([]);
  const [loadingMetrics, setLoadingMetrics] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(true);
  const [loadingAdapters, setLoadingAdapters] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [businessComponents, setBusinessComponents] = useState<BusinessComponent[]>([]);
  const [loadingComponents, setLoadingComponents] = useState(true);
  
  // Load business components without using the hook initially
  useEffect(() => {
    const loadBusinessComponents = async () => {
      try {
        console.log('Dashboard - loading business components...');
        const response = await businessComponentService.getAllBusinessComponents();
        if (response.success && response.data) {
          setBusinessComponents(response.data);
        }
      } catch (error) {
        console.error('Error loading business components:', error);
      } finally {
        setLoadingComponents(false);
      }
    };
    
    loadBusinessComponents();
  }, []);
  
  console.log('Dashboard - business components:', businessComponents, 'loading:', loadingComponents);

  // Fetch dashboard data
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setError(null);
        const businessComponentId = selectedBusinessComponent?.id;

        // Fetch metrics
        setLoadingMetrics(true);
        const metricsResponse = await dashboardService.getDashboardMetrics(businessComponentId);
        console.log('Metrics response:', metricsResponse);
        if (metricsResponse.success && metricsResponse.data && Array.isArray(metricsResponse.data)) {
          setMetrics(metricsResponse.data);
        } else {
          console.error('Failed to fetch metrics:', metricsResponse.error);
          // Set default metrics to prevent map error
          setMetrics([]);
        }
        setLoadingMetrics(false);

        // Fetch recent messages
        setLoadingMessages(true);
        const messagesResponse = await dashboardService.getRecentMessages(businessComponentId);
        if (messagesResponse.success && messagesResponse.data) {
          setRecentMessages(messagesResponse.data);
        } else {
          console.error('Failed to fetch messages:', messagesResponse.error);
          // Set default empty array to prevent map error
          setRecentMessages([]);
        }
        setLoadingMessages(false);

        // Fetch adapter statuses
        setLoadingAdapters(true);
        const adaptersResponse = await dashboardService.getAdapterStatuses(businessComponentId);
        if (adaptersResponse.success && adaptersResponse.data) {
          setAdapterStatuses(adaptersResponse.data);
        } else {
          console.error('Failed to fetch adapters:', adaptersResponse.error);
          // Set default empty array to prevent map error
          setAdapterStatuses([]);
        }
        setLoadingAdapters(false);

      } catch (err) {
        console.error('Dashboard data fetch error:', err);
        setError('Failed to load dashboard data');
        // Ensure arrays are initialized even on error
        setMetrics([]);
        setRecentMessages([]);
        setAdapterStatuses([]);
        setLoadingMetrics(false);
        setLoadingMessages(false);
        setLoadingAdapters(false);
      }
    };

    fetchDashboardData();
    
    // Refresh data every 30 seconds
    const interval = setInterval(fetchDashboardData, 30000);
    return () => clearInterval(interval);
  }, [selectedBusinessComponent]);

  console.log('Dashboard render - about to return JSX');
  
  return (
    <PageContainer>
      <PageHeader 
        title="Integration Dashboard"
        description="Monitor your integration flows and system health"
        icon={<LayoutDashboard />}
      />

      {/* Business Component Filter */}
      <div className="flex items-center gap-4">
        <Label htmlFor="business-component">Business Component:</Label>
        <Select
          value={selectedBusinessComponent?.id || 'all'}
          onValueChange={(value) => {
            if (value === 'all') {
              setSelectedBusinessComponent(null);
            } else {
              const component = businessComponents.find(bc => bc.id === value);
              setSelectedBusinessComponent(component || null);
            }
          }}
        >
          <SelectTrigger id="business-component" className="w-[280px]">
            <SelectValue placeholder="Select a business component" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Components</SelectItem>
            {businessComponents.map((bc) => (
              <SelectItem key={bc.id} value={bc.id}>
                {bc.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Error Display */}
      {error && <ErrorAlert error={error} />}

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {loadingMetrics ? (
          <div className="col-span-full flex justify-center p-8">
            <Loader2 className="h-8 w-8 animate-spin" />
          </div>
        ) : metrics && Array.isArray(metrics) ? (
          metrics.map((stat, index) => {
            const Icon = iconMap[stat.icon] || Activity;
            const isPositive = stat.change.startsWith('+') || stat.change.startsWith('-') && stat.change.includes('ms');
            
            return (
              <Card key={index}>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">{stat.title}</CardTitle>
                  <Icon className={`h-4 w-4 ${stat.color}`} />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{stat.value}</div>
                  <p className={`text-xs ${isPositive ? 'text-success' : 'text-destructive'}`}>
                    {stat.change} from last period
                  </p>
                </CardContent>
              </Card>
            );
          })
        ) : (
          <div className="col-span-full">
            <p className="text-muted-foreground text-center py-8">No metrics available</p>
          </div>
        )}
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Messages */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Messages</CardTitle>
            <CardDescription>Latest integration messages</CardDescription>
          </CardHeader>
          <CardContent>
            {loadingMessages ? (
              <div className="flex justify-center p-8">
                <Loader2 className="h-6 w-6 animate-spin" />
              </div>
            ) : !recentMessages || !Array.isArray(recentMessages) || recentMessages.length === 0 ? (
              <p className="text-muted-foreground text-center py-8">No recent messages</p>
            ) : (
              <div className="space-y-4">
                {recentMessages.map((message) => (
                  <div key={message.id} className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <Badge
                        variant={
                          message.status === 'success' ? 'success' :
                          message.status === 'failed' ? 'destructive' : 
                          'secondary'
                        }
                      >
                        {message.status}
                      </Badge>
                      <div>
                        <p className="text-sm font-medium">
                          {message.source} → {message.target}
                        </p>
                        <p className="text-xs text-muted-foreground">{message.time}</p>
                      </div>
                    </div>
                    {message.status === 'success' && <CheckCircle className="h-4 w-4 text-success" />}
                    {message.status === 'failed' && <XCircle className="h-4 w-4 text-destructive" />}
                    {message.status === 'processing' && <Clock className="h-4 w-4 text-warning animate-pulse" />}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Adapter Monitoring */}
        <Card>
          <CardHeader>
            <CardTitle>Adapter Monitoring</CardTitle>
            <CardDescription>Active communication adapters</CardDescription>
          </CardHeader>
          <CardContent>
            {loadingAdapters ? (
              <div className="flex justify-center p-8">
                <Loader2 className="h-6 w-6 animate-spin" />
              </div>
            ) : !adapterStatuses || !Array.isArray(adapterStatuses) || adapterStatuses.length === 0 ? (
              <p className="text-muted-foreground text-center py-8">No active adapters</p>
            ) : (
              <div className="space-y-4">
                {adapterStatuses.map((adapter, index) => (
                  <div key={index}>
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <Server className="h-4 w-4 text-muted-foreground" />
                        <span className="text-sm font-medium">{adapter.name}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge variant="outline">{adapter.type}</Badge>
                        <Badge variant="secondary">{adapter.mode}</Badge>
                        <Badge 
                          variant={
                            adapter.status === 'running' ? 'success' : 
                            adapter.status === 'stopped' ? 'secondary' : 
                            'destructive'
                          }
                        >
                          {adapter.status}
                        </Badge>
                      </div>
                    </div>
                    <Progress value={adapter.load} className="h-2" />
                    <div className="flex justify-between text-xs text-muted-foreground mt-1">
                      <span>Load: {adapter.load}%</span>
                      {adapter.messagesProcessed !== undefined && (
                        <span>Messages: {adapter.messagesProcessed.toLocaleString()}</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </PageContainer>
  );
};