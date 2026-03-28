// f:\projects\zhitu\frontend\src\student\dashboard\components\StatsCards.tsx
import type { FC } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { GraduationCap, Clock, Briefcase, Award } from "lucide-react";
import type { DashboardStats } from "../../mock/generator";

interface StatsCardsProps {
  stats: DashboardStats | null | undefined;
  loading: boolean;
}

const StatsCards: FC<StatsCardsProps> = ({ stats, loading }) => {
  if (loading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <div className="h-4 w-24 bg-gray-200 rounded"></div>
            </CardHeader>
            <CardContent>
              <div className="h-8 w-16 bg-gray-200 rounded"></div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  if (!stats) return null;

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <Card className="border-l-4 border-l-blue-500">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">实训项目</CardTitle>
          <Clock className="h-4 w-4 text-blue-500" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{stats.training_project_count || 0}</div>
          <p className="text-xs text-muted-foreground">
            当前参与的项目总数
          </p>
        </CardContent>
      </Card>
      <Card className="border-l-4 border-l-indigo-500">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">实习岗位</CardTitle>
          <Briefcase className="h-4 w-4 text-indigo-500" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{stats.internship_job_count || 0}</div>
          <p className="text-xs text-muted-foreground">
            匹配的优质岗位推荐
          </p>
        </CardContent>
      </Card>
      <Card className="border-l-4 border-l-orange-500">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">待办任务</CardTitle>
          <GraduationCap className="h-4 w-4 text-orange-500" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{stats.pending_tasks_count || 0}</div>
          <p className="text-xs text-muted-foreground">
            需要尽快处理的任务
          </p>
        </CardContent>
      </Card>
      <Card className="border-l-4 border-l-rose-500">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">成长总分</CardTitle>
          <Award className="h-4 w-4 text-rose-500" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{stats.growth_score || 0}</div>
          <p className="text-xs text-muted-foreground">
            基于综合表现的能力评估
          </p>
        </CardContent>
      </Card>
    </div>
  );
};

export default StatsCards;
