import * as Mock from '../mock/generator';

const INTERNSHIP_API = import.meta.env.VITE_INTERNSHIP_API_BASE_URL || '/api/internship/v1';
const PORTAL_API = import.meta.env.VITE_PORTAL_ENTERPRISE_API_BASE_URL || '/api/portal-enterprise/v1';
const TRAINING_API = import.meta.env.VITE_TRAINING_API_BASE_URL || '/api/training/v1';
const USER_API = import.meta.env.VITE_USER_API_BASE_URL || '/api/user/v1';
const GROWTH_API = import.meta.env.VITE_GROWTH_API_BASE_URL || '/api/growth/v1';

async function fetchWithFallback<T>(url: string, mockFn: () => T, options?: RequestInit): Promise<T> {
  try {
    const res = await fetch(url, options);
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

export const fetchDashboardStats = () =>
  fetchWithFallback(`${PORTAL_API}/dashboard/stats`, Mock.getMockDashboardStats);

export const fetchTodos = () =>
  fetchWithFallback(`${PORTAL_API}/todos`, Mock.getMockTodos);

export const fetchActivities = () =>
  fetchWithFallback(`${PORTAL_API}/activities`, Mock.getMockActivities);

// ── Jobs ──────────────────────────────────────────────────────────────────────

export const fetchJobs = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithFallback(`${INTERNSHIP_API}/enterprise/jobs${qs}`, Mock.getMockJobs);
};

export const createJob = (data: object) =>
  mutate(`${INTERNSHIP_API}/enterprise/jobs`, 'POST', data);

export const closeJob = (id: string) =>
  mutate(`${INTERNSHIP_API}/enterprise/jobs/${id}/close`, 'POST');

// ── Applications ──────────────────────────────────────────────────────────────

export const fetchApplications = (params?: { job_id?: string; status?: string }) => {
  const q = new URLSearchParams();
  if (params?.job_id) q.set('job_id', params.job_id);
  if (params?.status) q.set('status', params.status);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithFallback(`${INTERNSHIP_API}/enterprise/applications${qs}`, Mock.getMockApplications);
};

export const scheduleInterview = (data: object) =>
  mutate(`${INTERNSHIP_API}/enterprise/interviews`, 'POST', data);

export const rejectApplication = (id: string) =>
  mutate(`${INTERNSHIP_API}/enterprise/applications/${id}/reject`, 'POST');

// ── Talent Pool ───────────────────────────────────────────────────────────────

export const fetchTalentPool = () =>
  fetchWithFallback(`${PORTAL_API}/talent-pool`, Mock.getMockTalentPool);

export const addToTalentPool = (student_id: string, tags: string[]) =>
  mutate(`${PORTAL_API}/talent-pool/collect`, 'POST', { student_id, tags });

export const removeFromTalentPool = (id: string) =>
  mutate(`${PORTAL_API}/talent-pool/${id}`, 'DELETE');

// ── Training Projects ─────────────────────────────────────────────────────────

export const fetchTrainingProjects = () =>
  fetchWithFallback(`${TRAINING_API}/enterprise/projects`, Mock.getMockTrainingProjects);

export const createTrainingProject = (data: object) =>
  mutate(`${TRAINING_API}/enterprise/projects`, 'POST', data);

export const fetchProjectTeams = (projectId: string) =>
  fetchWithFallback(`${TRAINING_API}/enterprise/projects/${projectId}/teams`, () => Mock.getMockProjectTeams(projectId));

export const assignMentor = (projectId: string, data: object) =>
  mutate(`${TRAINING_API}/enterprise/projects/${projectId}/mentors`, 'POST', data);

// ── Interns ───────────────────────────────────────────────────────────────────

export const fetchInterns = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithFallback(`${INTERNSHIP_API}/enterprise/interns${qs}`, Mock.getMockInterns);
};

export const sendOffer = (data: object) =>
  mutate(`${INTERNSHIP_API}/enterprise/offers`, 'POST', data);

export const approveAttendance = (record_id: string, action: 'approve' | 'reject', comment?: string) =>
  mutate(`${INTERNSHIP_API}/enterprise/attendance/audit`, 'POST', { record_id, action, comment });

export const issueCertificate = (intern_id: string, comment: string) =>
  mutate(`${INTERNSHIP_API}/enterprise/certificates/issue`, 'POST', { intern_id, comment });

// ── Weekly Reports ────────────────────────────────────────────────────────────

export const fetchWeeklyReports = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithFallback(`${INTERNSHIP_API}/mentor/reports${qs}`, Mock.getMockWeeklyReports);
};

export const reviewWeeklyReport = (id: string, score: number, comment: string) =>
  mutate(`${INTERNSHIP_API}/mentor/reports/${id}/review`, 'POST', { score, comment });

// ── Code Reviews ──────────────────────────────────────────────────────────────

export const fetchCodeReviews = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithFallback(`${TRAINING_API}/mentor/code-reviews${qs}`, Mock.getMockCodeReviews);
};

export const submitCodeReview = (id: string, comment: string) =>
  mutate(`${TRAINING_API}/mentor/code-reviews/${id}/comment`, 'POST', { comment });

// ── Mentor Dashboard ──────────────────────────────────────────────────────────

export const fetchMentorDashboard = () =>
  fetchWithFallback(`${PORTAL_API}/mentor/dashboard`, Mock.getMockMentorDashboard);

// ── Analytics ─────────────────────────────────────────────────────────────────

export const fetchAnalytics = (timeRange?: string) => {
  const qs = timeRange ? `?range=${timeRange}` : '';
  return fetchWithFallback(`${PORTAL_API}/analytics${qs}`, Mock.getMockAnalytics);
};

// ── Evaluation ────────────────────────────────────────────────────────────────

export const submitEvaluation = (data: object) =>
  mutate(`${GROWTH_API}/evaluations/enterprise`, 'POST', data);

// ── Enterprise Profile ────────────────────────────────────────────────────────

export const fetchEnterpriseProfile = () =>
  fetchWithFallback(`${USER_API}/enterprise/profile`, () => ({
    id: 'ent_001',
    name: '字节跳动科技有限公司',
    logo: '',
    industry: '互联网',
    scale: '10000人以上',
    verification_status: 'verified' as const,
  }));
