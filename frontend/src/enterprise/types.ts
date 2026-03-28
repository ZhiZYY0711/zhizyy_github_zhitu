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
}

export interface Application {
  application_id: string;
  job_id: string;
  job_title: string;
  student_id: string;
  student_name: string;
  school: string;
  major: string;
  resume_url: string;
  match_score: number; // 0-1
  apply_time: string;
  status: 'pending' | 'interview' | 'offered' | 'rejected' | 'withdrawn';
  interview_time?: string;
  interview_type?: 'online' | 'onsite';
  interview_link?: string;
}

export interface TalentPoolItem {
  id: string;
  student_id: string;
  student_name: string;
  school: string;
  major: string;
  tags: string[];
  skills: string[];
  collect_time: string;
  notes?: string;
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

export interface Intern {
  id: string;
  student_id: string;
  name: string;
  school: string;
  major: string;
  position: string;
  start_date: string;
  end_date?: string;
  mentor_id: string;
  mentor_name: string;
  status: 'pending' | 'active' | 'completed' | 'terminated';
  contract_status: 'pending' | 'signed' | 'rejected';
  salary?: string;
}

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

export interface DashboardStats {
  activeJobCount: number;
  pendingApplicationCount: number;
  activeInternCount: number;
  trainingProjectCount: number;
}

export interface TodoItem {
  id: string;
  type: 'interview' | 'weekly_report' | 'code_review' | 'contract';
  title: string;
  description: string;
  due_time?: string;
  priority: 'high' | 'medium' | 'low';
}

export interface ActivityItem {
  id: string;
  type: 'application' | 'interview' | 'weekly_report' | 'review';
  title: string;
  description: string;
  time: string;
  actor?: string;
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
  students: MentorStudent[];
}

export interface AnalyticsData {
  conversion_rate: {
    internship_to_fulltime: number;
    cost_saving: number;
  };
  conversion_trend: { month: string; rate: number }[];
  contribution: {
    total_value: number;
    by_department: { department: string; value: number }[];
  };
  recruitment_funnel: { stage: string; count: number }[];
}

export interface NavItem {
  title: string;
  href: string;
  icon: string;
  roles: EnterpriseRole[];
  description?: string;
}
