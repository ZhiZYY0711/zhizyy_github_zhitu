export const UserRole = {
  SYSTEM_ADMIN: 'system_admin',
  AUDIT_MANAGER: 'audit_manager',
  OPERATIONS_MANAGER: 'operations_manager',
  DEVOPS_MANAGER: 'devops_manager',
} as const;

export type UserRole = typeof UserRole[keyof typeof UserRole];

export interface User {
  id: string;
  username: string;
  name: string;
  role: UserRole;
  permissions: string[];
  avatar?: string;
}

export interface Tenant {
  id: string;
  name: string;
  type: 'college' | 'enterprise';
  status: 'active' | 'inactive' | 'pending';
  domain?: string;
  admin_username: string;
  admin_email: string;
  max_students?: number;
  expire_date?: string;
  created_at: string;
  updated_at: string;
}

export interface EnterpriseAuditItem {
  id: string;
  name: string;
  license_url: string;
  contact_person: string;
  contact_phone: string;
  apply_time: string;
  status: 'pending' | 'approved' | 'rejected';
  audit_time?: string;
  auditor?: string;
  reject_reason?: string;
}

export interface ProjectAuditItem {
  id: string;
  name: string;
  provider: string;
  provider_type: 'enterprise' | 'college';
  tech_stack: string[];
  description: string;
  difficulty: number;
  apply_time: string;
  status: 'pending' | 'approved' | 'rejected';
  quality_rating?: 'S' | 'A' | 'B' | 'C';
  audit_comment?: string;
}

export interface Tag {
  id: string;
  category: 'industry' | 'tech_stack' | 'skill';
  name: string;
  parent_id?: string;
  order: number;
  created_at: string;
}

export interface SkillTreeNode {
  id: string;
  name: string;
  level: number;
  parent_id?: string;
  children?: SkillTreeNode[];
  description?: string;
}

export interface CertificateTemplate {
  id: string;
  name: string;
  background_url: string;
  elements_layout: Record<string, { x: number; y: number; fontSize: number }>;
  created_at: string;
  updated_at: string;
}

export interface ContractTemplate {
  id: string;
  name: string;
  version: string;
  content: string;
  variables: string[];
  status: 'draft' | 'active' | 'archived';
  created_at: string;
}

export interface RecommendationBanner {
  id: string;
  target_type: 'project' | 'enterprise' | 'course';
  target_id: string;
  title: string;
  image_url: string;
  order: number;
  status: 'active' | 'inactive';
  start_date?: string;
  end_date?: string;
}

export interface TopListItem {
  id: string;
  list_type: 'mentor' | 'course' | 'project';
  item_id: string;
  item_name: string;
  order: number;
  reason?: string;
}

export interface SystemHealth {
  cpu_usage: number;
  memory_usage: number;
  disk_usage: number;
  active_services: number;
  error_rate: number;
  online_users: number;
  timestamp: string;
}

export interface ServiceStatus {
  name: string;
  status: 'healthy' | 'degraded' | 'down';
  response_time: number;
  last_check: string;
}

export interface OnlineUserTrend {
  data: { time: string; count: number }[];
  period: '1h' | '24h' | '7d' | '30d';
}

export interface OperationLog {
  id: string;
  user_id: string;
  user_name: string;
  action: string;
  module: string;
  details?: Record<string, unknown>;
  ip: string;
  user_agent?: string;
  time: string;
  result: 'success' | 'fail';
  error_message?: string;
}

export interface SecurityLog {
  id: string;
  event_type: 'login_fail' | 'abnormal_access' | 'permission_denied' | 'data_breach';
  level: 'low' | 'medium' | 'high' | 'critical';
  description: string;
  user_id?: string;
  ip: string;
  time: string;
  handled: boolean;
}

export interface DashboardStats {
  total_users: number;
  total_colleges: number;
  total_enterprises: number;
  active_projects: number;
  internship_positions: number;
  pending_audits: number;
  system_health_score: number;
}

export interface NavItem {
  title: string;
  href: string;
  icon: string;
  roles: UserRole[];
  description?: string;
}
