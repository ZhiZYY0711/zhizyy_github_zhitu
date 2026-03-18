import { useEffect, useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  PieChart, Pie, Cell, ResponsiveContainer,
} from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Users, TrendingUp, Briefcase, DollarSign } from 'lucide-react';
import { fetchEmploymentStats, fetchTrends } from '../services/api';
import type { EmploymentStats, TrendData } from '../types';

const COLORS = ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6'];

const StatCard = ({ title, value, sub, icon: Icon, color }: {
  title: string; value: string; sub: string;
  icon: React.ElementType; color: string;
}) => (
  <Card>
    <CardContent className="p-6 flex items-center gap-4">
      <div className={`h-12 w-12 rounded-full flex items-center justify-center ${color}`}>
        <Icon className="h-6 w-6 text-white" />
      </div>
      <div>
        <p className="text-sm text-muted-foreground">{title}</p>
        <p className="text-2xl font-bold">{value}</p>
        <p className="text-xs text-muted-foreground">{sub}</p>
      </div>
    </CardContent>
  </Card>
);

const CollegeDashboard = () => {
  const [stats, setStats] = useState<EmploymentStats | null>(null);
  const [trends, setTrends] = useState<TrendData | null>(null);

  useEffect(() => {
    fetchEmploymentStats().then(setStats);
    fetchTrends().then(setTrends);
  }, []);

  const trendChartData = trends
    ? trends.labels.map((label, i) => ({
        name: label,
        实习率: Math.round(trends.series[0].data[i] * 100),
        三方签约率: Math.round(trends.series[1].data[i] * 100),
      }))
    : [];

  const industryData = stats?.top_industries.map(item => ({
    name: item.name,
    value: Math.round(item.ratio * 100),
  })) ?? [];

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold">管理驾驶舱</h1>
        <p className="text-muted-foreground text-sm mt-1">就业监测大屏 · 实时数据</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="毕业生总数"
          value={stats ? stats.total_graduates.toLocaleString() : '--'}
          sub="本届"
          icon={Users}
          color="bg-green-500"
        />
        <StatCard
          title="就业率"
          value={stats ? `${(stats.employment_rate * 100).toFixed(1)}%` : '--'}
          sub="已就业"
          icon={TrendingUp}
          color="bg-blue-500"
        />
        <StatCard
          title="实习率"
          value={stats ? `${(stats.internship_rate * 100).toFixed(1)}%` : '--'}
          sub="在实习"
          icon={Briefcase}
          color="bg-orange-500"
        />
        <StatCard
          title="平均薪资"
          value={stats ? `¥${stats.avg_salary.toLocaleString()}` : '--'}
          sub="元/月"
          icon={DollarSign}
          color="bg-purple-500"
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-base">就业趋势</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={trendChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis unit="%" tick={{ fontSize: 12 }} />
                <Tooltip formatter={(v) => `${v}%`} />
                <Legend />
                <Line type="monotone" dataKey="实习率" stroke="#22c55e" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="三方签约率" stroke="#3b82f6" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">就业行业分布</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={industryData} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({ name, value }) => `${name} ${value}%`} labelLine={false}>
                  {industryData.map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v) => `${v}%`} />
              </PieChart>
            </ResponsiveContainer>
            <div className="mt-2 space-y-1">
              {industryData.map((item, i) => (
                <div key={item.name} className="flex items-center gap-2 text-xs">
                  <span className="h-2 w-2 rounded-full shrink-0" style={{ background: COLORS[i % COLORS.length] }} />
                  <span className="text-muted-foreground">{item.name}</span>
                  <span className="ml-auto font-medium">{item.value}%</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Secondary stats */}
      {stats && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">补充指标</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">灵活就业率</p>
                <p className="text-lg font-semibold">{(stats.flexible_employment_rate * 100).toFixed(1)}%</p>
              </div>
              <div>
                <p className="text-muted-foreground">专业对口率</p>
                <p className="text-lg font-semibold">72.3%</p>
              </div>
              <div>
                <p className="text-muted-foreground">薪资中位数</p>
                <p className="text-lg font-semibold">¥8,000</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default CollegeDashboard;
