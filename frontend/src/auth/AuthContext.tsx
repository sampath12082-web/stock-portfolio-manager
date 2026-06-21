import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { UserResponse } from './types';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserResponse | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (accessToken: string, refreshToken: string, user: UserResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(() => localStorage.getItem('accessToken'));
  const [refreshToken, setRefreshToken] = useState<string | null>(() => localStorage.getItem('refreshToken'));
  const [user, setUser] = useState<UserResponse | null>(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = (at: string, rt: string, u: UserResponse) => {
    setAccessToken(at);
    setRefreshToken(rt);
    setUser(u);
    localStorage.setItem('accessToken', at);
    localStorage.setItem('refreshToken', rt);
    localStorage.setItem('user', JSON.stringify(u));
  };

  const logout = () => {
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{
      accessToken, refreshToken, user,
      isAuthenticated: !!accessToken,
      isAdmin: user?.role === 'ROLE_ADMIN',
      login, logout,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
}
