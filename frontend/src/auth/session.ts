import type { AuthSession } from './types';
import { isTokenExpired } from './types';

const SESSION_KEY = 'auth_session';

export function saveSession(session: AuthSession): void {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function loadSession(): AuthSession | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const session = JSON.parse(raw) as AuthSession;
    // token 已过期则清除，强制重新登录
    if (isTokenExpired(session)) {
      clearSession();
      return null;
    }
    return session;
  } catch {
    return null;
  }
}

export function clearSession(): void {
  localStorage.removeItem(SESSION_KEY);
}

/** 对接后端后调用：用新 access_token 更新 session */
export function updateAccessToken(accessToken: string, expiresIn: number): void {
  const session = loadSession();
  if (!session) return;
  session.accessToken = accessToken;
  session.expiresAt = Date.now() + expiresIn * 1000;
  saveSession(session);
}
