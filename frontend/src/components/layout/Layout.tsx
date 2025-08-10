import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { Breadcrumb } from './Breadcrumb';
import { SessionTimeoutNotification } from '@/components/SessionTimeoutNotification';
import { SessionTimeoutWarning } from '@/components/auth/SessionTimeoutWarning';
import { SidebarProvider } from '@/components/ui/sidebar';

export const Layout = () => {
  return (
    <SidebarProvider>
      <div className="min-h-screen bg-background">
        <Header />
        <div className="flex h-[calc(100vh-4rem)]">
          <Sidebar />
          <div className="flex-1 flex flex-col">
            <Breadcrumb />
            <main className="flex-1 overflow-auto">
              <Outlet />
            </main>
          </div>
        </div>
        <SessionTimeoutNotification />
        <SessionTimeoutWarning />
      </div>
    </SidebarProvider>
  );
};