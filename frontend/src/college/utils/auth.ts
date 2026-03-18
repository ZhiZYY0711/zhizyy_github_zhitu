import { CollegeRole, type CollegeRoleType, type CollegeUser } from '../types';

const MOCK_USERS: Record<CollegeRoleType, CollegeUser> = {
  [CollegeRole.COUNSELOR]: {
    id: 't_1001',
    name: '李辅导员',
    role: CollegeRole.COUNSELOR,
    college_id: 'col_001',
    department_id: 'dept_001',
    title: '讲师',
  },
  [CollegeRole.DEAN]: {
    id: 't_1002',
    name: '王系主任',
    role: CollegeRole.DEAN,
    college_id: 'col_001',
    department_id: 'dept_001',
    title: '教授',
  },
  [CollegeRole.ADMIN]: {
    id: 't_1003',
    name: '张教务',
    role: CollegeRole.ADMIN,
    college_id: 'col_001',
    department_id: 'dept_002',
    title: '教务主任',
  },
};

export function getMockCurrentUser(): CollegeUser {
  const stored = localStorage.getItem('college_mock_role') as CollegeRoleType | null;
  const role = stored && Object.values(CollegeRole).includes(stored as any) ? stored : CollegeRole.COUNSELOR;
  return MOCK_USERS[role];
}

export function setMockRole(role: CollegeRoleType): void {
  localStorage.setItem('college_mock_role', role);
}
