import { useEffect, useState } from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { AlertTriangle } from 'lucide-react';
import { fetchInternshipStudents } from '../../services/api';
import type { InternshipStudent } from '../../types';

const InternshipList = () => {
  const [students, setStudents] = useState<InternshipStudent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    fetchInternshipStudents()
      .then((data) => {
        setStudents(Array.isArray(data?.records) ? data.records : []);
        setError(null);
      })
      .catch((err) => {
        console.error('[InternshipList] Failed to fetch students:', err);
        setError('加载失败，请稍后重试');
        setStudents([]);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="rounded-md border overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-muted/50">
          <tr>
            <th className="text-left px-4 py-3 font-medium">学生姓名</th>
            <th className="text-left px-4 py-3 font-medium">学号</th>
            <th className="text-left px-4 py-3 font-medium">实习单位</th>
            <th className="text-left px-4 py-3 font-medium">岗位</th>
            <th className="text-left px-4 py-3 font-medium">企业导师</th>
            <th className="text-left px-4 py-3 font-medium">开始日期</th>
            <th className="text-left px-4 py-3 font-medium">最近周报</th>
            <th className="text-left px-4 py-3 font-medium">状态</th>
            <th className="text-left px-4 py-3 font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={9} className="text-center py-8 text-muted-foreground">加载中...</td></tr>
          ) : error ? (
            <tr><td colSpan={9} className="text-center py-8 text-red-500">{error}</td></tr>
          ) : students.length === 0 ? (
            <tr><td colSpan={9} className="text-center py-8 text-muted-foreground">暂无数据</td></tr>
          ) : students.map((s) => (
            <tr key={s.id} className="border-t hover:bg-muted/30 transition-colors">
              <td className="px-4 py-3 font-medium">{s.studentName || '-'}</td>
              <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{s.studentNo || '-'}</td>
              <td className="px-4 py-3">{s.enterpriseName || '-'}</td>
              <td className="px-4 py-3 text-muted-foreground">{s.jobTitle || '-'}</td>
              <td className="px-4 py-3 text-muted-foreground">{s.mentorName || '-'}</td>
              <td className="px-4 py-3 text-muted-foreground text-xs">{s.startDate || '-'}</td>
              <td className="px-4 py-3 text-muted-foreground text-xs">
                {s.lastReportTime
                  ? new Date(s.lastReportTime).toLocaleDateString('zh-CN')
                  : '-'}
              </td>
              <td className="px-4 py-3">
                {s.statusText === 'warning' ? (
                  <Badge className="border-transparent bg-red-500 text-white gap-1">
                    <AlertTriangle className="h-3 w-3" />
                    异常
                  </Badge>
                ) : s.statusText === 'completed' ? (
                  <Badge className="border-transparent bg-gray-400 text-white">已结束</Badge>
                ) : (
                  <Badge className="border-transparent bg-green-500 text-white">正常</Badge>
                )}
              </td>
              <td className="px-4 py-3">
                <Button variant="ghost" size="sm">查看</Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default InternshipList;
