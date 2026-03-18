import type { FC } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Users, GraduationCap, Building2, Code, Briefcase, ClipboardCheck } from "lucide-react";
import type { DashboardStats } from "../../types";

interface StatsCardsProps {
  stats: DashboardStats | null;
  loading: boolean;
}

const StatsCards: FC<StatsCardsProps> = ({ stats, loading }) => {
  if (loading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        {Array.from({ length: 6 }).map((_, i) => (
          <Card key={i}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <Skeleton className="h-4 w-20" />
              <Skeleton className="h-4 w-4 rounded" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-16 mb-1" />
              <Skeleton className="h-3 w-24" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  if (!stats) return null;

  const cards = [
    {
      title: "总用户数",
      value: stats.total_users.toLocaleString(),
      desc: "平台注册用户",
      icon: Users,
      color: "text-blue-500",
      bg: "bg-blue-50",
      warn: false,
    },
    {
      title: "高校数量",
      value: stats.total_colleges.toLocaleString(),
      desc: "已入驻高校",
      icon: GraduationCap,
      color: "text-purple-500",
      bg: "bg-purple-50",
      warn: false,
    },
    {
      title: "企业数量",
      value: stats.total_enterprises.toLocaleString(),
      desc: "已认证企业",
      icon: Building2,
      color: "text-green-500",
      bg: "bg-green-50",
      warn: false,
    },
    {
      title: "活跃项目",
      value: stats.active_projects.toLocaleString(),
      desc: "进行中实训项目",
      icon: Code,
      color: "text-orange-500",
      bg: "bg-orange-50",
      warn: false,
    },
    {
      title: "实习岗位",
      value: stats.internship_positions.toLocaleString(),
      desc: "开放实习岗位",
      icon: Briefcase,
      color: "text-cyan-500",
      bg: "bg-cyan-50",
      warn: false,
    },
    {
      title: "待审核",
      value: stats.pending_audits.toLocaleString(),
      desc: "需要处理的审核",
      icon: ClipboardCheck,
      color: stats.pending_audits > 10 ? "text-red-500" : "text-red-400",
      bg: stats.pending_audits > 10 ? "bg-red-50" : "bg-red-50",
      warn: stats.pending_audits > 10,
    },
  ];

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <Card key={card.title} className={card.warn ? "border-red-200" : ""}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">{card.title}</CardTitle>
              <div className={`p-1.5 rounded-md ${card.bg}`}>
                <Icon className={`h-4 w-4 ${card.color}`} />
              </div>
            </CardHeader>
            <CardContent>
              <div className={`text-2xl font-bold ${card.warn ? "text-red-600" : ""}`}>
                {card.value}
              </div>
              <p className="text-xs text-muted-foreground mt-1">{card.desc}</p>
              {card.warn && (
                <p className="text-xs text-red-500 mt-1 font-medium">⚠ 需要及时处理</p>
              )}
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
};

export default StatsCards;
