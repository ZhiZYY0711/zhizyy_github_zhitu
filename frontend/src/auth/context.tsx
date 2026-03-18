import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { AuthSession, LoginCredentials, LoginResult } from './types';
import { ROLE_ROUTES } from './types';
import { saveSession, loadSession, clearSession } from './session';

interface AuthContextValue {
  session: AuthSession | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => LoginResult;
  logout: () => void;
  getDashboardPath: () => string;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => loadSession());

  const login = useCallback((credentials: LoginCredentials): LoginResult => {
    const { username, password, role } = credentials;
    if (!username.trim()) return { success: false, error: '请输入用户名' };
    if (!password.trim()) return { success: false, error: '请输入密码' };

    const newSession: AuthSession = {
      username: username.trim(),
      role,
      loginAt: Date.now(),
    };
    saveSession(newSession);
    setSession(newSession);
    return { success: true };
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  const getDashboardPath = useCallback(() => {
    if (!session) return '/login';
    return ROLE_ROUTES[session.role];
  }, [session]);

  return (
    <AuthContext.Provider value={{ session, isAuthenticated: !!session, login, logout, getDashboardPath }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
