import { useState, useEffect } from 'react';
import { Briefcase, Users, UserCheck, ClipboardList, Calendar, FileText, Code2, Plus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useNavigate } from 'react-router-dom';
import { fetchDashboardStats, fetchTodos, fetchActivities } from '../services/api';
import type { DashboardStats, TodoItem, ActivityItem } from '../types';

const TREND_DATA = [
  { week: '第1周', applications: 12, interns: 3 },
  { week: '第2周', applications: 18, interns: 4 },
  { week: '第3周', applications: 15, interns: 5 },
  { week: '第4周', applications: 24, interns: 6 },
  { week: '第5周', applications: 20, interns: 7 },
  { week: '第6周', applications: 28, interns: 8 },
];

const PRIORITY_CONFIG: Record<string | number, {label: string, className: string}> = {
  high: { label: '紧急', className: 'bg-red-100 text-red-700 border-red-200' },
  medium: { label: '普通', className: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
  low: { label: '低优先', className: 'bg-gray-100 text-gray-600 border-gray-200' },
  3: { label: '紧急', className: 'bg-red-100 text-red-700 border-red-200' },
  2: { label: '普通', className: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
  1: { label: '低优先', className: 'bg-gray-100 text-gray-600 border-gray-200' },
};

const TODO_ICON: Record<TodoItem['type'], React.ReactNode> = {
  interview: <Calendar className="h-4 w-4 text-blue-500" />,
  weekly_report: <FileText className="h-4 w-4 text-green-500" />,
  code_review: <Code2 className="h-4 w-4 text-purple-500" />,
  contract: <ClipboardList className="h-4 w-4 text-orange-500" />,
};

const ACTIVITY_COLORS: Record<string, string> = {
  application: 'bg-blue-500',
  interview: 'bg-green-500',
  weekly_report: 'bg-yellow-500',
  review: 'bg-purple-500',
  report_submitted: 'bg-yellow-500',
  evaluation: 'bg-purple-500',
};

export default function DashboardPage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [todos, setTodos] = useState<TodoItem[]>([]);
  const [activities, setActivities] = useState<ActivityItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([fetchDashboardStats(), fetchTodos(), fetchActivities()])
      .then(([s, t, a]) => { 
        setStats(s); 
        setTodos(Array.isArray(t) ? t : (t as any).records || []); 
        setActivities(Array.isArray(a) ? a : (a as any).records || []); 
      })
      .finally(() => setLoading(false));
  }, []);

  const statCards = stats ? [
    { title: '在招职位', value: stats.activeJobCount || 0, icon: Briefcase, color: 'text-blue-600', bg: 'bg-blue-50' },
    { title: '待处理简历', value: stats.pendingApplicationCount || 0, icon: Users, color: 'text-orange-600', bg: 'bg-orange-50' },
    { title: '在岗实习生', value: stats.activeInternCount || 0, icon: UserCheck, color: 'text-green-600', bg: 'bg-green-50' },
    { title: '实训项目', value: stats.trainingProjectCount || 0, icon: ClipboardList, color: 'text-purple-600', bg: 'bg-purple-50' },
  ] : [];

  const todosByType = {
    interview: todos.filter(t => t.type === 'interview' || (t as any).todoType === 'interview_schedule'),
    weekly_report: todos.filter(t => t.type === 'weekly_report' || (t as any).todoType === 'report_review'),
    code_review: todos.filter(t => t.type === 'code_review' || (t as any).todoType === 'application_review'),
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">企业人才工作台</h1>
          <p className="text-muted-foreground mt-1">欢迎回来，今日有 {todos.filter(t => t.priority === 'high').length} 项紧急待办</p>
        </div>
        <Button onClick={() => navigate('/enterprise/recruitment')} className="bg-blue-600 hover:bg-blue-700">
          <Plus className="h-4 w-4 mr-2" />
          发布职位
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {loading ? Array.from({ length: 4 }).map((_, i) => (
          <Card key={i}><CardContent className="p-6"><div className="h-16 bg-muted animate-pulse rounded" /></CardContent></Card>
        )) : statCards.map(card => (
          <Card key={card.title}>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">{card.title}</p>
                  <p className="text-3xl font-bold mt-1">{card.value}</p>
                </div>
                <div className={`h-12 w-12 rounded-full ${card.bg} flex items-center justify-center`}>
                  <card.icon className={`h-6 w-6 ${card.color}`} />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Todos */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">待办事项</CardTitle>
            </CardHeader>
            <CardContent>
              <Tabs defaultValue="interview">
                <TabsList className="mb-4">
                  <TabsTrigger value="interview">待面试 ({todosByType.interview.length})</TabsTrigger>
                  <TabsTrigger value="weekly_report">待批阅 ({todosByType.weekly_report.length})</TabsTrigger>
                  <TabsTrigger value="code_review">待评审 ({todosByType.code_review.length})</TabsTrigger>
                </TabsList>
                {(['interview', 'weekly_report', 'code_review'] as const).map(type => (
                  <TabsContent key={type} value={type} className="space-y-2">
                    {todosByType[type].length === 0 ? (
                      <p className="text-center text-muted-foreground py-6 text-sm">暂无待办</p>
                    ) : todosByType[type].map(todo => (
                      <div key={todo.id} className="flex items-start gap-3 p-3 rounded-lg border hover:bg-muted/30 transition-colors">
                        <div className="mt-0.5">{TODO_ICON[todo.type]}</div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium truncate">{todo.title}</p>
                          <p className="text-xs text-muted-foreground mt-0.5">{todo.description || '暂无描述'}</p>
                          {(todo.due_time || (todo as any).dueDate) && <p className="text-xs text-blue-600 mt-1">{todo.due_time || (todo as any).dueDate}</p>}
                        </div>
                        <Badge className={PRIORITY_CONFIG[todo.priority]?.className || 'bg-gray-100 text-gray-600 border-gray-200'}>
                          {PRIORITY_CONFIG[todo.priority]?.label || '普通'}
                        </Badge>
                      </div>
                    ))}
                  </TabsContent>
                ))}
              </Tabs>
            </CardContent>
          </Card>
        </div>

        {/* Activity Timeline */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">最近动态</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {activities.map((act, i) => {
                const actType = (act as any).activityType || act.type;
                const actColor = ACTIVITY_COLORS[actType] || 'bg-gray-500';
                return (
                <div key={act.id} className="flex gap-3">
                  <div className="flex flex-col items-center">
                    <div className={`h-2.5 w-2.5 rounded-full mt-1.5 ${actColor}`} />
                    {i < activities.length - 1 && <div className="w-px flex-1 bg-border mt-1" />}
                  </div>
                  <div className="pb-4 min-w-0">
                    <p className="text-sm font-medium">{act.title || '系统通知'}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">{act.description}</p>
                    <p className="text-xs text-muted-foreground mt-1">{act.time || (act as any).createdAt}</p>
                  </div>
                </div>
              )})}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Trend Chart */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">近6周趋势</CardTitle>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={TREND_DATA}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="week" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Line type="monotone" dataKey="applications" stroke="#3b82f6" strokeWidth={2} name="简历投递" dot={false} />
              <Line type="monotone" dataKey="interns" stroke="#22c55e" strokeWidth={2} name="实习入职" dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </div>
  );
}
