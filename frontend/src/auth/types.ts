export type UserRole = 'student' | 'enterprise' | 'college' | 'platform';

export interface LoginCredentials {
  username: string;
  password: string;
  role: UserRole;
}

export interface AuthSession {
  username: string;
  role: UserRole;
  loginAt: number;
}

export interface LoginResult {
  success: boolean;
  error?: string;
}

export const ROLE_ROUTES: Record<UserRole, string> = {
  student: '/student/dashboard',
  enterprise: '/enterprise/dashboard',
  college: '/college/dashboard',
  platform: '/platform/dashboard',
};

export const ROLE_LABELS: Record<UserRole, string> = {
  student: '学生端',
  enterprise: '企业端',
  college: '高校端',
  platform: '平台端',
};
