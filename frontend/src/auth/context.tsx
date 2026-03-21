import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { AuthSession, LoginCredentials, LoginResult } from './types';
import { ROLE_ROUTES } from './types';
import { saveSession, loadSession, clearSession } from './session';

interface AuthContextValue {
  session: AuthSession | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => Promise<LoginResult>;
  logout: () => void;
  getDashboardPath: () => string;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => loadSession());

  const login = useCallback(async (credentials: LoginCredentials): Promise<LoginResult> => {
    const { username, password, role } = credentials;
    if (!username.trim()) return { success: false, error: '请输入用户名' };
    if (!password.trim()) return { success: false, error: '请输入密码' };

    try {
      const res = await fetch('/api/auth/v1/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: username.trim(), password, role }),
      });

      if (res.ok) {
        const data = await res.json();
        // 后端返回: { code, message, data: { accessToken, refreshToken, expiresIn, userInfo } }
        const payload = data.data ?? data;
        const newSession: AuthSession = {
          username: payload.userInfo?.username ?? username.trim(),
          role: payload.userInfo?.role ?? role,
          loginAt: Date.now(),
          accessToken: payload.accessToken,
          refreshToken: payload.refreshToken,
          expiresAt: payload.expiresIn ? Date.now() + payload.expiresIn * 1000 : undefined,
          userInfo: payload.userInfo,
        };
        saveSession(newSession);
        setSession(newSession);
        return { success: true };
      }

      // 后端返回错误
      const errData = await res.json().catch(() => ({}));
      return { success: false, error: errData.message ?? '用户名或密码错误' };

    } catch {
      // 后端不可用时降级：允许本地 mock 登录（开发阶段）
      console.warn('[auth] Login API unavailable, using mock session');
      const mockSession: AuthSession = {
        username: username.trim(),
        role,
        loginAt: Date.now(),
      };
      saveSession(mockSession);
      setSession(mockSession);
      return { success: true };
    }
  }, []);

  const logout = useCallback(async () => {
    const session = loadSession();
    if (session?.accessToken) {
      // 通知后端注销（忽略失败）
      fetch('/api/auth/v1/logout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.accessToken}`,
        },
      }).catch(() => {});
    }
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
