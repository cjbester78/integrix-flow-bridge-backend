import React from 'react';

interface QueryProviderProps {
  children: React.ReactNode;
}

/**
 * Temporary disabled React Query provider
 * Just passes through children without providing React Query
 */
export const QueryProvider: React.FC<QueryProviderProps> = ({ children }) => {
  return <>{children}</>;
};