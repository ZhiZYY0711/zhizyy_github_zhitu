import { useEffect, useState } from 'react';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Search } from 'lucide-react';
import { fetchStudents } from '../../services/api';
import type { Student } from '../../types';

const statusMap: Record<Student['status'], { label: string; className: string }> = {
  active: { label: '在读', className: 'border-transparent bg-green-500 text-white' },
  suspended: { label: '休学', className: 'border-transparent bg-yellow-500 text-white' },
  graduated: { label: '毕业', className: 'border-transparent bg-gray-400 text-white' },
};

const StudentList = () => {
  const [students, setStudents] = useState<Student[]>([]);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    fetchStudents({ keyword }).then(data => {
      setStudents(data.records);
      setTotal(data.total);
      setLoading(false);
    });
  }, [keyword]);

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <div className="relative flex-1 max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="搜索姓名/学号..."
            className="pl-9"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
          />
        </div>
        <span className="text-sm text-muted-foreground">共 {total} 名学生</span>
      </div>

      <div className="rounded-md border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="text-left px-4 py-3 font-medium">学号</th>
              <th className="text-left px-4 py-3 font-medium">姓名</th>
              <th className="text-left px-4 py-3 font-medium">班级</th>
              <th className="text-left px-4 py-3 font-medium">GPA</th>
              <th className="text-left px-4 py-3 font-medium">联系方式</th>
              <th className="text-left px-4 py-3 font-medium">状态</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} className="text-center py-8 text-muted-foreground">加载中...</td></tr>
            ) : students.length === 0 ? (
              <tr><td colSpan={6} className="text-center py-8 text-muted-foreground">暂无数据</td></tr>
            ) : students.map(s => {
              const st = statusMap[s.status];
              return (
                <tr key={s.id} className="border-t hover:bg-muted/30 transition-colors">
                  <td className="px-4 py-3 font-mono text-xs">{s.student_no}</td>
                  <td className="px-4 py-3 font-medium">{s.name}</td>
                  <td className="px-4 py-3 text-muted-foreground">{s.class_name}</td>
                  <td className="px-4 py-3">
                    <span className={s.gpa >= 3.5 ? 'text-green-600 font-medium' : s.gpa < 2.5 ? 'text-red-500' : ''}>
                      {s.gpa.toFixed(1)}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">{s.phone}</td>
                  <td className="px-4 py-3">
                    <Badge className={st.className}>{st.label}</Badge>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default StudentList;
