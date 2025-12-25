// f:\projects\zhitu\frontend\src\student\dashboard\page.tsx
import { useEffect, useState } from 'react';
import {
  fetchDashboardStats,
  fetchRadarData,
  fetchTasks,
  fetchRecommendations
} from '../services/api';
import type {
  DashboardStats,
  RadarData,
  TaskResponse,
  RecommendationItem
} from '../mock/generator';
import StatsCards from './components/StatsCards';
import CapabilityRadar from './components/CapabilityRadar';
import TaskList from './components/TaskList';
import RecommendationFeed from './components/RecommendationFeed';
import { Button } from "@/components/ui/button";
import { RefreshCw } from "lucide-react";

const StudentDashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [radarData, setRadarData] = useState<RadarData | null>(null);
  const [tasks, setTasks] = useState<TaskResponse | null>(null);
  const [recommendations, setRecommendations] = useState<RecommendationItem[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      // Parallel data fetching
      const [statsData, radar, taskData, recs] = await Promise.all([
        fetchDashboardStats(),
        fetchRadarData(),
        fetchTasks(),
        fetchRecommendations()
      ]);

      setStats(statsData);
      setRadarData(radar);
      setTasks(taskData);
      setRecommendations(recs);
    } catch (error) {
      console.error("Failed to load dashboard data", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">学生成长工作台</h1>
          <p className="text-muted-foreground">My Career DNA - 全景数字档案与能力画像</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={loadData} disabled={loading}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            刷新数据
          </Button>
        </div>
      </div>

      {/* 1. 核心指标 */}
      <section>
        <StatsCards stats={stats} loading={loading} />
      </section>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* 2. 能力雷达 */}
        <section className="col-span-1">
          <CapabilityRadar data={radarData} loading={loading} />
        </section>

        {/* 3. 任务指挥中心 */}
        <section className="col-span-1">
          <TaskList tasks={tasks?.records || []} loading={loading} />
        </section>

        {/* 4. 个性化推荐 */}
        <section className="col-span-1">
          <RecommendationFeed recommendations={recommendations} loading={loading} />
        </section>
      </div>
    </div>
  );
};

export default StudentDashboard;
