
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { TenantProvider } from "@/contexts/TenantContext";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

import { Layout } from "@/components/layout/Layout";
import { Login } from "@/pages/Login";
import { Dashboard } from "@/pages/Dashboard";
import { TestPage } from "@/pages/TestPage";
import { CreateFlowSelection } from "@/pages/CreateFlowSelection";
import { CreateDirectMappingFlow } from "@/pages/CreateDirectMappingFlow";
import { CreateOrchestrationFlow } from "@/pages/CreateOrchestrationFlow";
import { CreateCommunicationAdapter } from "@/pages/CreateCommunicationAdapter";
import CommunicationAdapters from "@/pages/CommunicationAdapters";
import { DataStructures } from "@/pages/DataStructures";
import { CreateDataStructure } from "@/pages/CreateDataStructure";
import { BusinessComponents } from "@/pages/BusinessComponents";
import { Messages } from "@/pages/Messages";
import AdapterMonitoring from "@/pages/AdapterMonitoring";
import InterfaceDetails from "@/pages/InterfaceDetails";
import { Admin } from "@/pages/Admin";
import { Settings } from "@/pages/Settings";
import { RetryManagement } from "@/pages/RetryManagement";
import { MessageAcknowledgmentPage } from "@/pages/MessageAcknowledgment";
import { DevelopmentFunctions } from "@/pages/DevelopmentFunctions";
import AllInterfaces from "@/pages/AllInterfaces";
import NotFound from "./pages/NotFound";

// Import providers and error boundary
import { QueryProvider } from "@/providers/query-provider";
import { ErrorBoundary } from "@/components/error-boundary";
import { NotificationDisplay } from "@/components/notification-display";

// Import stores and effects
import { useUIStore } from "@/stores/ui-store";
import { useEffect } from "react";

// Temporarily removed logger import to debug error boundary issue
// import { logger, LogCategory } from "@/lib/logger";
import { setupAxiosInterceptors } from "@/lib/axios-interceptors";

const ThemeEffect = () => {
  const theme = useUIStore((state) => state.theme);
  
  useEffect(() => {
    // Apply theme on mount and when it changes
    if (theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);
  
  return null;
};

// Navigation logger component - temporarily disabled to debug error
const NavigationLoggerComponent = () => {
  // useNavigationLogger(); // Disabled due to circular dependency
  return null;
};

const App = () => {
  // Setup axios interceptors and log app initialization
  useEffect(() => {
    // Delay interceptor setup to avoid initialization issues
    const timer = setTimeout(() => {
      setupAxiosInterceptors();
    }, 100);
    
    // Temporarily disabled logger to debug error boundary issue
    // logger.info(LogCategory.SYSTEM, 'Application initialized', {
    //   version: import.meta.env.VITE_APP_VERSION || '1.0.0',
    //   environment: import.meta.env.MODE,
    //   userAgent: navigator.userAgent,
    //   screenResolution: `${window.screen.width}x${window.screen.height}`,
    //   viewport: `${window.innerWidth}x${window.innerHeight}`
    // });
    
    console.log('Application initialized', {
      version: import.meta.env.VITE_APP_VERSION || '1.0.0',
      environment: import.meta.env.MODE
    });
    
    return () => clearTimeout(timer);
  }, []);

  return (
    <ErrorBoundary>
      <QueryProvider>
        <TooltipProvider>
          <ThemeEffect />
          <Toaster />
          <NotificationDisplay />
          <BrowserRouter>
            <NavigationLoggerComponent />
            <AuthProvider>
              <TenantProvider>
                <Routes>
                  {/* Public route - no authentication required */}
                  <Route path="/login" element={<Login />} />
                  
                  {/* Protected routes - require authentication */}
                  <Route element={<ProtectedRoute />}>
                <Route path="/" element={<Layout />}>
                  <Route index element={<Navigate to="/dashboard" replace />} />
                  <Route path="dashboard" element={<Dashboard />} />
                  <Route path="test" element={<TestPage />} />
                  <Route path="create-flow" element={<CreateFlowSelection />} />
                  <Route path="create-direct-mapping-flow" element={<CreateDirectMappingFlow />} />
                  <Route path="create-orchestration-flow" element={<CreateOrchestrationFlow />} />
                  <Route path="flows/:flowId/edit" element={<CreateDirectMappingFlow />} />
                  <Route path="communication-adapters" element={<CommunicationAdapters />} />
                  <Route path="communication-adapters/:id" element={<CreateCommunicationAdapter />} />
                  <Route path="create-communication-adapter" element={<CreateCommunicationAdapter />} />
                  <Route path="data-structures" element={<DataStructures />} />
                  <Route path="data-structures/:id" element={<CreateDataStructure />} />
                  <Route path="create-data-structure" element={<CreateDataStructure />} />
                  <Route path="business-components" element={<BusinessComponents />} />
                  <Route path="all-interfaces" element={<AllInterfaces />} />
                  <Route path="interfaces" element={<Navigate to="/all-interfaces" replace />} />
                  <Route path="interfaces/:flowId/details" element={<InterfaceDetails />} />
                  <Route path="messages" element={<Messages />} />
                  <Route path="adapter-monitoring" element={<AdapterMonitoring />} />
                  <Route path="admin" element={<Admin />} />
                  <Route path="settings" element={<Settings />} />
                  <Route path="retry-management" element={<RetryManagement />} />
                  <Route path="message-acknowledgments" element={<MessageAcknowledgmentPage />} />
                  <Route path="development-functions" element={<DevelopmentFunctions />} />
                  <Route path="*" element={<NotFound />} />
                </Route>
              </Route>
            </Routes>
              </TenantProvider>
            </AuthProvider>
          </BrowserRouter>
      </TooltipProvider>
    </QueryProvider>
  </ErrorBoundary>
  );
};

export default App;
