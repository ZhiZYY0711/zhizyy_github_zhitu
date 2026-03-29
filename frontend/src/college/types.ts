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
  id: string;                    // Long -> string
  userId: string;                // Long -> string
  tenantId: string;              // Long -> string
  studentNo: string;
  realName: string;
  gender: number;                // Integer: 1-男，2-女
  phone: string | null;
  email: string | null;
  avatarUrl: string | null;
  collegeId: string | null;      // Long -> string
  majorId: string | null;        // Long -> string
  classId: string | null;        // Long -> string
  grade: string | null;
  enrollmentDate: string | null; // LocalDate -> string (ISO 8601)
  graduationDate: string | null; // LocalDate -> string (ISO 8601)
  resumeUrl: string | null;
  skills: string | null;
  createdAt: string;             // 继承自 BaseEntity
  updatedAt: string;             // 继承自 BaseEntity
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
  id: string;
  studentId: string;
  enterpriseId: string;
  jobId: string;
  mentorId: string;
  teacherId: string;
  startDate: string;
  endDate: string | null;
  status: number; // 1=实习中 2=已结束
  createdAt: string;
  updatedAt: string;
  studentName: string;
  studentNo: string;
  enterpriseName: string;
  jobTitle: string;
  mentorName: string;
  teacherName: string;
  lastReportTime: string | null;
  statusText: 'normal' | 'warning' | 'completed';
}

export interface Contract {
  id: string;
  studentName: string;    // camelCase
  companyName: string;    // camelCase
  position: string;
  submitTime: string;     // camelCase, ISO 8601 date string
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
