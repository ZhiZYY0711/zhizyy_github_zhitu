export const CollegeRole = {
  COUNSELOR: 'counselor', // 辅导员
  DEAN: 'dean',           // 系主任
  ADMIN: 'admin',         // 教务
} as const;

export type CollegeRoleType = typeof CollegeRole[keyof typeof CollegeRole];

export interface CollegeUser {
  id: string;
  name: string;
  role: CollegeRoleType;
  college_id: string;
  department_id: string;
  title?: string;
  avatar?: string;
}

export interface Student {
  id: string;
  student_no: string;
  name: string;
  gender: 'male' | 'female';
  class_name: string;
  gpa: number;
  phone: string;
  status: 'active' | 'suspended' | 'graduated';
}

export interface TrainingPlan {
  id: string;
  course_name: string;
  start_date: string;
  end_date: string;
  target_majors: string[];
  status: 'draft' | 'published' | 'ongoing' | 'closed';
  description?: string;
  credits?: number;
}

export interface InternshipStudent {
  student_name: string;
  company: string;
  position: string;
  start_date: string;
  mentor_name: string;
  last_report_time: string;
  status: 'normal' | 'warning';
}

export interface Contract {
  id: string;
  student_name: string;
  company_name: string;
  position: string;
  submit_time: string;
  status: 'pending' | 'approved' | 'rejected';
}

export interface CooperativeEnterprise {
  id: string;
  name: string;
  industry: string;
  level: 'strategic' | 'core' | 'normal';
  contact_person: string;
  phone: string;
  active_interns: number;
  total_hired: number;
  status: 'active' | 'inactive';
}

export interface CrmAudit {
  id: string;
  enterprise_name: string;
  industry: string;
  contact_person: string;
  submit_time: string;
  status: 'pending' | 'approved' | 'rejected';
}

export interface VisitRecord {
  id: string;
  enterprise_id: string;
  enterprise_name: string;
  visit_date: string;
  visitors: string[];
  content: string;
}

export interface Warning {
  id: string;
  student_id: string;
  student_name: string;
  type: 'missing_report' | 'attendance_abnormal' | 'unemployed';
  level: 'high' | 'medium' | 'low';
  description: string;
  trigger_time: string;
  status: 'unhandled' | 'handling' | 'resolved';
}

export interface WarningStats {
  high_count: number;
  medium_count: number;
  low_count: number;
  type_distribution: {
    missing_report: number;
    attendance: number;
    safety: number;
  };
}

export interface EmploymentStats {
  total_graduates: number;
  employment_rate: number;
  internship_rate: number;
  flexible_employment_rate: number;
  avg_salary: number;
  top_industries: { name: string; ratio: number }[];
}

export interface TrendData {
  labels: string[];
  series: { name: string; data: number[] }[];
}

export interface NavItem {
  title: string;
  href: string;
  icon: string;
  roles: CollegeRoleType[];
}
