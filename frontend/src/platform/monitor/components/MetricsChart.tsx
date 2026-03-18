import { useState, useCallback } from 'react';
import type { FC } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { RefreshCw } from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { fetchOnlineUserTrend } from '../../services/api';
import { usePolling } from '../../utils/polling';
import type { OnlineUserTrend } from '../../types';

const MetricsChart: FC = () => {
  const [trend, setTrend] = useState<OnlineUserTrend | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    try {
      const data = await fetchOnlineUserTrend();
      setTrend(data);
    } finally {
      setLoading(false);
    }
  }, []);

  usePolling(load, 30_000);

  const currentCount = trend?.data.at(-1)?.count ?? 0;

  return (
    <Card className="h-full">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-base font-semibold">在线用户趋势（过去 24 小时）</CardTitle>
          <Button variant="ghost" size="icon" onClick={load} disabled={loading}>
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </Button>
        </div>
        {!loading && (
          <p className="text-3xl font-bold">{currentCount} <span className="text-sm font-normal text-muted-foreground">当前在线</span></p>
        )}
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="space-y-2">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-48 w-full" />
          </div>
        ) : !trend || trend.data.length === 0 ? (
          <div className="flex items-center justify-center h-48 text-muted-foreground text-sm">暂无数据</div>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={trend.data} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="time" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <Tooltip
                contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '12px' }}
                labelStyle={{ fontWeight: 600 }}
              />
              <Line
                type="monotone"
                dataKey="count"
                stroke="#3b82f6"
                strokeWidth={2}
                dot={{ r: 3, fill: '#3b82f6' }}
                activeDot={{ r: 5 }}
                name="在线用户"
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
};

export default MetricsChart;
