import * as Mock from '../mock/generator';

const PORTAL_API = import.meta.env.VITE_PORTAL_COLLEGE_API_BASE_URL || '/api/portal-college/v1';
const USER_API = import.meta.env.VITE_USER_API_BASE_URL || '/api/user/v1';
const TRAINING_API = import.meta.env.VITE_TRAINING_API_BASE_URL || '/api/training/v1';
const INTERNSHIP_API = import.meta.env.VITE_INTERNSHIP_API_BASE_URL || '/api/internship/v1';

async function fetchWithFallback<T>(url: string, mockFn: () => T): Promise<T> {
  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`API Error: ${res.status}`);
    const ct = res.headers.get('content-type');
    if (!ct?.includes('application/json')) throw new Error('Non-JSON response');
    return await res.json() as T;
  } catch (err) {
    console.warn(`Fetch failed for ${url}, falling back to mock.`, err);
    await new Promise(r => setTimeout(r, 300));
    return mockFn();
  }
}

async function mutate(url: string, method: string, body?: object): Promise<{ success: boolean }> {
  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: body ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) throw new Error(`${method} ${url} failed: ${res.status}`);
    return await res.json();
  } catch (err) {
    console.warn(`Mutate failed for ${url}, simulating success.`, err);
    return { success: true };
  }
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

export const fetchEmploymentStats = (year?: string) => {
  const qs = year ? `?year=${year}` : '';
  return fetchWithFallback(`${PORTAL_API}/dashboard/stats${qs}`, Mock.getMockEmploymentStats);
};

export const fetchTrends = (dimension = 'month') =>
  fetchWithFallback(`${PORTAL_API}/dashboard/trends?dimension=${dimension}`, Mock.getMockTrends);

// ── Students ──────────────────────────────────────────────────────────────────

export const fetchStudents = (params?: { keyword?: string; class_id?: string; status?: string }) => {
  const q = new URLSearchParams();
  if (params?.keyword) q.set('keyword', params.keyword);
  if (params?.class_id) q.set('class_id', params.class_id);
  if (params?.status) q.set('status', params.status);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithFallback(`${USER_API}/college/students${qs}`, Mock.getMockStudents);
};

// ── Training Plans ────────────────────────────────────────────────────────────

export const fetchTrainingPlans = (semester?: string) => {
  const qs = semester ? `?semester=${semester}` : '';
  return fetchWithFallback(`${TRAINING_API}/college/plans${qs}`, Mock.getMockTrainingPlans);
};

export const createTrainingPlan = (data: object) =>
  mutate(`${TRAINING_API}/college/plans`, 'POST', data);

export const assignMentor = (data: object) =>
  mutate(`${TRAINING_API}/college/mentors/assign`, 'POST', data);

// ── Internship ────────────────────────────────────────────────────────────────

export const fetchInternshipStudents = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithFallback(`${INTERNSHIP_API}/college/students${qs}`, Mock.getMockInternshipStudents);
};

export const fetchPendingContracts = () =>
  fetchWithFallback(`${INTERNSHIP_API}/college/contracts/pending`, Mock.getMockContracts);

export const auditContract = (id: string, result: 'pass' | 'reject', reject_reason?: string) =>
  mutate(`${INTERNSHIP_API}/college/contracts/${id}/audit`, 'POST', { result, reject_reason });

export const createInspection = (data: object) =>
  mutate(`${INTERNSHIP_API}/college/inspections`, 'POST', data);

// ── CRM ───────────────────────────────────────────────────────────────────────

export const fetchEnterprises = (params?: { level?: string; industry?: string }) => {
  const q = new URLSearchParams();
  if (params?.level) q.set('level', params.level);
  if (params?.industry) q.set('industry', params.industry);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithFallback(`${PORTAL_API}/crm/enterprises${qs}`, Mock.getMockEnterprises);
};

export const fetchCrmAudits = () =>
  fetchWithFallback(`${PORTAL_API}/crm/audits?status=pending`, Mock.getMockCrmAudits);

export const auditEnterprise = (id: string, action: 'approve' | 'reject', comment?: string) =>
  mutate(`${PORTAL_API}/crm/audits/${id}`, 'POST', { action, comment });

export const updateEnterpriseLevel = (id: string, level: string, reason: string) =>
  mutate(`${PORTAL_API}/crm/enterprises/${id}/level`, 'PUT', { level, reason });

export const fetchVisitRecords = (enterprise_id?: string) => {
  const qs = enterprise_id ? `?enterprise_id=${enterprise_id}` : '';
  return fetchWithFallback(`${PORTAL_API}/crm/visits${qs}`, Mock.getMockVisitRecords);
};

export const createVisitRecord = (data: object) =>
  mutate(`${PORTAL_API}/crm/visits`, 'POST', data);

// ── Warnings ──────────────────────────────────────────────────────────────────

export const fetchWarnings = (params?: { level?: string; type?: string; status?: string }) => {
  const q = new URLSearchParams();
  if (params?.level) q.set('level', params.level);
  if (params?.type) q.set('type', params.type);
  if (params?.status) q.set('status', params.status);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithFallback(`${PORTAL_API}/warnings${qs}`, Mock.getMockWarnings);
};

export const fetchWarningStats = () =>
  fetchWithFallback(`${PORTAL_API}/warnings/stats`, Mock.getMockWarningStats);

export const interveneWarning = (id: string, data: object) =>
  mutate(`${PORTAL_API}/warnings/${id}/intervene`, 'POST', data);
