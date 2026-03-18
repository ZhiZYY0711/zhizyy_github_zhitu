import { useEffect, useState } from 'react';
import { Button } from "@/components/ui/button";
import { RefreshCw } from "lucide-react";
import { fetchDashboardStats, fetchOnlineUserTrend } from '../services/api';
import type { DashboardStats, OnlineUserTrend } from '../types';
import StatsCards from './components/StatsCards';
import TrendChart from './components/TrendChart';
import QuickActions from './components/QuickActions';

const PlatformDashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [trend, setTrend] = useState<OnlineUserTrend | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const [statsData, trendData] = await Promise.all([
        fetchDashboardStats(),
        fetchOnlineUserTrend(),
      ]);
      setStats(statsData);
      setTrend(trendData);
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
      {/* 页面标题 */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">平台运营中心</h1>
          <p className="text-muted-foreground">全局数据概览与快捷操作入口</p>
        </div>
        <Button variant="outline" size="sm" onClick={loadData} disabled={loading}>
          <RefreshCw className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          刷新数据
        </Button>
      </div>

      {/* 统计卡片 */}
      <section>
        <StatsCards stats={stats} loading={loading} />
      </section>

      {/* 趋势图 + 快捷操作 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <section className="lg:col-span-2">
          <TrendChart
            data={trend?.data ?? []}
            title="在线用户趋势"
            loading={loading}
          />
        </section>
        <section className="lg:col-span-1">
          <QuickActions />
        </section>
      </div>
    </div>
  );
};

export default PlatformDashboard;
