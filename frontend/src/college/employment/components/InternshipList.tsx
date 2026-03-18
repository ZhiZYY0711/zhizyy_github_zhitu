import { useEffect, useState } from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { AlertTriangle } from 'lucide-react';
import { fetchInternshipStudents } from '../../services/api';
import type { InternshipStudent } from '../../types';

const InternshipList = () => {
  const [students, setStudents] = useState<InternshipStudent[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchInternshipStudents().then(data => {
      setStudents(data.records);
      setLoading(false);
    });
  }, []);

  return (
    <div className="rounded-md border overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-muted/50">
          <tr>
            <th className="text-left px-4 py-3 font-medium">学生姓名</th>
            <th className="text-left px-4 py-3 font-medium">实习单位</th>
            <th className="text-left px-4 py-3 font-medium">岗位</th>
            <th className="text-left px-4 py-3 font-medium">企业导师</th>
            <th className="text-left px-4 py-3 font-medium">最近周报</th>
            <th className="text-left px-4 py-3 font-medium">状态</th>
            <th className="text-left px-4 py-3 font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={7} className="text-center py-8 text-muted-foreground">加载中...</td></tr>
          ) : students.map((s, i) => (
            <tr key={i} className="border-t hover:bg-muted/30 transition-colors">
              <td className="px-4 py-3 font-medium">{s.student_name}</td>
              <td className="px-4 py-3">{s.company}</td>
              <td className="px-4 py-3 text-muted-foreground">{s.position}</td>
              <td className="px-4 py-3 text-muted-foreground">{s.mentor_name}</td>
              <td className="px-4 py-3 text-muted-foreground text-xs">{s.last_report_time}</td>
              <td className="px-4 py-3">
                {s.status === 'warning' ? (
                  <Badge className="border-transparent bg-red-500 text-white gap-1">
                    <AlertTriangle className="h-3 w-3" />
                    异常
                  </Badge>
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
