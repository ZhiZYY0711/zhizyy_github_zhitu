import { useEffect, useState } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { AlertTriangle, CheckCircle, Clock } from 'lucide-react';
import { fetchWarnings, fetchWarningStats, interveneWarning } from '../services/api';
import type { Warning, WarningStats } from '../types';

const levelMap: Record<Warning['level'], { label: string; className: string; icon: string }> = {
  high: { label: '红色预警', className: 'border-transparent bg-red-500 text-white', icon: '🔴' },
  medium: { label: '橙色预警', className: 'border-transparent bg-orange-500 text-white', icon: '🟠' },
  low: { label: '黄色预警', className: 'border-transparent bg-yellow-500 text-white', icon: '🟡' },
};

const typeMap: Record<Warning['type'], string> = {
  missing_report: '周报缺失',
  attendance_abnormal: '考勤异常',
  unemployed: '未就业',
};

const statusMap: Record<Warning['status'], { label: string; icon: React.ElementType; className: string }> = {
  unhandled: { label: '未处理', icon: AlertTriangle, className: 'text-red-500' },
  handling: { label: '处理中', icon: Clock, className: 'text-orange-500' },
  resolved: { label: '已解决', icon: CheckCircle, className: 'text-green-500' },
};

const WarningPage = () => {
  const [warnings, setWarnings] = useState<Warning[]>([]);
  const [stats, setStats] = useState<WarningStats | null>(null);
  const [levelFilter, setLevelFilter] = useState<string>('all');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchWarningStats().then(setStats);
    loadWarnings();
  }, []);

  const loadWarnings = (level?: string, status?: string) => {
    setLoading(true);
    const params: Record<string, string> = {};
    if (level && level !== 'all') params.level = level;
    if (status && status !== 'all') params.status = status;
    fetchWarnings(params).then(data => {
      setWarnings(data.records);
      setLoading(false);
    });
  };

  const handleLevelChange = (val: string) => {
    setLevelFilter(val);
    loadWarnings(val, statusFilter);
  };

  const handleStatusChange = (val: string) => {
    setStatusFilter(val);
    loadWarnings(levelFilter, val);
  };

  const handleIntervene = async (id: string) => {
    await interveneWarning(id, {
      method: 'phone_call',
      content: '已联系学生，了解情况',
      status: 'handling',
    });
    setWarnings(prev => prev.map(w => w.id === id ? { ...w, status: 'handling' } : w));
  };

  const filtered = warnings.filter(w => {
    if (levelFilter !== 'all' && w.level !== levelFilter) return false;
    if (statusFilter !== 'all' && w.status !== statusFilter) return false;
    return true;
  });

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold">预警指挥室</h1>
        <p className="text-muted-foreground text-sm mt-1">自动预警 · 干预处理 · 实时监控</p>
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-3 gap-4">
          <Card className="border-red-200">
            <CardContent className="p-4 text-center">
              <p className="text-3xl font-bold text-red-500">{stats.high_count}</p>
              <p className="text-sm text-muted-foreground mt-1">🔴 红色预警</p>
            </CardContent>
          </Card>
          <Card className="border-orange-200">
            <CardContent className="p-4 text-center">
              <p className="text-3xl font-bold text-orange-500">{stats.medium_count}</p>
              <p className="text-sm text-muted-foreground mt-1">🟠 橙色预警</p>
            </CardContent>
          </Card>
          <Card className="border-yellow-200">
            <CardContent className="p-4 text-center">
              <p className="text-3xl font-bold text-yellow-500">{stats.low_count}</p>
              <p className="text-sm text-muted-foreground mt-1">🟡 黄色预警</p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <Select value={levelFilter} onValueChange={handleLevelChange}>
          <SelectTrigger className="w-36">
            <SelectValue placeholder="预警级别" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部级别</SelectItem>
            <SelectItem value="high">红色预警</SelectItem>
            <SelectItem value="medium">橙色预警</SelectItem>
            <SelectItem value="low">黄色预警</SelectItem>
          </SelectContent>
        </Select>

        <Select value={statusFilter} onValueChange={handleStatusChange}>
          <SelectTrigger className="w-36">
            <SelectValue placeholder="处理状态" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="unhandled">未处理</SelectItem>
            <SelectItem value="handling">处理中</SelectItem>
            <SelectItem value="resolved">已解决</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Warning List */}
      <div className="space-y-3">
        {loading ? (
          <p className="text-center py-8 text-muted-foreground">加载中...</p>
        ) : filtered.length === 0 ? (
          <p className="text-center py-12 text-muted-foreground">暂无预警记录</p>
        ) : filtered.map(w => {
          const lv = levelMap[w.level];
          const st = statusMap[w.status];
          const StatusIcon = st.icon;
          return (
            <div key={w.id} className="border rounded-lg p-4 hover:bg-muted/30 transition-colors">
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-3 flex-1 min-w-0">
                  <span className="text-xl shrink-0">{lv.icon}</span>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-medium">{w.student_name}</span>
                      <Badge className={lv.className}>{lv.label}</Badge>
                      <span className="text-xs bg-muted px-2 py-0.5 rounded">{typeMap[w.type]}</span>
                    </div>
                    <p className="text-sm text-muted-foreground mt-1">{w.description}</p>
                    <div className="flex items-center gap-1 mt-1 text-xs text-muted-foreground">
                      <StatusIcon className={`h-3 w-3 ${st.className}`} />
                      <span className={st.className}>{st.label}</span>
                      <span className="ml-2">{w.trigger_time}</span>
                    </div>
                  </div>
                </div>
                {w.status === 'unhandled' && (
                  <Button
                    size="sm"
                    variant="outline"
                    className="shrink-0"
                    onClick={() => handleIntervene(w.id)}
                  >
                    立即干预
                  </Button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default WarningPage;
