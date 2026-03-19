import { fetchWithAuth, mutateWithAuth } from '@/lib/http';
import * as MockGenerator from '../mock/generator';

const SYSTEM_API = import.meta.env.VITE_API_BASE_URL || '/api/system/v1';
const PORTAL_API = import.meta.env.VITE_PORTAL_API_BASE_URL || '/api/portal-platform/v1';
const MONITOR_API = import.meta.env.VITE_MONITOR_API_BASE_URL || '/api/monitor/v1';

// ── Dashboard ─────────────────────────────────────────────────────────────────

export const fetchDashboardStats = () =>
  fetchWithAuth(`${SYSTEM_API}/dashboard/stats`, MockGenerator.getMockDashboardStats);

// ── Monitor ───────────────────────────────────────────────────────────────────

export const fetchSystemHealth = () =>
  fetchWithAuth(`${MONITOR_API}/health`, MockGenerator.getMockSystemHealth);

export const fetchOnlineUserTrend = () =>
  fetchWithAuth(`${MONITOR_API}/users/online-trend`, MockGenerator.getMockOnlineUserTrend);

export const fetchServiceStatuses = () =>
  fetchWithAuth(`${MONITOR_API}/services`, MockGenerator.getMockServiceStatuses);

// ── Tenants ───────────────────────────────────────────────────────────────────

export const fetchTenantList = (params?: { type?: string; status?: string }) => {
  const q = new URLSearchParams();
  if (params?.type) q.set('type', params.type);
  if (params?.status) q.set('status', params.status);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithAuth(`${SYSTEM_API}/tenants/colleges${qs}`, MockGenerator.getMockTenantList);
};

// ── Enterprise Audit ──────────────────────────────────────────────────────────

export const fetchEnterpriseAuditList = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithAuth(`${SYSTEM_API}/audits/enterprises${qs}`, MockGenerator.getMockEnterpriseAuditList);
};

export const auditEnterprise = (id: string, action: 'pass' | 'reject', reject_reason?: string) =>
  mutateWithAuth(`${SYSTEM_API}/audits/enterprises/${id}`, 'POST', { action, reject_reason });

// ── Project Audit ─────────────────────────────────────────────────────────────

export const fetchProjectAuditList = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithAuth(`${PORTAL_API}/audits/projects${qs}`, MockGenerator.getMockProjectAuditList);
};

export const auditProject = (id: string, action: 'pass' | 'reject', quality_rating?: string, comment?: string) =>
  mutateWithAuth(`${PORTAL_API}/audits/projects/${id}`, 'POST', { action, quality_rating, comment });

// ── Tags ──────────────────────────────────────────────────────────────────────

export const fetchTags = (category?: string) => {
  const qs = category ? `?category=${category}` : '';
  return fetchWithAuth(`${SYSTEM_API}/tags${qs}`, MockGenerator.getMockTags);
};

export const createTag = (data: { category: string; name: string; parent_id?: string }) =>
  mutateWithAuth(`${SYSTEM_API}/tags`, 'POST', data);

export const deleteTag = (id: string) =>
  mutateWithAuth(`${SYSTEM_API}/tags/${id}`, 'DELETE');

// ── Skill Tree ────────────────────────────────────────────────────────────────

export const fetchSkillTree = () =>
  fetchWithAuth(`${SYSTEM_API}/skills/tree`, MockGenerator.getMockSkillTree);

// ── Certificate Templates ─────────────────────────────────────────────────────

export const fetchCertificateTemplates = () =>
  fetchWithAuth(`${SYSTEM_API}/certificates/templates`, MockGenerator.getMockCertificateTemplates);

// ── Contract Templates ────────────────────────────────────────────────────────

export const fetchContractTemplates = () =>
  fetchWithAuth(`${SYSTEM_API}/contracts/templates`, MockGenerator.getMockContractTemplates);

// ── Recommendation Banners ────────────────────────────────────────────────────

export const fetchRecommendationBanners = () =>
  fetchWithAuth(`${PORTAL_API}/recommendations/banner`, MockGenerator.getMockRecommendationBanners);

export const saveRecommendationBanner = (data: object) =>
  mutateWithAuth(`${PORTAL_API}/recommendations/banner`, 'POST', data);

// ── Top List ──────────────────────────────────────────────────────────────────

export const fetchTopListItems = (listType: 'mentor' | 'course' | 'project') =>
  fetchWithAuth(
    `${PORTAL_API}/recommendations/top-list?list_type=${listType}`,
    () => MockGenerator.getMockTopListItems(listType)
  );

export const saveTopListItems = (listType: string, item_ids: string[]) =>
  mutateWithAuth(`${PORTAL_API}/recommendations/top-list`, 'POST', { list_type: listType, item_ids });

// ── Logs ──────────────────────────────────────────────────────────────────────

export const fetchOperationLogs = (params?: {
  user_id?: string; module?: string; result?: string;
  start_time?: string; end_time?: string;
}) => {
  const q = new URLSearchParams();
  if (params?.user_id) q.set('user_id', params.user_id);
  if (params?.module) q.set('module', params.module);
  if (params?.result) q.set('result', params.result);
  if (params?.start_time) q.set('start_time', params.start_time);
  if (params?.end_time) q.set('end_time', params.end_time);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithAuth(`${SYSTEM_API}/logs/operation${qs}`, MockGenerator.getMockOperationLogs);
};

export const fetchSecurityLogs = (params?: { level?: string }) => {
  const qs = params?.level ? `?level=${params.level}` : '';
  return fetchWithAuth(`${SYSTEM_API}/logs/security${qs}`, MockGenerator.getMockSecurityLogs);
};
