import { Card, CardContent } from '@/components/ui/card';
import { EmptyState } from '@/components/ui/empty-state';
import { ChannelCard } from './ChannelCard';
import { Channel } from '@/services/channelService';
import { Radio } from 'lucide-react';

interface ChannelListProps {
  channels: Channel[];
}

export const ChannelList = ({ channels }: ChannelListProps) => {
  return (
    <div className="space-y-4">
      {channels.length > 0 ? (
        channels.map((channel) => (
          <ChannelCard key={channel.id} channel={channel} />
        ))
      ) : (
        <EmptyState
          icon={<Radio className="h-12 w-12" />}
          title="No channels found"
          description="Channels will appear here once your integration flows are configured and deployed."
        />
      )}
    </div>
  );
};