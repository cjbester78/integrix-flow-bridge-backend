// @ts-nocheck
import { useEffect } from 'react';

export const useMetaDescription = (description: string) => {
  useEffect(() => {
    const metaDescription = document.querySelector('meta[name="description"]');
    const ogDescription = document.querySelector('meta[property="og:description"]');
    
    if (metaDescription) {
      const originalDescription = metaDescription.getAttribute('content') || '';
      metaDescription.setAttribute('content', description);
      
      // Also update Open Graph description
      if (ogDescription) {
        ogDescription.setAttribute('content', description);
      }
      
      return () => {
        metaDescription.setAttribute('content', originalDescription);
        if (ogDescription && originalDescription) {
          ogDescription.setAttribute('content', originalDescription);
        }
      };
    }
  }, [description]);
};