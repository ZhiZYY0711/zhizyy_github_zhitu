import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Code, Briefcase, Trophy, GraduationCap, LogOut } from 'lucide-react';
import { useAuth } from '../auth/context';

const StudentLayout = () => {
  const { logout, session } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };
  const navItems = [
    {
      title: "成长工作台",
      href: "/student/dashboard",
      icon: LayoutDashboard,
      description: "My Career DNA"
    },
    {
      title: "智能实训",
      href: "/student/training",
      icon: Code,
      description: "Smart Training"
    },
    {
      title: "实习全流程",
      href: "/student/internship",
      icon: Briefcase,
      description: "Internship Journey"
    },
    {
      title: "职业成长",
      href: "/student/growth",
      icon: Trophy,
      description: "Growth & Evaluation"
    }
  ];

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      {/* Sidebar */}
      <aside className="border-r bg-card hidden md:flex flex-col transition-all duration-300 w-16 lg:w-64">
        <div className="h-16 flex items-center justify-center lg:justify-start lg:px-6 border-b">
          <div className="h-8 w-8 bg-primary rounded-lg flex items-center justify-center shrink-0">
            <GraduationCap className="h-5 w-5 text-primary-foreground" />
          </div>
          <span className="font-bold text-lg tracking-tight ml-2 hidden lg:block">智途·职通</span>
        </div>

        <nav className="flex-1 py-4 space-y-2 overflow-y-auto px-2 lg:px-4">
          {navItems.map((item) => (
            <NavLink
              key={item.href}
              to={item.href}
              className={({ isActive }) =>
                `flex items-center justify-center lg:justify-start px-2 lg:px-4 py-3 text-sm font-medium rounded-md transition-colors ${isActive
                  ? "bg-primary text-primary-foreground shadow-sm"
                  : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
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
          <div className="flex items-center gap-3 w-full min-w-0">
            <div className="h-8 w-8 rounded-full bg-secondary flex items-center justify-center text-xs font-bold shrink-0">
              {session?.username?.slice(0, 2).toUpperCase() ?? 'ST'}
            </div>
            <div className="flex-col hidden lg:flex min-w-0 flex-1">
              <span className="text-sm font-medium truncate">{session?.username ?? 'Student User'}</span>
              <span className="text-xs text-muted-foreground">学生端</span>
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

export default StudentLayout;
