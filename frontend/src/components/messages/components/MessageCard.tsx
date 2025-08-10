import { useState } from 'react';
import { Message } from '@/services/messageService';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ChevronDown, ChevronRight, FileText } from 'lucide-react';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { getStatusIcon, getLogLevelIcon } from './MessageIcons';

interface MessageCardProps {
  message: Message;
}

export const MessageCard = ({ message }: MessageCardProps) => {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <Card className="bg-gradient-secondary border-border/50 hover-scale">
      <Collapsible open={isExpanded} onOpenChange={setIsExpanded}>
        <CollapsibleTrigger asChild>
          <Button variant="ghost" className="w-full p-0 h-auto hover:bg-transparent">
            <CardHeader className="w-full">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  {getStatusIcon(message.status)}
                  <div className="text-left">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-foreground">{message.id}</span>
                      <Badge variant="outline" className="text-xs">
                        {message.type}
                      </Badge>
                    </div>
                    <div className="text-sm text-muted-foreground">
                      {message.source} → {message.target}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-3 text-right">
                  <div className="text-sm">
                    <div className="text-muted-foreground">{message.timestamp}</div>
                    <div className="text-xs text-muted-foreground">
                      {message.processingTime} • {message.size}
                    </div>
                  </div>
                  {isExpanded ? (
                    <ChevronDown className="h-4 w-4 text-muted-foreground" />
                  ) : (
                    <ChevronRight className="h-4 w-4 text-muted-foreground" />
                  )}
                </div>
              </div>
            </CardHeader>
          </Button>
        </CollapsibleTrigger>
        
        <CollapsibleContent>
          <CardContent className="pt-0">
            <div className="border-t border-border/50 pt-4">
              <h4 className="text-sm font-medium text-foreground mb-3 flex items-center gap-2">
                <FileText className="h-4 w-4" />
                Processing Logs
              </h4>
              <div className="space-y-2 max-h-64 overflow-y-auto">
                {message.logs.map((log, index) => (
                  <div key={index} className="flex items-start gap-3 text-xs">
                    {getLogLevelIcon(log.level)}
                    <span className="text-muted-foreground min-w-[120px] font-mono">
                      {log.timestamp.split(' ')[1]}
                    </span>
                    <span className="text-foreground">{log.message}</span>
                  </div>
                ))}
              </div>
            </div>
          </CardContent>
        </CollapsibleContent>
      </Collapsible>
    </Card>
  );
};