import { useState, useEffect } from 'react';
import { TrendingUp, DollarSign, Search, CheckCircle, XCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, BarChart, Bar,
} from 'recharts';
import { fetchAnalytics } from '../services/api';
import type { AnalyticsData } from '../types';

const PIE_COLORS = ['#3b82f6', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6'];

export default function AnalyticsPage() {
  const [data, setData] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState('month');
  const [certCode, setCertCode] = useState('');
  const [certResult, setCertResult] = useState<{ valid: boolean; name?: string; date?: string } | null>(null);

  useEffect(() => {
    setLoading(true);
    fetchAnalytics(timeRange).then(setData).finally(() => setLoading(false));
  }, [timeRange]);

  const handleVerifyCert = () => {
    if (!certCode.trim()) return;
    // Mock verification
    const valid = certCode.length >= 8;
    setCertResult(valid
      ? { valid: true, name: '张三', date: '2025-01-15' }
      : { valid: false }
    );
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">企业效益分析</h1>
          <p className="text-muted-foreground mt-1">评估人才合作价值与招聘效益</p>
        </div>
        <Select value={timeRange} onValueChange={setTimeRange}>
          <SelectTrigger className="w-36">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="month">本月</SelectItem>
            <SelectItem value="quarter">本季度</SelectItem>
            <SelectItem value="year">本年度</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {loading ? (
        <div className="grid grid-cols-2 gap-4">{Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-32 bg-muted animate-pulse rounded-lg" />
        ))}</div>
      ) : data && (
        <>
          {/* KPI Cards */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <Card>
              <CardContent className="p-5">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-full bg-green-50 flex items-center justify-center">
                    <TrendingUp className="h-5 w-5 text-green-600" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">实习转正率</p>
                    <p className="text-2xl font-bold text-green-600">{Math.round(data.conversion_rate.internship_to_fulltime * 100)}%</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-5">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-full bg-blue-50 flex items-center justify-center">
                    <DollarSign className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">招聘成本节省</p>
                    <p className="text-2xl font-bold text-blue-600">¥{(data.conversion_rate.cost_saving / 10000).toFixed(1)}万</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-5">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-full bg-purple-50 flex items-center justify-center">
                    <TrendingUp className="h-5 w-5 text-purple-600" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">实习生贡献总值</p>
                    <p className="text-2xl font-bold text-purple-600">¥{(data.contribution.total_value / 10000).toFixed(0)}万</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-5">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-full bg-orange-50 flex items-center justify-center">
                    <TrendingUp className="h-5 w-5 text-orange-600" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">最终入职人数</p>
                    <p className="text-2xl font-bold text-orange-600">{data.recruitment_funnel[data.recruitment_funnel.length - 1]?.count ?? 0}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Conversion Trend */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">转化率趋势</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={200}>
                  <LineChart data={data.conversion_trend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                    <YAxis tickFormatter={v => `${Math.round(v * 100)}%`} tick={{ fontSize: 12 }} />
                    <Tooltip formatter={(v: unknown) => [`${Math.round((v as number) * 100)}%`, '转化率']} />
                    <Line type="monotone" dataKey="rate" stroke="#22c55e" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            {/* Department Contribution */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">部门贡献度分布</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={200}>
                  <PieChart>
                    <Pie data={data.contribution.by_department} dataKey="value" nameKey="department" cx="50%" cy="50%" outerRadius={70} label={(props) => { const p = props as unknown as { department: string; percent?: number }; return `${p.department} ${Math.round((p.percent ?? 0) * 100)}%`; }} labelLine={false}>
                      {data.contribution.by_department.map((_, i) => (
                        <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(v: unknown) => [`¥${((v as number) / 10000).toFixed(1)}万`]} />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>

          {/* Recruitment Funnel */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">招聘漏斗</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={180}>
                <BarChart data={data.recruitment_funnel} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis type="number" tick={{ fontSize: 12 }} />
                  <YAxis dataKey="stage" type="category" tick={{ fontSize: 12 }} width={80} />
                  <Tooltip />
                  <Bar dataKey="count" fill="#3b82f6" radius={[0, 4, 4, 0]} name="人数" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </>
      )}

      {/* Certificate Verification */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">证书验证</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-3 max-w-md">
            <Input
              placeholder="输入证书编号..."
              value={certCode}
              onChange={e => { setCertCode(e.target.value); setCertResult(null); }}
            />
            <Button onClick={handleVerifyCert} disabled={!certCode.trim()} className="bg-blue-600 hover:bg-blue-700 shrink-0">
              <Search className="h-4 w-4 mr-2" />验证
            </Button>
          </div>
          {certResult && (
            <div className={`mt-4 p-4 rounded-lg border flex items-start gap-3 max-w-md ${certResult.valid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
              {certResult.valid ? (
                <>
                  <CheckCircle className="h-5 w-5 text-green-600 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-green-700">证书有效</p>
                    <p className="text-sm text-green-600 mt-1">持有人：{certResult.name}</p>
                    <p className="text-sm text-green-600">颁发时间：{certResult.date}</p>
                  </div>
                </>
              ) : (
                <>
                  <XCircle className="h-5 w-5 text-red-600 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-red-700">证书无效</p>
                    <p className="text-sm text-red-600 mt-1">未找到对应证书，请检查编号是否正确</p>
                  </div>
                </>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
