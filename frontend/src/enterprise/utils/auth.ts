import { EnterpriseRole, type EnterpriseUser, type NavItem } from '../types';

export const filterNavItemsByRole = (items: NavItem[], role: EnterpriseRole): NavItem[] => {
  if (role === EnterpriseRole.ADMIN) return items;
  return items.filter(item => item.roles.includes(role));
};

export const hasRole = (userRole: EnterpriseRole, requiredRole: EnterpriseRole): boolean => {
  if (userRole === EnterpriseRole.ADMIN) return true;
  return userRole === requiredRole;
};

export const getMockCurrentUser = (): EnterpriseUser => ({
  id: 'ent_user_001',
  name: '张经理',
  role: EnterpriseRole.ADMIN,
  enterprise_id: 'ent_001',
  enterprise_name: '字节跳动科技有限公司',
});
