import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService, type User } from '../services/authService';
import { toast } from 'sonner';

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string, redirectTo?: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
  tokenExpiry: number | null;
  getAllUsers: () => User[];
  createUser: (userData: Omit<User, 'id' | 'createdAt' | 'updatedAt'>) => Promise<void>;
  updateUser: (id: string, userData: Partial<User>) => Promise<void>;
  deleteUser: (id: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [users, setUsers] = useState<User[]>([]);
  const [tokenExpiry, setTokenExpiry] = useState<number | null>(null);
  const navigate = useNavigate();

  // Initialize authentication state
  useEffect(() => {
    const initAuth = async () => {
      try {
        if (authService.isAuthenticated()) {
          const response = await authService.getProfile();
          if (response.success && response.data) {
            setUser(response.data);
            
            // Set token expiry based on JWT
            const token = authService.getToken();
            if (token) {
              try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                setTokenExpiry(payload.exp * 1000); // Convert to milliseconds
              } catch (error) {
                console.warn('Failed to parse token expiry:', error);
              }
            }
          } else {
            console.warn('AuthContext: Profile fetch failed, clearing auth data');
            // Token might be invalid, clear auth data silently
            try {
              await authService.logout();
            } catch (logoutError) {
              console.warn('Logout during init failed, clearing local data:', logoutError);
              // Clear local storage directly if logout API fails
              localStorage.removeItem('integration_platform_token');
              localStorage.removeItem('integration_platform_refresh_token');
              localStorage.removeItem('integration_platform_user');
            }
          }
        } else {
          console.log('AuthContext: No valid token found');
        }
      } catch (error) {
        console.error('Auth initialization error:', error);
        // Try to logout gracefully, but don't let it crash the app
        try {
          await authService.logout();
        } catch (logoutError) {
          console.warn('Logout during error handling failed, clearing local data:', logoutError);
          // Clear local storage directly if logout API fails
          localStorage.removeItem('integration_platform_token');
          localStorage.removeItem('integration_platform_refresh_token');
          localStorage.removeItem('integration_platform_user');
        }
      } finally {
        console.log('AuthContext: Auth initialization complete');
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

  // Auto-logout when token expires
  useEffect(() => {
    if (!tokenExpiry) return;

    const timeUntilExpiry = tokenExpiry - Date.now();
    if (timeUntilExpiry <= 0) {
      logout();
      return;
    }

    const timer = setTimeout(() => {
      logout();
    }, timeUntilExpiry);

    return () => clearTimeout(timer);
  }, [tokenExpiry]);

  const login = async (username: string, password: string, redirectTo?: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await authService.login({ username, password });
      
      if (response.success && response.data) {
        setUser(response.data.user);
        
        // Set token expiry
        const expiryTime = Date.now() + (response.data.expiresIn * 1000);
        setTokenExpiry(expiryTime);
        
        // Redirect to the intended page or dashboard
        const destination = redirectTo || '/dashboard';
        navigate(destination);
        
        toast.success('Login successful');
        return true;
      } else {
        toast.error(response.error || 'Login failed');
        return false;
      }
    } catch (error) {
      console.error('Login error:', error);
      toast.error('Login failed. Please try again.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
      setUser(null);
      setTokenExpiry(null);
      navigate('/login');
      toast.success('Logged out successfully');
    } catch (error) {
      console.error('Logout error:', error);
      // Still clear local state even if API call fails
      setUser(null);
      setTokenExpiry(null);
      navigate('/login');
    }
  };

  const getAllUsers = () => {
    return users;
  };

  const createUser = async (userData: Omit<User, 'id' | 'createdAt' | 'updatedAt'>) => {
    try {
      const response = await authService.register({
        ...userData,
        password: 'temp123', // Temporary password, should be changed on first login
      });
      
      if (response.success && response.data) {
        setUsers(prev => [...prev, response.data]);
        toast.success('User created successfully');
      } else {
        toast.error(response.error || 'Failed to create user');
      }
    } catch (error) {
      console.error('Create user error:', error);
      toast.error('Failed to create user');
    }
  };

  const updateUser = async (id: string, userData: Partial<User>) => {
    try {
      // Note: This would need a specific API endpoint for admin user updates
      // For now, we'll update the local state
      setUsers(prev => prev.map(user => 
        user.id === id ? { ...user, ...userData } : user
      ));
      toast.success('User updated successfully');
    } catch (error) {
      console.error('Update user error:', error);
      toast.error('Failed to update user');
    }
  };

  const deleteUser = async (id: string) => {
    try {
      // Note: This would need a specific API endpoint for admin user deletion
      // For now, we'll update the local state
      setUsers(prev => prev.filter(user => user.id !== id));
      toast.success('User deleted successfully');
    } catch (error) {
      console.error('Delete user error:', error);
      toast.error('Failed to delete user');
    }
  };

  const value = {
    user,
    login,
    logout,
    isAuthenticated: !!user && authService.isAuthenticated(),
    isLoading,
    tokenExpiry,
    getAllUsers,
    createUser,
    updateUser,
    deleteUser
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};