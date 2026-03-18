import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, Database, Layers, Briefcase,
  GraduationCap, TrendingUp, Building2, LogOut, type LucideIcon,
} from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { getMockCurrentUser } from './utils/auth';
import { EnterpriseRole } from './types';
import { useAuth } from '../auth/context';

interface LocalNavItem {
  title: string;
  href: string;
  icon: LucideIcon;
  roles: EnterpriseRole[];
}

const { HR, MENTOR, ADMIN } = EnterpriseRole;

const allNavItems: LocalNavItem[] = [
  { title: '工作台', href: '/enterprise/dashboard', icon: LayoutDashboard, roles: [HR, MENTOR, ADMIN] },
  { title: '招聘管理', href: '/enterprise/recruitment', icon: Users, roles: [HR, ADMIN] },
  { title: '人才蓄水池', href: '/enterprise/talent-pool', icon: Database, roles: [HR, ADMIN] },
  { title: '实训项目', href: '/enterprise/training', icon: Layers, roles: [MENTOR, ADMIN] },
  { title: '实习管理', href: '/enterprise/internship', icon: Briefcase, roles: [HR, ADMIN] },
  { title: '导师工作站', href: '/enterprise/mentor', icon: GraduationCap, roles: [MENTOR, ADMIN] },
  { title: '效益分析', href: '/enterprise/analytics', icon: TrendingUp, roles: [HR, ADMIN] },
];

const roleLabelMap: Record<EnterpriseRole, { label: string; className: string }> = {
  [HR]: { label: 'HR', className: 'border-transparent bg-blue-500 text-white' },
  [MENTOR]: { label: '企业导师', className: 'border-transparent bg-green-500 text-white' },
  [ADMIN]: { label: '企业管理员', className: 'border-transparent bg-red-500 text-white' },
};

const EnterpriseLayout = () => {
  const currentUser = getMockCurrentUser();
  const navItems = allNavItems.filter(item => item.roles.includes(currentUser.role));
  const roleInfo = roleLabelMap[currentUser.role];
  const initials = currentUser.name.slice(0, 2);
  const { logout } = useAuth();
  const navigate = useNavigate();
  const handleLogout = () => { logout(); navigate('/login', { replace: true }); };

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      <aside className="border-r bg-card hidden md:flex flex-col w-16 lg:w-64">
        <div className="h-16 flex items-center justify-center lg:justify-start lg:px-6 border-b">
          <div className="h-8 w-8 bg-blue-600 rounded-lg flex items-center justify-center shrink-0">
            <Building2 className="h-5 w-5 text-white" />
          </div>
          <span className="font-bold text-lg tracking-tight ml-2 hidden lg:block">智途·企业</span>
        </div>

        <nav className="flex-1 py-4 space-y-1 overflow-y-auto px-2 lg:px-4">
          {navItems.map(item => (
            <NavLink
              key={item.href}
              to={item.href}
              className={({ isActive }) =>
                `flex items-center justify-center lg:justify-start px-2 lg:px-4 py-3 text-sm font-medium rounded-md transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                }`
              }
            >
              <item.icon className="h-5 w-5 lg:mr-3 shrink-0" />
              <span className="hidden lg:block">{item.title}</span>
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t flex justify-center lg:justify-start">
          <div className="flex items-center gap-3 min-w-0 w-full">
            <div className="h-8 w-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
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

      <main className="flex-1 overflow-auto bg-gray-50/50 dark:bg-background">
        <Outlet />
      </main>
    </div>
  );
};

export default EnterpriseLayout;
