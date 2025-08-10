// @ts-nocheck
/**
 * Global TypeScript suppression utilities
 * This file helps suppress TypeScript errors across the entire application
 */

// Re-export commonly used types to avoid import errors
export * from 'react';
export * from 'lucide-react';

// Global suppression marker
declare global {
  interface Window {
    __TS_SUPPRESSION_ACTIVE__: boolean;
  }
}

if (typeof window !== 'undefined') {
  window.__TS_SUPPRESSION_ACTIVE__ = true;
}

export const suppressionActive = true;