
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { Layout } from "@/components/layout/Layout";
import { Login } from "@/pages/Login";
import { Dashboard } from "@/pages/Dashboard";
import { CreateFlowSelection } from "@/pages/CreateFlowSelection";
import { CreateDirectMappingFlow } from "@/pages/CreateDirectMappingFlow";
import { CreateOrchestrationFlow } from "@/pages/CreateOrchestrationFlow";
import { CreateCommunicationAdapter } from "@/pages/CreateCommunicationAdapter";
import { DataStructures } from "@/pages/DataStructures";
import { BusinessComponents } from "@/pages/BusinessComponents";
import { Messages } from "@/pages/Messages";
import Channels from "@/pages/Channels";

import { Admin } from "@/pages/Admin";
import { Settings } from "@/pages/Settings";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
              <Route index element={<Navigate to="/admin" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="create-flow" element={<CreateFlowSelection />} />
              <Route path="create-direct-mapping-flow" element={<CreateDirectMappingFlow />} />
              <Route path="create-orchestration-flow" element={<CreateOrchestrationFlow />} />
              <Route path="create-communication-adapter" element={<CreateCommunicationAdapter />} />
              <Route path="data-structures" element={<DataStructures />} />
              <Route path="business-components" element={<BusinessComponents />} />
              <Route path="messages" element={<Messages />} />
              <Route path="channels" element={<Channels />} />
              <Route path="admin" element={<Admin />} />
              <Route path="settings" element={<Settings />} />
            </Route>
            {/* All other routes (including 404) require authentication */}
            <Route path="*" element={<ProtectedRoute><NotFound /></ProtectedRoute>} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
