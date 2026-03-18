import type { FC } from 'react';
import { Link } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Building2, Code, LayoutDashboard, Activity, ChevronRight } from "lucide-react";

interface QuickAction {
  title: string;
  description: string;
  href: string;
  icon: React.ElementType;
  color: string;
  bg: string;
}

const actions: QuickAction[] = [
  {
    title: "审核企业入驻",
    description: "审核企业资质，管理入驻申请",
    href: "/platform/audit",
    icon: Building2,
    color: "text-blue-500",
    bg: "bg-blue-50",
  },
  {
    title: "审核实训项目",
    description: "审核项目内容，评定质量等级",
    href: "/platform/audit",
    icon: Code,
    color: "text-purple-500",
    bg: "bg-purple-50",
  },
  {
    title: "管理推荐位",
    description: "配置首页 Banner 和推荐榜单",
    href: "/platform/resources",
    icon: LayoutDashboard,
    color: "text-green-500",
    bg: "bg-green-50",
  },
  {
    title: "查看系统监控",
    description: "实时监控系统健康状态",
    href: "/platform/monitor",
    icon: Activity,
    color: "text-orange-500",
    bg: "bg-orange-50",
  },
];

const QuickActions: FC = () => {
  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle className="text-base font-semibold">快捷操作</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {actions.map((action) => {
          const Icon = action.icon;
          return (
            <Link
              key={action.title}
              to={action.href}
              className="flex items-center gap-3 p-3 rounded-lg border hover:bg-accent transition-colors group"
            >
              <div className={`p-2 rounded-md ${action.bg} shrink-0`}>
                <Icon className={`h-4 w-4 ${action.color}`} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium leading-none mb-1">{action.title}</p>
                <p className="text-xs text-muted-foreground truncate">{action.description}</p>
              </div>
              <ChevronRight className="h-4 w-4 text-muted-foreground shrink-0 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          );
        })}
      </CardContent>
    </Card>
  );
};

export default QuickActions;
