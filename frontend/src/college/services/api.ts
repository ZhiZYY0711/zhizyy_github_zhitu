import { fetchWithAuth, mutateWithAuth } from '@/lib/http';
import * as Mock from '../mock/generator';

const PORTAL_API = import.meta.env.VITE_PORTAL_COLLEGE_API_BASE_URL || '/api/portal-college/v1';
const COLLEGE_API = import.meta.env.VITE_COLLEGE_API_BASE_URL || '/api/college/v1';
const TRAINING_API = import.meta.env.VITE_TRAINING_API_BASE_URL || '/api/training/v1';
const INTERNSHIP_API = import.meta.env.VITE_INTERNSHIP_API_BASE_URL || '/api/internship/v1';

// ── Dashboard ─────────────────────────────────────────────────────────────────

export const fetchEmploymentStats = (year?: string) => {
  const qs = year ? `?year=${year}` : '';
  return fetchWithAuth(`${PORTAL_API}/dashboard/stats${qs}`, Mock.getMockEmploymentStats);
};

export const fetchTrends = (dimension = 'month') =>
  fetchWithAuth(`${PORTAL_API}/dashboard/trends?dimension=${dimension}`, Mock.getMockTrends);

// ── Students ──────────────────────────────────────────────────────────────────

export const fetchStudents = (params?: { keyword?: string; classId?: string; status?: string }) => {
  const q = new URLSearchParams();
  if (params?.keyword) q.set('keyword', params.keyword);
  if (params?.classId) q.set('classId', params.classId);
  if (params?.status) q.set('status', params.status);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithAuth(`${COLLEGE_API}/students${qs}`, Mock.getMockStudents);
};

// ── Training Plans ────────────────────────────────────────────────────────────

export const fetchTrainingPlans = (semester?: string) => {
  const qs = semester ? `?semester=${semester}` : '';
  return fetchWithAuth(`${TRAINING_API}/college/plans${qs}`, Mock.getMockTrainingPlans);
};

export const createTrainingPlan = (data: object) =>
  mutateWithAuth(`${TRAINING_API}/college/plans`, 'POST', data);

export const assignMentor = (data: object) =>
  mutateWithAuth(`${TRAINING_API}/college/mentors/assign`, 'POST', data);

// ── Internship ────────────────────────────────────────────────────────────────

export const fetchInternshipStudents = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithAuth(`${INTERNSHIP_API}/college/students${qs}`, Mock.getMockInternshipStudents);
};

export const fetchPendingContracts = () =>
  fetchWithAuth(`${INTERNSHIP_API}/college/contracts/pending`, Mock.getMockContracts).then(
    (page: any) => {
      console.log('[API] fetchPendingContracts raw response:', page);
      // MyBatis Plus 分页返回 page.records
      const result = page?.records ?? page?.content ?? [];
      console.log('[API] fetchPendingContracts extracted result:', result);
      return result;
    }
  );

export const auditContract = (id: string, result: 'pass' | 'reject', reject_reason?: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/college/contracts/${id}/audit`, 'POST', { action: result, comment: reject_reason });

export const createInspection = (data: object) =>
  mutateWithAuth(`${INTERNSHIP_API}/college/inspections`, 'POST', data);

// ── CRM ───────────────────────────────────────────────────────────────────────

export const fetchEnterprises = (params?: { level?: string; industry?: string }) => {
  const q = new URLSearchParams();
  if (params?.level) q.set('level', params.level);
  if (params?.industry) q.set('industry', params.industry);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithAuth(`${PORTAL_API}/crm/enterprises${qs}`, Mock.getMockEnterprises);
};

export const fetchCrmAudits = () =>
  fetchWithAuth(`${PORTAL_API}/crm/audits?status=pending`, Mock.getMockCrmAudits);

export const auditEnterprise = (id: string, action: 'pass' | 'reject', comment?: string) =>
  mutateWithAuth(`${PORTAL_API}/crm/audits/${id}`, 'POST', { action, comment });

export const updateEnterpriseLevel = (id: string, level: string, reason: string) =>
  mutateWithAuth(`${PORTAL_API}/crm/enterprises/${id}/level`, 'PUT', { level, reason });

export const fetchVisitRecords = (enterpriseId?: string) => {
  const qs = enterpriseId ? `?enterpriseId=${enterpriseId}` : '';
  return fetchWithAuth(`${PORTAL_API}/crm/visits${qs}`, Mock.getMockVisitRecords);
};

export const createVisitRecord = (data: object) =>
  mutateWithAuth(`${PORTAL_API}/crm/visits`, 'POST', data);

// ── Warnings ──────────────────────────────────────────────────────────────────

export const fetchWarnings = (params?: { level?: string; type?: string; status?: string }) => {
  const q = new URLSearchParams();
  if (params?.level) q.set('level', params.level);
  if (params?.type) q.set('type', params.type);
  if (params?.status) q.set('status', params.status);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithAuth(`${PORTAL_API}/warnings${qs}`, Mock.getMockWarnings);
};

export const fetchWarningStats = () =>
  fetchWithAuth(`${PORTAL_API}/warnings/stats`, Mock.getMockWarningStats);

export const interveneWarning = (id: string, data: object) =>
  mutateWithAuth(`${PORTAL_API}/warnings/${id}/intervene`, 'POST', data);
