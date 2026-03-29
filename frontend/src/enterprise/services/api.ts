import { fetchWithAuth, mutateWithAuth } from '@/lib/http';
import * as Mock from '../mock/generator';
import type {
  Job, JobResponse,
  Application, ApplicationResponse,
  TalentPoolItem, TalentPoolResponse,
  Intern, InternResponse,
  TodoItem, TodoResponse,
  ActivityItem, ActivityResponse,
  DashboardStats,
  AnalyticsData,
  EnterpriseProfile,
  PageResult,
  TrainingProject, TrainingProjectResponse,
  ProjectTeam, ProjectTeamResponse,
  MentorDashboard,
} from '../types';

const INTERNSHIP_API = import.meta.env.VITE_INTERNSHIP_API_BASE_URL || '/api/internship/v1';
const PORTAL_API = import.meta.env.VITE_PORTAL_ENTERPRISE_API_BASE_URL || '/api/portal-enterprise/v1';
const TRAINING_API = import.meta.env.VITE_TRAINING_API_BASE_URL || '/api/training/v1';
const USER_API = import.meta.env.VITE_USER_API_BASE_URL || '/api/user/v1';
const GROWTH_API = import.meta.env.VITE_GROWTH_API_BASE_URL || '/api/growth/v1';

// ── 数据转换函数 ───────────────────────────────────────────────────────────────

/** 将后端职位数据转换为前端格式 */
function transformJob(item: JobResponse): Job {
  return {
    id: String(item.id),
    title: item.jobTitle,
    type: 'internship',
    description: item.description,
    requirements: typeof item.requirements === 'string'
      ? item.requirements.split('\n').filter(Boolean)
      : [],
    salary_range: `${item.salaryMin}-${item.salaryMax}/天`,
    location: item.city,
    status: item.status === 1 ? 'active' : 'closed',
    created_at: item.createdAt?.slice(0, 10) || '',
    updated_at: item.updatedAt?.slice(0, 10) || item.createdAt?.slice(0, 10) || '',
    tech_stack: item.techStack || [],
    headcount: item.headcount,
    start_date: item.startDate,
    end_date: item.endDate,
  };
}

/** 将前端创建岗位请求转换为后端格式 */
function transformCreateJobRequest(data: {
  title: string;
  description: string;
  requirements: string;
  salary_range: string;
  location: string;
  type?: string;
}) {
  // 解析薪资范围 "200-300/天" -> { min: 200, max: 300 }
  const salaryMatch = data.salary_range.match(/(\d+)-(\d+)/);
  const salaryMin = salaryMatch ? parseInt(salaryMatch[1]) : 0;
  const salaryMax = salaryMatch ? parseInt(salaryMatch[2]) : 0;

  return {
    jobTitle: data.title,
    jobType: '技术类',
    description: data.description,
    requirements: data.requirements,
    techStack: [],
    city: data.location,
    salaryMin,
    salaryMax,
    headcount: 1,
    startDate: new Date().toISOString().slice(0, 10),
    endDate: new Date(Date.now() + 90 * 86400000).toISOString().slice(0, 10),
  };
}

/** 将后端申请数据转换为前端格式 */
function transformApplication(item: ApplicationResponse): Application {
  const statusMap: Record<number, Application['status']> = {
    0: 'pending',
    1: 'offered',
    2: 'rejected',
  };

  return {
    application_id: String(item.id),
    job_id: String(item.jobId),
    job_title: item.jobTitle || '',
    student_id: String(item.studentId),
    student_name: item.studentName,
    student_no: item.studentNo,
    school: item.school || '',
    major: item.major || '',
    resume_url: item.resumeUrl || '',
    apply_time: item.createdAt?.slice(0, 10) || '',
    status: statusMap[item.status] || 'pending',
    interview_time: item.interviewTime,
    interview_type: item.interviewType === 'video' ? 'online' : item.interviewType === 'onsite' ? 'onsite' : undefined,
  };
}

/** 将后端人才库数据转换为前端格式 */
function transformTalentPool(item: TalentPoolResponse): TalentPoolItem {
  let skills: string[] = [];
  try {
    skills = item.skills ? JSON.parse(item.skills) : [];
  } catch {
    skills = [];
  }

  return {
    id: String(item.id),
    student_id: String(item.studentId),
    student_name: item.studentName,
    student_no: item.studentNo,
    major: item.major || '',
    grade: item.grade,
    tags: [],
    skills,
    collect_time: item.collectedAt?.slice(0, 10) || '',
    notes: item.remark,
  };
}

/** 将后端待办数据转换为前端格式 */
function transformTodo(item: TodoResponse): TodoItem {
  const typeMap: Record<string, TodoItem['type']> = {
    'application_review': 'contract',
    'interview_schedule': 'interview',
    'report_review': 'weekly_report',
    'evaluation_pending': 'code_review',
  };

  const priorityMap: Record<number, TodoItem['priority']> = {
    1: 'low',
    2: 'medium',
    3: 'high',
  };

  return {
    id: String(item.id),
    type: typeMap[item.todoType] || 'contract',
    title: item.title,
    description: item.title,
    due_time: item.dueDate,
    priority: priorityMap[item.priority] || 'medium',
    status: item.status === 0 ? 'pending' : 'completed',
    ref_type: item.refType,
    ref_id: String(item.refId),
  };
}

/** 将后端活动数据转换为前端格式 */
function transformActivity(item: ActivityResponse): ActivityItem {
  const typeMap: Record<string, ActivityItem['type']> = {
    'application': 'application',
    'interview': 'interview',
    'report_submitted': 'weekly_report',
    'evaluation': 'review',
  };

  return {
    id: String(item.id),
    type: typeMap[item.activityType] || 'application',
    title: item.description.split(' ')[0] || '',
    description: item.description,
    time: item.createdAt?.slice(0, 10) || '',
    ref_type: item.refType,
    ref_id: String(item.refId),
  };
}

/** 将后端实习生数据转换为前端格式 */
function transformIntern(item: InternResponse): Intern {
  const statusMap: Record<number, Intern['status']> = {
    0: 'pending',
    1: 'active',
    2: 'completed',
    3: 'terminated',
  };

  return {
    id: String(item.id),
    student_id: String(item.studentId),
    name: item.studentName || '',
    student_no: item.studentNo,
    school: item.schoolName || '',
    major: item.major,
    position: item.jobTitle || '',
    start_date: item.startDate,
    end_date: item.endDate,
    mentor_id: String(item.mentorId || 0),
    mentor_name: item.mentorName || '',
    status: statusMap[item.status] || 'pending',
    contract_status: 'signed',
  };
}

/** 将后端实训项目数据转换为前端格式 */
function transformTrainingProject(item: TrainingProjectResponse): TrainingProject {
  return {
    id: String(item.id),
    name: item.name,
    description: item.description || '',
    difficulty: item.difficulty || 3,
    tech_stack: item.techStack || [],
    max_teams: item.maxTeams || 10,
    current_teams: item.currentTeams || 0,
    status: item.status as TrainingProject['status'] || 'draft',
    start_date: item.startDate,
    end_date: item.endDate,
    created_at: item.createdAt?.slice(0, 10) || '',
  };
}

/** 将后端项目团队数据转换为前端格式 */
function transformProjectTeam(item: ProjectTeamResponse): ProjectTeam {
  return {
    team_id: item.teamId,
    project_id: item.projectId,
    team_name: item.teamName,
    members: (item.members || []).map(m => ({
      student_id: m.studentId,
      student_name: m.studentName,
      school: m.school,
      role: m.role as 'leader' | 'member',
    })),
    mentor_id: item.mentorId,
    mentor_name: item.mentorName,
    progress: item.progress || 0,
    status: item.status as ProjectTeam['status'] || 'active',
  };
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

export const fetchDashboardStats = (): Promise<DashboardStats> =>
  fetchWithAuth(`${PORTAL_API}/dashboard/stats`, Mock.getMockDashboardStats);

export const fetchTodos = (): Promise<TodoItem[]> =>
  fetchWithAuth<PageResult<TodoResponse>>(`${PORTAL_API}/todos`, Mock.getMockTodos as any)
    .then(res => res.records?.map(transformTodo) || []);

export const fetchActivities = (): Promise<ActivityItem[]> =>
  fetchWithAuth<PageResult<ActivityResponse>>(`${PORTAL_API}/activities`, Mock.getMockActivities as any)
    .then(res => res.records?.map(transformActivity) || []);

// ── Jobs ──────────────────────────────────────────────────────────────────────

export interface PaginatedJobs {
  records: Job[];
  total: number;
  page: number;
  size: number;
}

export const fetchJobs = (params?: { status?: number; page?: number; size?: number }): Promise<PaginatedJobs> => {
  const q = new URLSearchParams();
  q.set('page', String(params?.page || 1));
  q.set('size', String(params?.size || 10));
  if (params?.status !== undefined) q.set('status', String(params.status));
  const qs = `?${q}`;
  return fetchWithAuth<PageResult<JobResponse>>(`${INTERNSHIP_API}/enterprise/jobs${qs}`, Mock.getMockJobs)
    .then(res => ({
      records: res.records?.map(transformJob) || [],
      total: res.total || 0,
      page: res.page || 1,
      size: res.size || 10,
    }));
};

export const createJob = (data: {
  title: string;
  description: string;
  requirements: string;
  salary_range: string;
  location: string;
  type?: string;
}) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/jobs`, 'POST', transformCreateJobRequest(data));

export const closeJob = (id: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/jobs/${id}/close`, 'POST', {});

// ── Applications ──────────────────────────────────────────────────────────────

export interface PaginatedApplications {
  records: Application[];
  total: number;
  page: number;
  size: number;
}

export const fetchApplications = (params?: { job_id?: number; status?: number; page?: number; size?: number }): Promise<PaginatedApplications> => {
  const q = new URLSearchParams();
  q.set('page', String(params?.page || 1));
  q.set('size', String(params?.size || 10));
  if (params?.job_id !== undefined) q.set('jobId', String(params.job_id));
  if (params?.status !== undefined) q.set('status', String(params.status));
  const qs = `?${q}`;
  return fetchWithAuth<PageResult<ApplicationResponse>>(`${INTERNSHIP_API}/enterprise/applications${qs}`, Mock.getMockApplications as any)
    .then(res => ({
      records: res.records?.map(transformApplication) || [],
      total: res.total || 0,
      page: res.page || 1,
      size: res.size || 10,
    }));
};

export const scheduleInterview = (data: {
  application_id: number;
  student_id: number;
  time?: string;
  link?: string;
  type?: 'online' | 'onsite';
}) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/interviews`, 'POST', {
    applicationId: data.application_id,
    studentId: data.student_id,
    interviewTime: data.time ? new Date(data.time).toISOString() : undefined,
    location: data.link || '待定',
    interviewType: data.type === 'online' ? 'video' : 'onsite',
    notes: '',
  });

export const rejectApplication = (id: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/applications/${id}/reject`, 'POST', {});

// ── Talent Pool ───────────────────────────────────────────────────────────────

export interface PaginatedTalentPool {
  records: TalentPoolItem[];
  total: number;
  page: number;
  size: number;
}

export const fetchTalentPool = (params?: { page?: number; size?: number }): Promise<PaginatedTalentPool> => {
  const q = new URLSearchParams();
  q.set('page', String(params?.page || 1));
  q.set('size', String(params?.size || 10));
  const qs = `?${q}`;
  return fetchWithAuth<PageResult<TalentPoolResponse>>(`${PORTAL_API}/talent-pool${qs}`, Mock.getMockTalentPool as any)
    .then(res => ({
      records: res.records?.map(transformTalentPool) || [],
      total: res.total || 0,
      page: res.page || 1,
      size: res.size || 10,
    }));
};

export const addToTalentPool = (student_id: string, tags: string[]) =>
  mutateWithAuth(`${PORTAL_API}/talent-pool/collect`, 'POST', { studentId: Number(student_id), tags });

export const removeFromTalentPool = (id: string) =>
  mutateWithAuth(`${PORTAL_API}/talent-pool/${id}`, 'DELETE');

// ── Training Projects ─────────────────────────────────────────────────────────

export const fetchTrainingProjects = (): Promise<TrainingProject[]> =>
  fetchWithAuth<TrainingProjectResponse[]>(`${TRAINING_API}/enterprise/projects`, Mock.getMockTrainingProjects as any)
    .then(res => (res || []).map(transformTrainingProject));

export const createTrainingProject = (data: object) =>
  mutateWithAuth(`${TRAINING_API}/enterprise/projects`, 'POST', data);

export const fetchProjectTeams = (projectId: string): Promise<ProjectTeam[]> =>
  fetchWithAuth<ProjectTeamResponse[]>(`${TRAINING_API}/enterprise/projects/${projectId}/teams`, () => Mock.getMockProjectTeams(projectId) as any)
    .then(res => (res || []).map(transformProjectTeam));

export const assignMentor = (projectId: string, data: object) =>
  mutateWithAuth(`${TRAINING_API}/enterprise/projects/${projectId}/mentors`, 'POST', data);

// ── Interns ───────────────────────────────────────────────────────────────────

export const fetchInterns = (status?: number): Promise<Intern[]> => {
  const qs = status !== undefined ? `?status=${status}&page=1&size=100` : '?page=1&size=100';
  return fetchWithAuth<PageResult<InternResponse>>(`${INTERNSHIP_API}/enterprise/interns${qs}`, Mock.getMockInterns as any)
    .then(res => res.records?.map(transformIntern) || []);
};

export const sendOffer = (data: object) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/offers`, 'POST', data);

export const approveAttendance = (record_id: string, action: 'approve' | 'reject', comment?: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/attendance/audit`, 'POST', {
    attendanceId: Number(record_id),
    status: action === 'approve' ? 1 : 2,
    auditRemark: comment,
  });

export const issueCertificate = (intern_id: string, _comment: string) =>
  mutateWithAuth(`${INTERNSHIP_API}/enterprise/certificates/issue?internshipId=${intern_id}`, 'POST', {});

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

/** 将后端导师仪表板数据转换为前端格式 */
function transformMentorDashboard(data: any): MentorDashboard {
  return {
    pending_code_reviews: data.pendingCodeReviewCount || 0,
    pending_weekly_reports: data.pendingReportCount || 0,
    upcoming_interviews: 0, // 后端暂未实现
    students: [], // 后端暂未实现，返回空数组
  };
}

export const fetchMentorDashboard = (): Promise<MentorDashboard> =>
  fetchWithAuth(`${PORTAL_API}/mentor/dashboard`, Mock.getMockMentorDashboard)
    .then(transformMentorDashboard);

// ── Analytics ─────────────────────────────────────────────────────────────────

const transformAnalytics = (data: any): AnalyticsData => ({
  conversion_rate: data.conversionRate ? {
    internship_to_fulltime: data.conversionRate.internshipToFulltime || 0,
    cost_saving: data.conversionRate.costSaving || 0,
  } : { internship_to_fulltime: 0, cost_saving: 0 },
  conversion_trend: data.conversionTrend || [],
  contribution: data.contribution ? {
    total_value: data.contribution.totalValue || 0,
    by_department: (data.contribution.byDepartment || []).map((d: any) => ({
      department: d.department,
      value: d.value,
    })),
  } : { total_value: 0, by_department: [] },
  recruitment_funnel: data.recruitmentFunnel || [],
  applicationTrends: data.applicationTrends || [],
  internPerformance: data.internPerformance,
  projectCompletionRate: data.projectCompletionRate,
  mentorSatisfaction: data.mentorSatisfaction,
});

export const fetchAnalytics = (timeRange?: string): Promise<AnalyticsData> => {
  const qs = timeRange ? `?range=${timeRange}` : '';
  return fetchWithAuth(`${PORTAL_API}/analytics${qs}`, Mock.getMockAnalytics)
    .then(transformAnalytics);
};

// ── Evaluation ────────────────────────────────────────────────────────────────

export const submitEvaluation = (data: object) =>
  mutateWithAuth(`${GROWTH_API}/evaluations/enterprise`, 'POST', data);

// ── Enterprise Profile ────────────────────────────────────────────────────────

export const fetchEnterpriseProfile = (): Promise<EnterpriseProfile> =>
  fetchWithAuth(`${USER_API}/enterprise/profile`, () => ({
    id: 1,
    enterpriseName: '智途科技有限公司',
    industry: '互联网',
    scale: '100-500人',
    city: '深圳市',
  }));

export const updateEnterpriseProfile = (data: Partial<EnterpriseProfile>) =>
  mutateWithAuth(`${USER_API}/enterprise/profile`, 'PUT', data);
