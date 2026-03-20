import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Database,
  ClipboardCheck,
  Layers,
  Activity,
  FileText,
  Shield,
  LogOut,
  type LucideIcon,
} from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { getMockCurrentUser } from './utils/auth';
import { UserRole } from './types';
import { useAuth } from '../auth/context';

interface LocalNavItem {
  title: string;
  href: string;
  icon: LucideIcon;
  description: string;
  roles: UserRole[];
}

const { SYSTEM_ADMIN, AUDIT_MANAGER, OPERATIONS_MANAGER, DEVOPS_MANAGER } = UserRole;

const allNavItems: LocalNavItem[] = [
  { title: '数据概览', href: '/platform/dashboard', icon: LayoutDashboard, description: 'Overview', roles: [SYSTEM_ADMIN, OPERATIONS_MANAGER] },
  { title: '主数据管理', href: '/platform/master-data', icon: Database, description: 'Master Data', roles: [SYSTEM_ADMIN, OPERATIONS_MANAGER] },
  { title: '机构审核', href: '/platform/audit', icon: ClipboardCheck, description: 'Audit', roles: [SYSTEM_ADMIN, AUDIT_MANAGER] },
  { title: '资源调度', href: '/platform/resources', icon: Layers, description: 'Resources', roles: [SYSTEM_ADMIN, OPERATIONS_MANAGER] },
  { title: '系统监控', href: '/platform/monitor', icon: Activity, description: 'Monitor', roles: [SYSTEM_ADMIN, DEVOPS_MANAGER] },
  { title: '日志审计', href: '/platform/logs', icon: FileText, description: 'Logs', roles: [SYSTEM_ADMIN, DEVOPS_MANAGER] },
];

const roleLabelMap: Record<UserRole, { label: string; className: string }> = {
  [SYSTEM_ADMIN]: { label: '超级管理员', className: 'border-transparent bg-red-500 text-white' },
  [AUDIT_MANAGER]: { label: '审核管理员', className: 'border-transparent bg-blue-500 text-white' },
  [OPERATIONS_MANAGER]: { label: '运营管理员', className: 'border-transparent bg-green-500 text-white' },
  [DEVOPS_MANAGER]: { label: '运维管理员', className: 'border-transparent bg-orange-500 text-white' },
};

const PlatformLayout = () => {
  const currentUser = getMockCurrentUser();
  // filterNavItemsByRole expects NavItem with icon: string, so we filter manually using same logic
  const navItems = allNavItems.filter(item => item.roles.includes(currentUser.role));
  const roleInfo = roleLabelMap[currentUser.role];
  const initials = currentUser.name.slice(0, 2);
  const { logout } = useAuth();
  const navigate = useNavigate();
  const handleLogout = () => { logout(); navigate('/login', { replace: true }); };

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      {/* Sidebar */}
      <aside className="border-r bg-card hidden md:flex flex-col transition-all duration-300 w-16 lg:w-64">
        <div className="h-16 flex items-center justify-center lg:justify-start lg:px-6 border-b">
          <div className="h-8 w-8 bg-primary rounded-lg flex items-center justify-center shrink-0">
            <Shield className="h-5 w-5 text-primary-foreground" />
          </div>
          <span className="font-bold text-lg tracking-tight ml-2 hidden lg:block">智途·平台</span>
        </div>

        <nav className="flex-1 py-4 space-y-2 overflow-y-auto px-2 lg:px-4">
          {navItems.map((item) => (
            <NavLink
              key={item.href}
              to={item.href}
              className={({ isActive }) =>
                `flex items-center justify-center lg:justify-start px-2 lg:px-4 py-3 text-sm font-medium rounded-md transition-colors ${
                  isActive
                    ? 'bg-primary text-primary-foreground shadow-sm'
                    : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                }`
              }
            >
              <item.icon className="h-5 w-5 lg:mr-3 shrink-0" />
              <div className="flex-col items-start hidden lg:flex">
                <span>{item.title}</span>
              </div>
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t flex justify-center lg:justify-start">
          <div className="flex items-center gap-3 min-w-0 w-full">
            <div className="h-8 w-8 rounded-full bg-secondary flex items-center justify-center text-xs font-bold shrink-0">
              {initials}
            </div>
            <div className="flex-col hidden lg:flex min-w-0 flex-1">
              <span className="text-sm font-medium truncate">{currentUser.name}</span>
              <Badge className={roleInfo.className}>{roleInfo.label}</Badge>
            </div>
            <button onClick={handleLogout} className="hidden lg:flex items-center justify-center h-7 w-7 rounded hover:bg-accent text-muted-foreground hover:text-foreground transition-colors shrink-0" title="退出登录">
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto bg-gray-50/50 dark:bg-background">
        <Outlet />
      </main>
    </div>
  );
};

export default PlatformLayout;
