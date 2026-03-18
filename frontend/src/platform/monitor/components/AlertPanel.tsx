import type { FC } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import type { ServiceStatus } from '../../types';

interface AlertPanelProps {
  services: ServiceStatus[];
  loading: boolean;
}

const statusConfig = {
  healthy: { label: '正常', className: 'bg-green-100 text-green-700 border-green-200' },
  degraded: { label: '降级', className: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
  down: { label: '宕机', className: 'bg-red-100 text-red-700 border-red-200' },
};

const AlertPanel: FC<AlertPanelProps> = ({ services, loading }) => {
  const healthy = services.filter(s => s.status === 'healthy').length;
  const degraded = services.filter(s => s.status === 'degraded').length;
  const down = services.filter(s => s.status === 'down').length;

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle className="text-base font-semibold">微服务健康状态</CardTitle>
        {!loading && services.length > 0 && (
          <p className="text-xs text-muted-foreground">
            <span className="text-green-600 font-medium">{healthy} 正常</span>
            {degraded > 0 && <span className="text-yellow-600 font-medium ml-2">{degraded} 降级</span>}
            {down > 0 && <span className="text-red-600 font-medium ml-2">{down} 宕机</span>}
          </p>
        )}
      </CardHeader>
      <CardContent className="p-0">
        {loading ? (
          <div className="space-y-2 px-6 pb-4">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-10 w-full" />
            ))}
          </div>
        ) : services.length === 0 ? (
          <div className="flex items-center justify-center h-32 text-muted-foreground text-sm">暂无数据</div>
        ) : (
          <div className="divide-y">
            {services.map(svc => {
              const cfg = statusConfig[svc.status];
              const isDown = svc.status === 'down';
              return (
                <div
                  key={svc.name}
                  className={`flex items-center justify-between px-6 py-3 text-sm ${isDown ? 'bg-red-50' : ''}`}
                >
                  <span className={`font-medium ${isDown ? 'text-red-700' : ''}`}>{svc.name}</span>
                  <div className="flex items-center gap-3">
                    <Badge variant="outline" className={cfg.className}>{cfg.label}</Badge>
                    <span className="text-muted-foreground w-16 text-right">{svc.response_time} ms</span>
                    <span className="text-muted-foreground text-xs w-20 text-right">
                      {new Date(svc.last_check).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default AlertPanel;
