import { useState } from 'react';
import { Message } from '@/services/messageService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { EmptyState } from '@/components/ui/empty-state';
import { FileText, Database } from 'lucide-react';
import { TimeFilter } from './types/timeFilter';
import { filterMessagesByTime, getFilterDescription } from './utils/timeFilters';
import { TimeFilterSelect } from './components/TimeFilterSelect';
import { MessageCard } from './components/MessageCard';

interface MessageListProps {
  messages: Message[];
  isBusinessComponentSelected: boolean;
  statusFilter?: string | null;
  loading?: boolean;
}

export const MessageList = ({ messages, isBusinessComponentSelected, statusFilter, loading = false }: MessageListProps) => {
  const [timeFilter, setTimeFilter] = useState<TimeFilter>('today');

  // Apply filters with safe messages array
  let filteredMessages = filterMessagesByTime(messages || [], timeFilter);
  
  if (statusFilter) {
    filteredMessages = filteredMessages.filter(msg => msg.status === statusFilter);
  }

  // Sort by timestamp (newest first)
  filteredMessages.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

  return (
    <div className="space-y-4">
      {/* Filter Controls */}
      <Card className="bg-gradient-secondary border-border/50">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg font-semibold flex items-center gap-2">
              <FileText className="h-5 w-5" />
              Message History
            </CardTitle>
            <TimeFilterSelect value={timeFilter} onValueChange={setTimeFilter} />
          </div>
          <p className="text-sm text-muted-foreground">
            {getFilterDescription(timeFilter, filteredMessages.length)}
          </p>
        </CardHeader>
      </Card>

      {/* Message List */}
      <div className="space-y-3">
        {loading ? (
          // Loading skeleton
          Array.from({ length: 5 }).map((_, i) => (
            <Card key={i} className="bg-gradient-secondary border-border/50">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <Skeleton className="h-6 w-6 rounded-full" />
                    <div>
                      <Skeleton className="h-4 w-32 mb-2" />
                      <Skeleton className="h-3 w-48" />
                    </div>
                  </div>
                  <div>
                    <Skeleton className="h-3 w-24 mb-1" />
                    <Skeleton className="h-3 w-16" />
                  </div>
                </div>
              </CardHeader>
            </Card>
          ))
        ) : filteredMessages.length === 0 ? (
          <EmptyState
            icon={<Database className="h-12 w-12" />}
            title="No Messages Found"
            description={statusFilter 
              ? `No ${statusFilter} messages found for the selected time period.`
              : 'No messages found for the selected time period.'
            }
          />
        ) : (
          filteredMessages.map((message) => (
            <MessageCard key={message.id} message={message} />
          ))
        )}
      </div>
    </div>
  );
};