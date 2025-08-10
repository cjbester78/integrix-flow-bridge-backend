import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { FlowSchedule, FlowWebhook, FlowDefinition } from '@/types/flow';
import { toast } from '@/hooks/use-toast';
import { 
  Clock, 
  Webhook, 
  Play, 
  Pause, 
  Calendar,
  Globe,
  Settings,
  Plus,
  Trash2,
  CheckCircle,
  XCircle
} from 'lucide-react';

interface FlowSchedulerProps {
  flowDefinition: FlowDefinition;
}

export const FlowScheduler: React.FC<FlowSchedulerProps> = ({ flowDefinition }) => {
  const [schedules, setSchedules] = useState<FlowSchedule[]>([]);
  const [webhooks, setWebhooks] = useState<FlowWebhook[]>([]);
  const [newSchedule, setNewSchedule] = useState<Partial<FlowSchedule>>({
    flowId: flowDefinition.id,
    cronExpression: '0 9 * * 1-5', // Default: 9 AM weekdays
    timezone: 'UTC',
    enabled: true
  });
  const [newWebhook, setNewWebhook] = useState<Partial<FlowWebhook>>({
    flowId: flowDefinition.id,
    method: 'POST',
    enabled: true,
    authentication: { type: 'none' }
  });

  useEffect(() => {
    loadSchedulesAndWebhooks();
  }, [flowDefinition.id]);

  const loadSchedulesAndWebhooks = async () => {
    // Mock data - in real implementation, fetch from API
    setSchedules([
      {
        id: 'schedule_1',
        flowId: flowDefinition.id,
        cronExpression: '0 9 * * 1-5',
        timezone: 'UTC',
        enabled: true,
        nextRun: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
        lastRun: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString()
      }
    ]);

    setWebhooks([
      {
        id: 'webhook_1',
        flowId: flowDefinition.id,
        url: `https://api.example.com/flows/${flowDefinition.id}/trigger`,
        method: 'POST',
        enabled: true,
        authentication: { type: 'bearer', credentials: { token: 'hidden' } }
      }
    ]);
  };

  const createSchedule = async () => {
    if (!newSchedule.cronExpression) {
      toast({
        title: "Validation Error",
        description: "Please enter a cron expression",
        variant: "destructive"
      });
      return;
    }

    const schedule: FlowSchedule = {
      id: `schedule_${Date.now()}`,
      flowId: flowDefinition.id,
      cronExpression: newSchedule.cronExpression!,
      timezone: newSchedule.timezone || 'UTC',
      enabled: newSchedule.enabled || true,
      nextRun: calculateNextRun(newSchedule.cronExpression!),
      parameters: newSchedule.parameters
    };

    setSchedules(prev => [...prev, schedule]);
    setNewSchedule({
      flowId: flowDefinition.id,
      cronExpression: '',
      timezone: 'UTC',
      enabled: true
    });

    toast({
      title: "Schedule Created",
      description: "Flow schedule has been created successfully",
      variant: "default"
    });
  };

  const createWebhook = async () => {
    if (!newWebhook.url) {
      toast({
        title: "Validation Error",
        description: "Please enter a webhook URL",
        variant: "destructive"
      });
      return;
    }

    const webhook: FlowWebhook = {
      id: `webhook_${Date.now()}`,
      flowId: flowDefinition.id,
      url: newWebhook.url!,
      method: newWebhook.method || 'POST',
      headers: newWebhook.headers,
      authentication: newWebhook.authentication,
      enabled: newWebhook.enabled || true
    };

    setWebhooks(prev => [...prev, webhook]);
    setNewWebhook({
      flowId: flowDefinition.id,
      method: 'POST',
      enabled: true,
      authentication: { type: 'none' }
    });

    toast({
      title: "Webhook Created",
      description: "Flow webhook has been created successfully",
      variant: "default"
    });
  };

  const toggleSchedule = (scheduleId: string) => {
    setSchedules(prev =>
      prev.map(schedule =>
        schedule.id === scheduleId
          ? { ...schedule, enabled: !schedule.enabled }
          : schedule
      )
    );
  };

  const toggleWebhook = (webhookId: string) => {
    setWebhooks(prev =>
      prev.map(webhook =>
        webhook.id === webhookId
          ? { ...webhook, enabled: !webhook.enabled }
          : webhook
      )
    );
  };

  const deleteSchedule = (scheduleId: string) => {
    setSchedules(prev => prev.filter(schedule => schedule.id !== scheduleId));
    toast({
      title: "Schedule Deleted",
      description: "Flow schedule has been deleted",
      variant: "default"
    });
  };

  const deleteWebhook = (webhookId: string) => {
    setWebhooks(prev => prev.filter(webhook => webhook.id !== webhookId));
    toast({
      title: "Webhook Deleted",
      description: "Flow webhook has been deleted",
      variant: "default"
    });
  };

  const calculateNextRun = (cronExpression: string): string => {
    // Simple mock calculation - in real implementation, use a cron library
    return new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString();
  };

  const formatCronExpression = (cron: string): string => {
    const cronMap: Record<string, string> = {
      '0 9 * * 1-5': 'Daily at 9 AM (Weekdays)',
      '0 */6 * * *': 'Every 6 hours',
      '0 0 * * 0': 'Weekly on Sunday',
      '0 0 1 * *': 'Monthly on 1st'
    };
    return cronMap[cron] || cron;
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Settings className="h-5 w-5" />
            Flow Automation: {flowDefinition.name}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="schedules" className="w-full">
            <TabsList>
              <TabsTrigger value="schedules" className="flex items-center gap-2">
                <Clock className="h-4 w-4" />
                Schedules
              </TabsTrigger>
              <TabsTrigger value="webhooks" className="flex items-center gap-2">
                <Webhook className="h-4 w-4" />
                Webhooks
              </TabsTrigger>
            </TabsList>

            <TabsContent value="schedules" className="space-y-6">
              {/* Create New Schedule */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Plus className="h-4 w-4" />
                    Create Schedule
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="cron-expression">Cron Expression</Label>
                      <Input
                        id="cron-expression"
                        placeholder="0 9 * * 1-5"
                        value={newSchedule.cronExpression || ''}
                        onChange={(e) => setNewSchedule(prev => ({ 
                          ...prev, 
                          cronExpression: e.target.value 
                        }))}
                      />
                      <p className="text-xs text-muted-foreground">
                        Format: minute hour day month day-of-week
                      </p>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="timezone">Timezone</Label>
                      <Select
                        value={newSchedule.timezone}
                        onValueChange={(value) => setNewSchedule(prev => ({ 
                          ...prev, 
                          timezone: value 
                        }))}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="UTC">UTC</SelectItem>
                          <SelectItem value="America/New_York">Eastern Time</SelectItem>
                          <SelectItem value="America/Los_Angeles">Pacific Time</SelectItem>
                          <SelectItem value="Europe/London">London</SelectItem>
                          <SelectItem value="Asia/Tokyo">Tokyo</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="schedule-enabled"
                      checked={newSchedule.enabled}
                      onCheckedChange={(checked) => setNewSchedule(prev => ({ 
                        ...prev, 
                        enabled: checked 
                      }))}
                    />
                    <Label htmlFor="schedule-enabled">Enable immediately</Label>
                  </div>

                  <Button onClick={createSchedule} className="w-full">
                    <Plus className="h-4 w-4 mr-2" />
                    Create Schedule
                  </Button>
                </CardContent>
              </Card>

              {/* Existing Schedules */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    Active Schedules
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {schedules.length === 0 ? (
                    <Alert>
                      <Clock className="h-4 w-4" />
                      <AlertDescription>No schedules configured for this flow.</AlertDescription>
                    </Alert>
                  ) : (
                    <div className="space-y-4">
                      {schedules.map(schedule => (
                        <Card key={schedule.id}>
                          <CardContent className="pt-4">
                            <div className="flex items-center justify-between">
                              <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                  <Badge variant={schedule.enabled ? 'default' : 'secondary'}>
                                    {schedule.enabled ? 'Active' : 'Disabled'}
                                  </Badge>
                                  <span className="font-medium">
                                    {formatCronExpression(schedule.cronExpression)}
                                  </span>
                                </div>
                                <div className="text-sm text-muted-foreground">
                                  Cron: {schedule.cronExpression} | Timezone: {schedule.timezone}
                                </div>
                                <div className="text-sm text-muted-foreground">
                                  Next run: {schedule.nextRun ? new Date(schedule.nextRun).toLocaleString() : 'Not scheduled'}
                                </div>
                                {schedule.lastRun && (
                                  <div className="text-sm text-muted-foreground">
                                    Last run: {new Date(schedule.lastRun).toLocaleString()}
                                  </div>
                                )}
                              </div>

                              <div className="flex items-center gap-2">
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() => toggleSchedule(schedule.id)}
                                >
                                  {schedule.enabled ? (
                                    <Pause className="h-4 w-4" />
                                  ) : (
                                    <Play className="h-4 w-4" />
                                  )}
                                </Button>
                                <Button
                                  variant="destructive"
                                  size="sm"
                                  onClick={() => deleteSchedule(schedule.id)}
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="webhooks" className="space-y-6">
              {/* Create New Webhook */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Plus className="h-4 w-4" />
                    Create Webhook
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="webhook-url">Webhook URL</Label>
                      <Input
                        id="webhook-url"
                        placeholder="https://api.example.com/webhook"
                        value={newWebhook.url || ''}
                        onChange={(e) => setNewWebhook(prev => ({ 
                          ...prev, 
                          url: e.target.value 
                        }))}
                      />
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="webhook-method">HTTP Method</Label>
                      <Select
                        value={newWebhook.method}
                        onValueChange={(value) => setNewWebhook(prev => ({ 
                          ...prev, 
                          method: value as 'POST' | 'PUT' | 'PATCH'
                        }))}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="POST">POST</SelectItem>
                          <SelectItem value="PUT">PUT</SelectItem>
                          <SelectItem value="PATCH">PATCH</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="auth-type">Authentication</Label>
                    <Select
                      value={newWebhook.authentication?.type}
                      onValueChange={(value) => setNewWebhook(prev => ({ 
                        ...prev, 
                        authentication: { 
                          ...prev.authentication, 
                          type: value as 'none' | 'basic' | 'bearer' | 'api-key' 
                        } 
                      }))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="none">None</SelectItem>
                        <SelectItem value="basic">Basic Auth</SelectItem>
                        <SelectItem value="bearer">Bearer Token</SelectItem>
                        <SelectItem value="api-key">API Key</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="webhook-enabled"
                      checked={newWebhook.enabled}
                      onCheckedChange={(checked) => setNewWebhook(prev => ({ 
                        ...prev, 
                        enabled: checked 
                      }))}
                    />
                    <Label htmlFor="webhook-enabled">Enable immediately</Label>
                  </div>

                  <Button onClick={createWebhook} className="w-full">
                    <Plus className="h-4 w-4 mr-2" />
                    Create Webhook
                  </Button>
                </CardContent>
              </Card>

              {/* Existing Webhooks */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Globe className="h-4 w-4" />
                    Active Webhooks
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {webhooks.length === 0 ? (
                    <Alert>
                      <Webhook className="h-4 w-4" />
                      <AlertDescription>No webhooks configured for this flow.</AlertDescription>
                    </Alert>
                  ) : (
                    <div className="space-y-4">
                      {webhooks.map(webhook => (
                        <Card key={webhook.id}>
                          <CardContent className="pt-4">
                            <div className="flex items-center justify-between">
                              <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                  <Badge variant={webhook.enabled ? 'default' : 'secondary'}>
                                    {webhook.enabled ? 'Active' : 'Disabled'}
                                  </Badge>
                                  <span className="font-medium">{webhook.method}</span>
                                  <code className="text-sm bg-muted px-2 py-1 rounded">
                                    {webhook.url}
                                  </code>
                                </div>
                                <div className="text-sm text-muted-foreground">
                                  Authentication: {webhook.authentication?.type || 'none'}
                                </div>
                                {webhook.headers && (
                                  <div className="text-sm text-muted-foreground">
                                    Custom headers: {Object.keys(webhook.headers).length} defined
                                  </div>
                                )}
                              </div>

                              <div className="flex items-center gap-2">
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() => toggleWebhook(webhook.id)}
                                >
                                  {webhook.enabled ? (
                                    <Pause className="h-4 w-4" />
                                  ) : (
                                    <Play className="h-4 w-4" />
                                  )}
                                </Button>
                                <Button
                                  variant="destructive"
                                  size="sm"
                                  onClick={() => deleteWebhook(webhook.id)}
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
};
