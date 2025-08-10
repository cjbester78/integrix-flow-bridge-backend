import { QueryClient } from '@tanstack/react-query';

// Create a minimal query client with no custom configuration
export const queryClient = new QueryClient();

// Export queryKeys from the original file
export { queryKeys, invalidateRelatedQueries } from './query-client';