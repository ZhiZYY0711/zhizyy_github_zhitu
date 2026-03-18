export type UserRole = 'student' | 'enterprise' | 'college' | 'platform';

// 企业端子角色
export type EnterpriseSubRole = 'hr' | 'mentor' | 'admin';
// 高校端子角色
export type CollegeSubRole = 'counselor' | 'dean' | 'admin';

export interface LoginCredentials {
  username: string;
  password: string;
  role: UserRole;
}

// 登录成功后服务端返回的用户信息（开发阶段 mock，后续对接真实接口）
export interface UserInfo {
  id: string;           // 统一用字符串，避免 JS 大数精度问题
  name: string;
  role: UserRole;
  sub_role?: EnterpriseSubRole | CollegeSubRole; // 企业/高校端子角色
  avatar?: string;
  // 学生端扩展
  college_id?: string;
  major_id?: string;
  // 企业端扩展
  enterprise_id?: string;
  // 高校端扩展
  department_id?: string;
}

export interface AuthSession {
  username: string;
  role: UserRole;
  loginAt: number;       // Unix timestamp (ms)
  // Token 字段（开发阶段为空，对接后端后填充）
  accessToken?: string;
  refreshToken?: string;
  expiresAt?: number;    // Unix timestamp (ms)，token 过期时间
  userInfo?: UserInfo;
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

/** 判断 token 是否已过期（留 30s 缓冲） */
export function isTokenExpired(session: AuthSession): boolean {
  if (!session.expiresAt) return false; // 开发阶段无 token，不过期
  return Date.now() > session.expiresAt - 30_000;
}
