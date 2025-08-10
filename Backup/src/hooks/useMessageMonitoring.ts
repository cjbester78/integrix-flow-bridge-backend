import { useState, useEffect, useCallback } from 'react';
import { messageService, Message, MessageStats, MessageFilters } from '@/services/messageService';
import { useToast } from '@/hooks/use-toast';

export const useMessageMonitoring = (customerId?: string) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [stats, setStats] = useState<MessageStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [connected, setConnected] = useState(false);
  const { toast } = useToast();

  // Load initial data
  const loadMessages = useCallback(async (filters?: MessageFilters) => {
    setLoading(true);
    try {
      const response = customerId 
        ? await messageService.getCustomerMessages(customerId, filters)
        : await messageService.getMessages(filters);
        
      if (response.success && response.data) {
        setMessages(response.data.messages);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load messages",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  }, [customerId, toast]);

  const loadStats = useCallback(async (filters?: Omit<MessageFilters, 'limit' | 'offset'>) => {
    try {
      const response = await messageService.getMessageStats(filters);
      if (response.success && response.data) {
        setStats(response.data);
      }
    } catch (error) {
      console.error('Failed to load message stats:', error);
    }
  }, []);

  // Real-time updates
  useEffect(() => {
    // Connect WebSocket
    messageService.connectWebSocket(customerId);
    setConnected(true);

    // Subscribe to message updates
    const unsubscribeMessages = messageService.onMessageUpdate((newMessage) => {
      setMessages(prev => {
        const existingIndex = prev.findIndex(m => m.id === newMessage.id);
        if (existingIndex >= 0) {
          // Update existing message
          const updated = [...prev];
          updated[existingIndex] = newMessage;
          return updated;
        } else {
          // Add new message at the beginning
          return [newMessage, ...prev];
        }
      });

      // Show toast for failed messages
      if (newMessage.status === 'failed') {
        toast({
          title: "Message Failed",
          description: `Message ${newMessage.id} failed processing`,
          variant: "destructive",
        });
      }
    });

    // Subscribe to stats updates
    const unsubscribeStats = messageService.onStatsUpdate((newStats) => {
      setStats(newStats);
    });

    // Load initial data
    loadMessages();
    loadStats();

    // Cleanup on unmount
    return () => {
      unsubscribeMessages();
      unsubscribeStats();
      messageService.disconnectWebSocket();
      setConnected(false);
    };
  }, [customerId, loadMessages, loadStats, toast]);

  const reprocessMessage = useCallback(async (messageId: string) => {
    try {
      const response = await messageService.reprocessMessage(messageId);
      if (response.success) {
        toast({
          title: "Success",
          description: "Message queued for reprocessing",
        });
        // The WebSocket will handle the real-time update
      } else {
        throw new Error(response.error || 'Failed to reprocess message');
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to reprocess message",
        variant: "destructive",
      });
    }
  }, [toast]);

  const refreshData = useCallback((filters?: MessageFilters) => {
    loadMessages(filters);
    loadStats(filters);
  }, [loadMessages, loadStats]);

  const subscribeToMessageType = useCallback((messageType: string) => {
    messageService.sendCommand('subscribe', { messageType });
  }, []);

  const unsubscribeFromMessageType = useCallback((messageType: string) => {
    messageService.sendCommand('unsubscribe', { messageType });
  }, []);

  return {
    messages,
    stats,
    loading,
    connected,
    loadMessages,
    loadStats,
    reprocessMessage,
    refreshData,
    subscribeToMessageType,
    unsubscribeFromMessageType,
  };
};