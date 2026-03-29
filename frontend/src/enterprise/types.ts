export const EnterpriseRole = {
  HR: 'hr',
  MENTOR: 'mentor',
  ADMIN: 'admin',
} as const;

export type EnterpriseRole = typeof EnterpriseRole[keyof typeof EnterpriseRole];

export interface EnterpriseUser {
  id: string;
  name: string;
  role: EnterpriseRole;
  enterprise_id: string;
  enterprise_name: string;
  avatar?: string;
  email?: string;
}

// ── 后端岗位类型 ─────────────────────────────────────────────────────────────

/** 后端岗位响应格式 */
export interface JobResponse {
  id: number;
  jobTitle: string;
  jobType: string;
  description: string;
  requirements: string;
  techStack: string[];
  city: string;
  salaryMin: number;
  salaryMax: number;
  headcount: number;
  startDate: string;
  endDate: string;
  status: number; // 1-招募中，0-已关闭
  createdAt: string;
  updatedAt?: string;
}

/** 前端岗位类型（转换后） */
export interface Job {
  id: string;
  title: string;
  type: 'internship' | 'full_time' | 'part_time';
  description: string;
  requirements: string[];
  salary_range: string;
  location: string;
  status: 'active' | 'closed' | 'draft';
  created_at: string;
  updated_at: string;
  applicant_count?: number;
  tech_stack?: string[];
  headcount?: number;
  start_date?: string;
  end_date?: string;
}

// ── 申请类型 ──────────────────────────────────────────────────────────────────

/** 后端申请响应格式 */
export interface ApplicationResponse {
  id: number;
  jobId: number;
  jobTitle?: string;
  studentId: number;
  studentName: string;
  studentNo?: string;
  school?: string;
  major?: string;
  resumeUrl?: string;
  status: number; // 0-待处理，1-已通过，2-已拒绝
  interviewTime?: string;
  interviewType?: string;
  interviewLocation?: string;
  createdAt: string;
}

/** 前端申请类型（转换后） */
export interface Application {
  application_id: string;
  job_id: string;
  job_title: string;
  student_id: string;
  student_name: string;
  student_no?: string;
  school: string;
  major: string;
  resume_url: string;
  match_score?: number;
  apply_time: string;
  status: 'pending' | 'interview' | 'offered' | 'rejected' | 'withdrawn';
  interview_time?: string;
  interview_type?: 'online' | 'onsite';
  interview_link?: string;
}

// ── 人才库类型 ───────────────────────────────────────────────────────────────

/** 后端人才库响应格式 */
export interface TalentPoolResponse {
  id: number;
  studentId: number;
  studentName: string;
  studentNo?: string;
  major?: string;
  grade?: string;
  skills?: string; // JSON array string
  remark?: string;
  collectedAt: string;
}

/** 前端人才库类型（转换后） */
export interface TalentPoolItem {
  id: string;
  student_id: string;
  student_name: string;
  student_no?: string;
  school?: string;
  major: string;
  grade?: string;
  tags: string[];
  skills: string[];
  collect_time: string;
  notes?: string;
}

// ── 实训项目 ───────────────────────────────────────────────────────────────

/** 后端实训项目响应格式 */
export interface TrainingProjectResponse {
  id: number;
  name: string;
  description: string;
  difficulty: number;
  techStack: string[];
  maxTeams: number;
  currentTeams: number;
  status: string;
  startDate?: string;
  endDate?: string;
  createdAt: string;
}

export interface TrainingProject {
  id: string;
  name: string;
  description: string;
  difficulty: number; // 1-5
  tech_stack: string[];
  max_teams: number;
  current_teams: number;
  resources_url?: string;
  status: 'draft' | 'recruiting' | 'in_progress' | 'completed';
  start_date?: string;
  end_date?: string;
  created_at: string;
}

export interface TeamMember {
  student_id: string;
  student_name: string;
  school: string;
  role: 'leader' | 'member';
}

/** 后端项目团队响应格式 */
export interface ProjectTeamResponse {
  teamId: string;
  projectId: string;
  teamName: string;
  members: TeamMemberResponse[];
  mentorId?: string;
  mentorName?: string;
  progress: number;
  status: string;
}

export interface TeamMemberResponse {
  studentId: string;
  studentName: string;
  school: string;
  role: string;
}

export interface ProjectTeam {
  team_id: string;
  project_id: string;
  team_name: string;
  members: TeamMember[];
  mentor_id?: string;
  mentor_name?: string;
  progress: number; // 0-100
  status: 'active' | 'completed' | 'dropped';
}

// ── 实习生类型 ───────────────────────────────────────────────────────────────

/** 后端实习生响应格式 */
export interface InternResponse {
  id: number;
  studentId: number;
  studentName: string;
  studentNo?: string;
  studentPhone?: string;
  schoolName?: string;
  major?: string;
  grade?: string;
  enterpriseId: number;
  enterpriseName?: string;
  jobId?: number;
  jobTitle?: string;
  mentorId?: number;
  mentorName?: string;
  teacherId?: number;
  teacherName?: string;
  startDate: string;
  endDate?: string;
  status: number; // 1=实习中，2=已结束
  createdAt: string;
  updatedAt?: string;
}

/** 前端实习生类型（转换后） */
export interface Intern {
  id: string;
  student_id: string;
  name: string;
  student_no?: string;
  school?: string;
  major?: string;
  position: string;
  start_date: string;
  end_date?: string;
  mentor_id: string;
  mentor_name: string;
  status: 'pending' | 'active' | 'completed' | 'terminated';
  contract_status: 'pending' | 'signed' | 'rejected';
  salary?: string;
}

// ── 周报与代码评审 ──────────────────────────────────────────────────────────

export interface WeeklyReport {
  id: string;
  intern_id: string;
  intern_name: string;
  week: number;
  content: string;
  submit_time: string;
  score?: number; // 0-100
  mentor_comment?: string;
  status: 'pending' | 'reviewed';
}

export interface CodeReview {
  id: string;
  project_id: string;
  project_name: string;
  student_id: string;
  student_name: string;
  file: string;
  line: number;
  code_snippet: string;
  comment: string;
  status: 'pending' | 'resolved' | 'closed';
  created_at: string;
  resolved_at?: string;
}

// ── Dashboard 类型 ───────────────────────────────────────────────────────────

export interface DashboardStats {
  activeJobCount: number;
  pendingApplicationCount: number;
  activeInternCount: number;
  trainingProjectCount: number;
}

/** 后端待办响应格式 */
export interface TodoResponse {
  id: number;
  todoType: 'application_review' | 'interview_schedule' | 'report_review' | 'evaluation_pending';
  refType: 'job' | 'application' | 'intern';
  refId: number;
  title: string;
  priority: number; // 1=低 2=中 3=高
  dueDate?: string;
  status: number; // 0=待处理 1=已完成
  createdAt: string;
}

/** 前端待办类型（转换后） */
export interface TodoItem {
  id: string;
  type: 'interview' | 'weekly_report' | 'code_review' | 'contract';
  title: string;
  description: string;
  due_time?: string;
  priority: 'high' | 'medium' | 'low';
  status: 'pending' | 'completed';
  ref_type?: string;
  ref_id?: string;
}

/** 后端活动响应格式 */
export interface ActivityResponse {
  id: number;
  activityType: 'application' | 'interview' | 'report_submitted' | 'evaluation';
  description: string;
  refType: 'job' | 'application' | 'intern';
  refId: number;
  createdAt: string;
}

/** 前端活动类型（转换后） */
export interface ActivityItem {
  id: string;
  type: 'application' | 'interview' | 'weekly_report' | 'review';
  title: string;
  description: string;
  time: string;
  actor?: string;
  ref_type?: string;
  ref_id?: string;
}

// ── 导师仪表板 ──────────────────────────────────────────────────────────────

/** 后端导师仪表板响应格式 */
export interface MentorDashboardResponse {
  assignedInternCount: number;
  pendingReportCount: number;
  pendingCodeReviewCount: number;
  recentActivities: ActivityResponse[];
}

export interface MentorStudent {
  student_id: string;
  student_name: string;
  type: 'training' | 'internship';
  project_name?: string;
  position?: string;
  start_date: string;
}

export interface MentorDashboard {
  pending_code_reviews: number;
  pending_weekly_reports: number;
  upcoming_interviews: number;
  students?: MentorStudent[];
}

// ── 分析数据 ────────────────────────────────────────────────────────────────

export interface AnalyticsData {
  conversion_rate?: {
    internship_to_fulltime: number;
    cost_saving: number;
  };
  conversion_trend?: { month: string; rate: number }[];
  contribution?: {
    total_value: number;
    by_department: { department: string; value: number }[];
  };
  recruitment_funnel?: { stage: string; count: number }[];
  /** 后端实际返回的分析数据 */
  applicationTrends?: { period: string; count: number }[];
  internPerformance?: {
    averageScore: number;
    totalInterns: number;
    evaluatedInterns: number;
  };
  projectCompletionRate?: number;
  mentorSatisfaction?: number;
}

// ── 企业档案类型 ────────────────────────────────────────────────────────────

export interface EnterpriseProfile {
  id?: number;
  enterpriseName: string;
  industry?: string;
  scale?: string;
  province?: string;
  city?: string;
  address?: string;
  logoUrl?: string;
  website?: string;
  description?: string;
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
}

// ── 导航类型 ────────────────────────────────────────────────────────────────

export interface NavItem {
  title: string;
  href: string;
  icon: string;
  roles: EnterpriseRole[];
  description?: string;
}

// ── 分页类型 ────────────────────────────────────────────────────────────────

export interface PageResult<T> {
  total: number;
  records: T[];
  page: number;
  size: number;
}
