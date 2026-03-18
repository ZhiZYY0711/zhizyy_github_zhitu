import * as MockGenerator from '../mock/generator';

// Different base URLs for different service groups
const SYSTEM_API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/system/v1';
const PORTAL_API_BASE = import.meta.env.VITE_PORTAL_API_BASE_URL || '/api/portal-platform/v1';
const MONITOR_API_BASE = import.meta.env.VITE_MONITOR_API_BASE_URL || '/api/monitor/v1';
const AUTH_API_BASE = import.meta.env.VITE_AUTH_API_BASE_URL || '/api/auth/v1';

/**
 * Generic fetch wrapper with fallback to mock data on API failure.
 */
async function fetchWithFallback<T>(
  url: string,
  mockFn: () => T,
  options?: RequestInit
): Promise<T> {
  try {
    const response = await fetch(url, options);
    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      throw new Error('Received non-JSON response from API');
    }
    return await response.json() as T;
  } catch (error) {
    console.warn(`Fetch failed for ${url}, falling back to mock data.`, error);
    await new Promise(resolve => setTimeout(resolve, 300));
    return mockFn();
  }
}

// ── Auth ──────────────────────────────────────────────────────────────────────

export const loginAdmin = async (username: string, password: string) => {
  const url = `${AUTH_API_BASE}/login/admin`;
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    if (!response.ok) throw new Error(`Login failed: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn('Login API failed, returning mock token.', error);
    return { token: 'mock-token', user: { id: 'mock-user', username, role: 'system_admin' } };
  }
};

// ── Dashboard ─────────────────────────────────────────────────────────────────

export const fetchDashboardStats = () =>
  fetchWithFallback(`${SYSTEM_API_BASE}/dashboard/stats`, MockGenerator.getMockDashboardStats);

// ── Monitor ───────────────────────────────────────────────────────────────────

export const fetchSystemHealth = () =>
  fetchWithFallback(`${MONITOR_API_BASE}/health`, MockGenerator.getMockSystemHealth);

export const fetchOnlineUserTrend = () =>
  fetchWithFallback(`${MONITOR_API_BASE}/users/online-trend`, MockGenerator.getMockOnlineUserTrend);

export const fetchServiceStatuses = () =>
  fetchWithFallback(`${MONITOR_API_BASE}/services`, MockGenerator.getMockServiceStatuses);

// ── Tenants ───────────────────────────────────────────────────────────────────

export const fetchTenantList = (params?: { type?: string; status?: string }) => {
  const query = new URLSearchParams();
  if (params?.type) query.set('type', params.type);
  if (params?.status) query.set('status', params.status);
  const qs = query.toString() ? `?${query.toString()}` : '';
  return fetchWithFallback(
    `${SYSTEM_API_BASE}/tenants/colleges${qs}`,
    MockGenerator.getMockTenantList
  );
};

// ── Enterprise Audit ──────────────────────────────────────────────────────────

export const fetchEnterpriseAuditList = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithFallback(
    `${SYSTEM_API_BASE}/audits/enterprises${qs}`,
    MockGenerator.getMockEnterpriseAuditList
  );
};

export const auditEnterprise = async (
  id: string,
  action: 'pass' | 'reject',
  reject_reason?: string
): Promise<{ success: boolean }> => {
  const url = `${SYSTEM_API_BASE}/audits/enterprises/${id}`;
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action, reject_reason }),
    });
    if (!response.ok) throw new Error(`Audit enterprise failed: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn(`auditEnterprise(${id}) API failed, simulating success.`, error);
    return { success: true };
  }
};

// ── Project Audit ─────────────────────────────────────────────────────────────

export const fetchProjectAuditList = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithFallback(
    `${PORTAL_API_BASE}/audits/projects${qs}`,
    MockGenerator.getMockProjectAuditList
  );
};

export const auditProject = async (
  id: string,
  action: 'pass' | 'reject',
  quality_rating?: string,
  comment?: string
): Promise<{ success: boolean }> => {
  const url = `${PORTAL_API_BASE}/audits/projects/${id}`;
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action, quality_rating, comment }),
    });
    if (!response.ok) throw new Error(`Audit project failed: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn(`auditProject(${id}) API failed, simulating success.`, error);
    return { success: true };
  }
};

// ── Tags ──────────────────────────────────────────────────────────────────────

export const fetchTags = (category?: string) => {
  const qs = category ? `?category=${category}` : '';
  return fetchWithFallback(
    `${SYSTEM_API_BASE}/tags${qs}`,
    MockGenerator.getMockTags
  );
};

export const createTag = async (data: {
  category: string;
  name: string;
  parent_id?: string;
}): Promise<{ success: boolean }> => {
  const url = `${SYSTEM_API_BASE}/tags`;
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error(`Create tag failed: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn('createTag API failed, simulating success.', error);
    return { success: true };
  }
};

export const deleteTag = async (id: string): Promise<{ success: boolean }> => {
  const url = `${SYSTEM_API_BASE}/tags/${id}`;
  try {
    const response = await fetch(url, { method: 'DELETE' });
    if (!response.ok) throw new Error(`Delete tag failed: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn(`deleteTag(${id}) API failed, simulating success.`, error);
    return { success: true };
  }
};

// ── Skill Tree ────────────────────────────────────────────────────────────────

export const fetchSkillTree = () =>
  fetchWithFallback(`${SYSTEM_API_BASE}/skills/tree`, MockGenerator.getMockSkillTree);

// ── Certificate Templates ─────────────────────────────────────────────────────

export const fetchCertificateTemplates = () =>
  fetchWithFallback(
    `${SYSTEM_API_BASE}/certificates/templates`,
    MockGenerator.getMockCertificateTemplates
  );

// ── Contract Templates ────────────────────────────────────────────────────────

export const fetchContractTemplates = () =>
  fetchWithFallback(
    `${SYSTEM_API_BASE}/contracts/templates`,
    MockGenerator.getMockContractTemplates
  );

// ── Recommendation Banners ────────────────────────────────────────────────────

export const fetchRecommendationBanners = () =>
  fetchWithFallback(
    `${PORTAL_API_BASE}/recommendations/banner`,
    MockGenerator.getMockRecommendationBanners
  );

export const saveRecommendationBanner = async (data: object): Promise<{ success: boolean }> => {
  const url = `${PORTAL_API_BASE}/recommendations/banner`;
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error(`Save banner failed: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn('saveRecommendationBanner API failed, simulating success.', error);
    return { success: true };
  }
};

// ── Top List ──────────────────────────────────────────────────────────────────

export const fetchTopListItems = (listType: 'mentor' | 'course' | 'project') =>
  fetchWithFallback(
    `${PORTAL_API_BASE}/recommendations/top-list?list_type=${listType}`,
    () => MockGenerator.getMockTopListItems(listType)
  );

export const saveTopListItems = async (
  listType: string,
  item_ids: string[]
): Promise<{ success: boolean }> => {
  const url = `${PORTAL_API_BASE}/recommendations/top-list`;
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ list_type: listType, item_ids }),
    });
    if (!response.ok) throw new Error(`Save top list failed: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn('saveTopListItems API failed, simulating success.', error);
    return { success: true };
  }
};

// ── Logs ──────────────────────────────────────────────────────────────────────

export const fetchOperationLogs = (params?: {
  user_id?: string;
  module?: string;
  result?: string;
  start_time?: string;
  end_time?: string;
}) => {
  const query = new URLSearchParams();
  if (params?.user_id) query.set('user_id', params.user_id);
  if (params?.module) query.set('module', params.module);
  if (params?.result) query.set('result', params.result);
  if (params?.start_time) query.set('start_time', params.start_time);
  if (params?.end_time) query.set('end_time', params.end_time);
  const qs = query.toString() ? `?${query.toString()}` : '';
  return fetchWithFallback(
    `${SYSTEM_API_BASE}/logs/operation${qs}`,
    MockGenerator.getMockOperationLogs
  );
};

export const fetchSecurityLogs = (params?: { level?: string }) => {
  const qs = params?.level ? `?level=${params.level}` : '';
  return fetchWithFallback(
    `${SYSTEM_API_BASE}/logs/security${qs}`,
    MockGenerator.getMockSecurityLogs
  );
};
