import type { FC } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Skeleton } from '@/components/ui/skeleton';
import type { SystemHealth } from '../../types';

interface HealthDashboardProps {
  health: SystemHealth | null;
  loading: boolean;
}

const MetricRow: FC<{ label: string; value: number; unit?: string; threshold: number; isPercent?: boolean }> = ({
  label, value, unit = '%', threshold, isPercent = true,
}) => {
  const over = value > threshold;
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-sm">
        <span className="text-muted-foreground">{label}</span>
        <span className={over ? 'text-red-600 font-semibold' : 'font-medium'}>
          {value.toFixed(1)}{unit}
        </span>
      </div>
      {isPercent && (
        <Progress
          value={value}
          indicatorClassName={over ? 'bg-red-500' : undefined}
        />
      )}
    </div>
  );
};

const HealthDashboard: FC<HealthDashboardProps> = ({ health, loading }) => {
  if (loading || !health) {
    return (
      <Card>
        <CardHeader><CardTitle>系统健康指标</CardTitle></CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-6 w-16" />
                <Skeleton className="h-2 w-full" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  const errorOver = health.error_rate > 1;

  return (
    <Card>
      <CardHeader><CardTitle>系统健康指标</CardTitle></CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
          <div className="space-y-2">
            <MetricRow label="CPU 使用率" value={health.cpu_usage} threshold={70} />
          </div>
          <div className="space-y-2">
            <MetricRow label="内存使用率" value={health.memory_usage} threshold={75} />
          </div>
          <div className="space-y-2">
            <MetricRow label="磁盘使用率" value={health.disk_usage} threshold={80} />
          </div>
          <div className="space-y-2">
            <p className="text-sm text-muted-foreground">在线用户数</p>
            <p className="text-2xl font-bold">{health.online_users}</p>
          </div>
          <div className="space-y-2">
            <p className="text-sm text-muted-foreground">活跃服务数</p>
            <p className="text-2xl font-bold">{health.active_services}</p>
          </div>
          <div className="space-y-2">
            <p className="text-sm text-muted-foreground">错误率</p>
            <p className={`text-2xl font-bold ${errorOver ? 'text-red-600' : ''}`}>
              {health.error_rate.toFixed(2)}%
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default HealthDashboard;
