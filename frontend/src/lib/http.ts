/**
 * 统一 HTTP 客户端
 * - 自动注入 Authorization: Bearer <token>
 * - 401 自动刷新 token 并重试
 * - 支持 mock fallback（后端不可用时降级）
 */

import { loadSession, clearSession, updateAccessToken } from '@/auth/session';

// ── Token 刷新队列（防止并发刷新） ────────────────────────────────────────────

let isRefreshing = false;
let pendingQueue: Array<(token: string | null) => void> = [];

async function refreshAccessToken(): Promise<string | null> {
  if (isRefreshing) {
    return new Promise(resolve => pendingQueue.push(resolve));
  }
  isRefreshing = true;
  try {
    const session = loadSession();
    if (!session?.refreshToken) throw new Error('No refresh token');

    const res = await fetch('/api/auth/v1/token/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refresh_token: session.refreshToken }),
    });
    if (!res.ok) throw new Error('Refresh failed');

    const { access_token, expires_in } = await res.json();
    updateAccessToken(access_token, expires_in);
    pendingQueue.forEach(cb => cb(access_token));
    return access_token;
  } catch {
    pendingQueue.forEach(cb => cb(null));
    clearSession();
    window.location.href = '/login';
    return null;
  } finally {
    pendingQueue = [];
    isRefreshing = false;
  }
}

// ── 核心请求函数 ──────────────────────────────────────────────────────────────

interface RequestOptions extends RequestInit {
  skipAuth?: boolean;
}

async function request<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const { skipAuth = false, headers: extraHeaders, ...rest } = options;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(extraHeaders as Record<string, string>),
  };

  if (!skipAuth) {
    const session = loadSession();
    if (session?.accessToken) {
      headers['Authorization'] = `Bearer ${session.accessToken}`;
    }
  }

  const res = await fetch(url, { ...rest, headers });

  // 401 → 尝试刷新 token
  if (res.status === 401 && !skipAuth) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      headers['Authorization'] = `Bearer ${newToken}`;
      const retryRes = await fetch(url, { ...rest, headers });
      if (!retryRes.ok) throw new ApiError(retryRes.status, await retryRes.text());
      return unwrap<T>(retryRes);
    }
    throw new ApiError(401, 'Unauthorized');
  }

  if (!res.ok) {
    const msg = await res.text().catch(() => res.statusText);
    throw new ApiError(res.status, msg);
  }

  return unwrap<T>(res);
}

/** 解包后端统一响应格式 { code, message, data } */
async function unwrap<T>(res: Response): Promise<T> {
  const contentType = res.headers.get('content-type');
  if (!contentType?.includes('application/json')) {
    throw new Error('Non-JSON response');
  }
  const json = await res.json();
  // 后端统一格式：{ code, message, data }
  if (json && typeof json === 'object' && 'data' in json) {
    return json.data as T;
  }
  return json as T;
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

// ── 带 mock fallback 的请求封装 ───────────────────────────────────────────────

export async function fetchWithAuth<T>(
  url: string,
  mockFn: () => T,
  options?: RequestOptions
): Promise<T> {
  try {
    return await request<T>(url, options);
  } catch (err) {
    console.warn(`[http] Fetch failed for ${url}, falling back to mock.`, err);
    await new Promise(r => setTimeout(r, 300));
    return mockFn();
  }
}

export async function mutateWithAuth(
  url: string,
  method: string,
  body?: object,
  options?: RequestOptions
): Promise<{ success: boolean }> {
  try {
    return await request<{ success: boolean }>(url, {
      method,
      body: body ? JSON.stringify(body) : undefined,
      ...options,
    });
  } catch (err) {
    console.warn(`[http] Mutate failed for ${url}, simulating success.`, err);
    return { success: true };
  }
}

// ── 便捷方法 ──────────────────────────────────────────────────────────────────

export const http = {
  get: <T>(url: string, options?: RequestOptions) =>
    request<T>(url, { method: 'GET', ...options }),

  post: <T>(url: string, body?: object, options?: RequestOptions) =>
    request<T>(url, { method: 'POST', body: body ? JSON.stringify(body) : undefined, ...options }),

  put: <T>(url: string, body?: object, options?: RequestOptions) =>
    request<T>(url, { method: 'PUT', body: body ? JSON.stringify(body) : undefined, ...options }),

  delete: <T>(url: string, options?: RequestOptions) =>
    request<T>(url, { method: 'DELETE', ...options }),
};
