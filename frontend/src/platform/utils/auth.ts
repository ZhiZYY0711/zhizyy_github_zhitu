import { UserRole } from '../types';
import type { NavItem } from '../types';

export const filterNavItemsByRole = (items: NavItem[], role: UserRole): NavItem[] => {
  return items.filter(item => item.roles.includes(role));
};

export const hasRole = (userRole: UserRole, requiredRole: UserRole): boolean => {
  if (userRole === UserRole.SYSTEM_ADMIN) return true;
  return userRole === requiredRole;
};

export const hasPermission = (permissions: string[], permission: string): boolean => {
  return permissions.includes('system:all') || permissions.includes(permission);
};

// Mock current user for development
export const getMockCurrentUser = () => ({
  id: 'admin_001',
  username: 'admin',
  name: '系统管理员',
  role: UserRole.SYSTEM_ADMIN,
  permissions: ['system:all'],
});
