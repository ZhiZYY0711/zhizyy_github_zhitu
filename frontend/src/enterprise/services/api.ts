import { fetchWithAuth, mutateWithAuth } from '@/lib/http';
import * as Mock from '../mock/generator';

const INTERNSHIP_API = import.meta.env.VITE_INTERNSHIP_API_BASE_URL || '/api/internship/v1';
const PORTAL_API = import.meta.env.VITE_PORTAL_ENTERPRISE_API_BASE_URL || '/api/portal-enterprise/v1';
const TRAINING_API = import.meta.env.VITE_TRAINING_API_BASE_URL || '/api/training/v1';
const USER_API = import.meta.env.VITE_USER_API_BASE_URL || '/api/user/v1';
const GROWTH_API = import.meta.env.VITE_GROWTH_API_BASE_URL || '/api/growth/v1';

// ── Dashboard ─────────────────────────────────────────────────────────────────

export const fetchDashboardStats = () =>
  fetchWithAuth(`${PORTAL_API}/dashboard/stats`, Mock.getMockDashboardStats);

export const fetchTodos = () =>
  fetchWithAuth(`${PORTAL_API}/todos`, Mock.getMockTodos);

export const fetchActivities = () =>
  fetchWithAuth(`${PORTAL_API}/activities`, Mock.getMockActivities);

// ── Jobs ──────────────────────────────────────────────────────────────────────

export const fetchJobs = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithAuth(`${INTERNSHIP_API}/enterprise/jobs${qs}`, Mock.getMockJobs);
};

export const createJob = (data: object) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/jobs`, 'POST', data);

export const closeJob = (id: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/jobs/${id}/close`, 'POST');

// ── Applications ──────────────────────────────────────────────────────────────

export const fetchApplications = (params?: { job_id?: string; status?: string }) => {
  const q = new URLSearchParams();
  if (params?.job_id) q.set('jobId', params.job_id);
  if (params?.status) q.set('status', params.status);
  const qs = q.toString() ? `?${q}` : '';
  return fetchWithAuth(`${INTERNSHIP_API}/enterprise/applications${qs}`, Mock.getMockApplications);
};

export const scheduleInterview = (data: any) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/interviews`, 'POST', {
    applicationId: data.application_id,
    interviewTime: data.time ? new Date(data.time).toISOString() : undefined,
    location: data.link || '待定',
    interviewType: data.type === 'online' ? 'video' : 'onsite'
  });

export const rejectApplication = (id: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/applications/${id}/reject`, 'POST');

// ── Talent Pool ───────────────────────────────────────────────────────────────

export const fetchTalentPool = () =>
  fetchWithAuth(`${PORTAL_API}/talent-pool`, Mock.getMockTalentPool);

export const addToTalentPool = (student_id: string, tags: string[]) =>
  mutateWithAuth(`${PORTAL_API}/talent-pool/collect`, 'POST', { student_id, tags });

export const removeFromTalentPool = (id: string) =>
  mutateWithAuth(`${PORTAL_API}/talent-pool/${id}`, 'DELETE');

// ── Training Projects ─────────────────────────────────────────────────────────

export const fetchTrainingProjects = () =>
  fetchWithAuth(`${TRAINING_API}/enterprise/projects`, Mock.getMockTrainingProjects);

export const createTrainingProject = (data: object) =>
  mutateWithAuth(`${TRAINING_API}/enterprise/projects`, 'POST', data);

export const fetchProjectTeams = (projectId: string) =>
  fetchWithAuth(`${TRAINING_API}/enterprise/projects/${projectId}/teams`, () => Mock.getMockProjectTeams(projectId));

export const assignMentor = (projectId: string, data: object) =>
  mutateWithAuth(`${TRAINING_API}/enterprise/projects/${projectId}/mentors`, 'POST', data);

// ── Interns ───────────────────────────────────────────────────────────────────

export const fetchInterns = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithAuth(`${INTERNSHIP_API}/enterprise/interns${qs}`, Mock.getMockInterns);
};

export const sendOffer = (data: object) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/offers`, 'POST', data);

export const approveAttendance = (record_id: string, action: 'approve' | 'reject', comment?: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/attendance/audit`, 'POST', { attendanceId: record_id, status: action === 'approve' ? 1 : 2, auditRemark: comment });

export const issueCertificate = (intern_id: string, _comment: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/certificates/issue?internshipId=${intern_id}`, 'POST');

// ── Weekly Reports ────────────────────────────────────────────────────────────

export const fetchWeeklyReports = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithAuth(`${INTERNSHIP_API}/mentor/reports${qs}`, Mock.getMockWeeklyReports);
};

export const reviewWeeklyReport = (id: string, _score: number, comment: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/mentor/reports/${id}/review`, 'POST', { reviewComment: comment });

// ── Code Reviews ──────────────────────────────────────────────────────────────

export const fetchCodeReviews = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return fetchWithAuth(`${TRAINING_API}/mentor/code-reviews${qs}`, Mock.getMockCodeReviews);
};

export const submitCodeReview = (id: string, comment: string) =>
  mutateWithAuth(`${TRAINING_API}/mentor/code-reviews/${id}/comment`, 'POST', { comment });

// ── Mentor Dashboard ──────────────────────────────────────────────────────────

export const fetchMentorDashboard = () =>
  fetchWithAuth(`${PORTAL_API}/mentor/dashboard`, Mock.getMockMentorDashboard);

// ── Analytics ─────────────────────────────────────────────────────────────────

export const fetchAnalytics = (timeRange?: string) => {
  const qs = timeRange ? `?range=${timeRange}` : '';
  return fetchWithAuth(`${PORTAL_API}/analytics${qs}`, Mock.getMockAnalytics);
};

// ── Evaluation ────────────────────────────────────────────────────────────────

export const submitEvaluation = (data: object) =>
  mutateWithAuth(`${GROWTH_API}/evaluations/enterprise`, 'POST', data);

// ── Enterprise Profile ────────────────────────────────────────────────────────

export const fetchEnterpriseProfile = () =>
  fetchWithAuth(`${USER_API}/enterprise/profile`, () => ({
    id: 'ent_001',
    name: '字节跳动科技有限公司',
    logo: '',
    industry: '互联网',
    scale: '10000人以上',
    verification_status: 'verified' as const,
  }));
