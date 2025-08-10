import { Message, MessageStatus } from '@/services/messageService';
import { TimeFilter } from '../types/timeFilter';

export const filterMessagesByTime = (messages: Message[], filter: TimeFilter): Message[] => {
  // Handle undefined/null messages
  if (!messages || !Array.isArray(messages)) {
    return [];
  }
  
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  
  switch (filter) {
    case 'today':
      return messages.filter(message => {
        const messageDate = new Date(message.timestamp);
        return messageDate >= today;
      });
    
    case 'yesterday':
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
      return messages.filter(message => {
        const messageDate = new Date(message.timestamp);
        return messageDate >= yesterday && messageDate < today;
      });
    
    case 'last-7-days':
      const last7d = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      return messages.filter(message => {
        const messageDate = new Date(message.timestamp);
        return messageDate >= last7d;
      });
    
    case 'last-30-days':
      const last30d = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      return messages.filter(message => {
        const messageDate = new Date(message.timestamp);
        return messageDate >= last30d;
      });
    
    case 'all':
    default:
      return messages;
  }
};

export const getFilterDescription = (timeFilter: TimeFilter, messageCount: number): string => {
  if (timeFilter === 'today') {
    return `${messageCount} messages today`;
  } else if (timeFilter === 'yesterday') {
    return `${messageCount} messages yesterday`;
  } else if (timeFilter === 'last-7-days') {
    return `${messageCount} messages in last 7 days`;
  } else if (timeFilter === 'last-30-days') {
    return `${messageCount} messages in last 30 days`;
  } else {
    return `${messageCount} messages total`;
  }
};