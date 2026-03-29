import { useState, useEffect } from 'react';
import { Award } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { fetchInterns, issueCertificate } from '../../services/api';
import type { Intern } from '../../types';

const STATUS_CONFIG = {
  pending: { label: '待入职', className: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
  active: { label: '在岗', className: 'bg-green-100 text-green-700 border-green-200' },
  completed: { label: '已离职', className: 'bg-gray-100 text-gray-600 border-gray-200' },
  terminated: { label: '已终止', className: 'bg-red-100 text-red-700 border-red-200' },
};

export default function InternList() {
  const [interns, setInterns] = useState<Intern[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('all');
  const [certTarget, setCertTarget] = useState<Intern | null>(null);
  const [certComment, setCertComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      // 后端使用数字状态
      const statusParam = statusFilter === 'all' ? undefined :
                          statusFilter === 'active' ? 1 :
                          statusFilter === 'pending' ? 0 :
                          statusFilter === 'completed' ? 2 : 3;
      setInterns(await fetchInterns(statusParam));
    }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [statusFilter]);

  const handleIssueCert = async () => {
    if (!certTarget) return;
    setSubmitting(true);
    try {
      await issueCertificate(certTarget.student_id, certComment);
      setCertTarget(null);
      setCertComment('');
    } finally { setSubmitting(false); }
  };

  const filtered = interns.filter(i => statusFilter === 'all' || i.status === statusFilter);

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="active">在岗</SelectItem>
            <SelectItem value="pending">待入职</SelectItem>
            <SelectItem value="completed">已离职</SelectItem>
          </SelectContent>
        </Select>
        <span className="text-sm text-muted-foreground">共 {filtered.length} 名实习生</span>
      </div>

      <div className="rounded-md border overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">姓名</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">岗位</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">导师</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">入职时间</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">薪资</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">状态</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">操作</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className="px-4 py-8 text-center text-muted-foreground">加载中...</td></tr>
            ) : filtered.length === 0 ? (
              <tr><td colSpan={7} className="px-4 py-8 text-center text-muted-foreground">暂无实习生</td></tr>
            ) : filtered.map(intern => (
              <tr key={intern.id} className="border-t hover:bg-muted/20 transition-colors">
                <td className="px-4 py-3">
                  <div>
                    <p className="font-medium">{intern.name}</p>
                    <p className="text-xs text-muted-foreground">{intern.school}</p>
                  </div>
                </td>
                <td className="px-4 py-3 text-muted-foreground">{intern.position}</td>
                <td className="px-4 py-3 text-muted-foreground">{intern.mentor_name}</td>
                <td className="px-4 py-3 text-muted-foreground">{intern.start_date}</td>
                <td className="px-4 py-3 text-muted-foreground">{intern.salary ?? '-'}</td>
                <td className="px-4 py-3">
                  <Badge className={STATUS_CONFIG[intern.status].className}>{STATUS_CONFIG[intern.status].label}</Badge>
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-1.5">
                    {intern.status === 'active' && (
                      <Button variant="outline" size="sm" onClick={() => { setCertTarget(intern); setCertComment(''); }}>
                        <Award className="h-3.5 w-3.5 mr-1" />发证明
                      </Button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Dialog open={!!certTarget} onOpenChange={() => setCertTarget(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>发放实习证明 — {certTarget?.name}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="p-3 bg-muted/50 rounded-lg text-sm space-y-1">
              <p><span className="text-muted-foreground">姓名：</span>{certTarget?.name}</p>
              <p><span className="text-muted-foreground">岗位：</span>{certTarget?.position}</p>
              <p><span className="text-muted-foreground">入职时间：</span>{certTarget?.start_date}</p>
            </div>
            <div className="space-y-1.5">
              <Label>评价意见</Label>
              <Textarea rows={4} placeholder="如：该同学在实习期间表现优秀，技术能力强，团队协作好，欢迎转正..." value={certComment} onChange={e => setCertComment(e.target.value)} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCertTarget(null)}>取消</Button>
            <Button onClick={handleIssueCert} disabled={submitting || !certComment} className="bg-blue-600 hover:bg-blue-700">
              {submitting ? '发放中...' : '确认发放'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
