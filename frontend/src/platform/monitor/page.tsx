import { useState, useCallback } from 'react';
import type { FC } from 'react';
import { Button } from '@/components/ui/button';
import { RefreshCw } from 'lucide-react';
import HealthDashboard from './components/HealthDashboard';
import MetricsChart from './components/MetricsChart';
import AlertPanel from './components/AlertPanel';
import { fetchSystemHealth, fetchServiceStatuses } from '../services/api';
import { usePolling } from '../utils/polling';
import type { SystemHealth, ServiceStatus } from '../types';

const MonitorPage: FC = () => {
  const [health, setHealth] = useState<SystemHealth | null>(null);
  const [services, setServices] = useState<ServiceStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const [h, s] = await Promise.all([fetchSystemHealth(), fetchServiceStatuses()]);
      setHealth(h);
      setServices(s);
      setLastUpdated(new Date());
    } finally {
      setLoading(false);
    }
  }, []);

  usePolling(refresh, 30_000);

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">系统运维监控</h1>
          {lastUpdated && (
            <p className="text-sm text-muted-foreground mt-1">
              最后更新：{lastUpdated.toLocaleTimeString('zh-CN')}
            </p>
          )}
        </div>
        <Button variant="outline" size="sm" onClick={refresh} disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
          刷新
        </Button>
      </div>

      <HealthDashboard health={health} loading={loading} />

      <div className="grid grid-cols-3 gap-6">
        <div className="col-span-2">
          <MetricsChart />
        </div>
        <div className="col-span-1">
          <AlertPanel services={services} loading={loading} />
        </div>
      </div>
    </div>
  );
};

export default MonitorPage;
