// @ts-nocheck
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * Theme type
 */
export type Theme = 'light' | 'dark' | 'system';

/**
 * Sidebar state
 */
export type SidebarState = 'expanded' | 'collapsed' | 'hidden';

/**
 * UI preferences interface
 */
interface UIPreferences {
  theme: Theme;
  sidebarState: SidebarState;
  compactMode: boolean;
  showLineNumbers: boolean;
  autoSave: boolean;
  animationsEnabled: boolean;
}

/**
 * UI state interface
 */
interface UIState extends UIPreferences {
  // Modal/dialog state
  activeModal: string | null;
  modalData: any;
  
  // Loading states
  globalLoading: boolean;
  loadingMessage: string | null;
  
  // Breadcrumbs
  breadcrumbs: Array<{ label: string; path?: string }>;
  
  // Actions
  setTheme: (theme: Theme) => void;
  setSidebarState: (state: SidebarState) => void;
  toggleSidebar: () => void;
  setCompactMode: (enabled: boolean) => void;
  setShowLineNumbers: (enabled: boolean) => void;
  setAutoSave: (enabled: boolean) => void;
  setAnimationsEnabled: (enabled: boolean) => void;
  
  // Modal actions
  openModal: (modalId: string, data?: any) => void;
  closeModal: () => void;
  
  // Loading actions
  setGlobalLoading: (loading: boolean, message?: string) => void;
  
  // Breadcrumb actions
  setBreadcrumbs: (breadcrumbs: Array<{ label: string; path?: string }>) => void;
  addBreadcrumb: (breadcrumb: { label: string; path?: string }) => void;
  
  // Reset preferences
  resetPreferences: () => void;
}

/**
 * Default preferences
 */
const defaultPreferences: UIPreferences = {
  theme: 'dark',
  sidebarState: 'expanded',
  compactMode: false,
  showLineNumbers: true,
  autoSave: true,
  animationsEnabled: true,
};

/**
 * UI store with persistence for preferences
 */
export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      // Initial state
      ...defaultPreferences,
      activeModal: null,
      modalData: null,
      globalLoading: false,
      loadingMessage: null,
      breadcrumbs: [],

      // Theme actions
      setTheme: (theme) => {
        set({ theme });
        // Apply theme to document
        if (theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
          document.documentElement.classList.add('dark');
        } else {
          document.documentElement.classList.remove('dark');
        }
      },

      // Sidebar actions
      setSidebarState: (sidebarState) => set({ sidebarState }),
      
      toggleSidebar: () => set((state) => ({
        sidebarState: state.sidebarState === 'expanded' ? 'collapsed' : 'expanded',
      })),

      // Preference actions
      setCompactMode: (compactMode) => set({ compactMode }),
      setShowLineNumbers: (showLineNumbers) => set({ showLineNumbers }),
      setAutoSave: (autoSave) => set({ autoSave }),
      setAnimationsEnabled: (animationsEnabled) => set({ animationsEnabled }),

      // Modal actions
      openModal: (activeModal, modalData = null) => set({ activeModal, modalData }),
      closeModal: () => set({ activeModal: null, modalData: null }),

      // Loading actions
      setGlobalLoading: (globalLoading, loadingMessage = null) => 
        set({ globalLoading, loadingMessage }),

      // Breadcrumb actions
      setBreadcrumbs: (breadcrumbs) => set({ breadcrumbs }),
      
      addBreadcrumb: (breadcrumb) => set((state) => ({
        breadcrumbs: [...state.breadcrumbs, breadcrumb],
      })),

      // Reset preferences
      resetPreferences: () => set(defaultPreferences),
    }),
    {
      name: 'ui-preferences',
      partialize: (state) => ({
        theme: state.theme,
        sidebarState: state.sidebarState,
        compactMode: state.compactMode,
        showLineNumbers: state.showLineNumbers,
        autoSave: state.autoSave,
        animationsEnabled: state.animationsEnabled,
      }),
    }
  )
);

/**
 * Hook to get current theme (resolved from system if needed)
 */
export const useResolvedTheme = (): 'light' | 'dark' => {
  const theme = useUIStore((state) => state.theme);
  
  if (theme === 'system') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  
  return theme;
};

/**
 * Hook to check if animations are enabled
 */
export const useAnimations = () => {
  const enabled = useUIStore((state) => state.animationsEnabled);
  return {
    enabled,
    duration: enabled ? 'duration-200' : 'duration-0',
    transition: enabled ? 'transition-all' : '',
  };
};