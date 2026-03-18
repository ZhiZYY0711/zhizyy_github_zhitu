import { useState, useEffect } from 'react';
import { Code2 } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { fetchCodeReviews, submitCodeReview } from '../../services/api';
import type { CodeReview } from '../../types';

const STATUS_CONFIG = {
  pending: { label: '待评审', className: 'bg-orange-100 text-orange-700 border-orange-200' },
  resolved: { label: '已解决', className: 'bg-green-100 text-green-700 border-green-200' },
  closed: { label: '已关闭', className: 'bg-gray-100 text-gray-600 border-gray-200' },
};

export default function CodeReviewList() {
  const [reviews, setReviews] = useState<CodeReview[]>([]);
  const [loading, setLoading] = useState(false);
  const [target, setTarget] = useState<CodeReview | null>(null);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try { setReviews(await fetchCodeReviews()); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async () => {
    if (!target || !comment) return;
    setSubmitting(true);
    try {
      await submitCodeReview(target.id, comment);
      setReviews(prev => prev.map(r => r.id === target.id ? { ...r, comment, status: 'resolved' as const } : r));
      setTarget(null);
    } finally { setSubmitting(false); }
  };

  const pending = reviews.filter(r => r.status === 'pending');
  const done = reviews.filter(r => r.status !== 'pending');

  const ReviewTable = ({ items, title }: { items: CodeReview[]; title: string }) => (
    <div>
      <p className="text-sm font-medium mb-3 text-muted-foreground">{title} ({items.length})</p>
      <div className="rounded-md border overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">项目</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">学生</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">文件</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">状态</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">操作</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={5} className="px-4 py-6 text-center text-muted-foreground">加载中...</td></tr>
            ) : items.length === 0 ? (
              <tr><td colSpan={5} className="px-4 py-6 text-center text-muted-foreground">暂无数据</td></tr>
            ) : items.map(r => (
              <tr key={r.id} className="border-t hover:bg-muted/20 transition-colors">
                <td className="px-4 py-3 text-muted-foreground">{r.project_name}</td>
                <td className="px-4 py-3 font-medium">{r.student_name}</td>
                <td className="px-4 py-3">
                  <span className="font-mono text-xs bg-muted px-2 py-0.5 rounded">{r.file}:{r.line}</span>
                </td>
                <td className="px-4 py-3">
                  <Badge className={STATUS_CONFIG[r.status].className}>{STATUS_CONFIG[r.status].label}</Badge>
                </td>
                <td className="px-4 py-3">
                  <Button variant="outline" size="sm" onClick={() => { setTarget(r); setComment(r.comment); }}>
                    {r.status === 'pending' ? '评审' : '查看'}
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      <ReviewTable items={pending} title="待评审" />
      <ReviewTable items={done} title="已完成" />

      <Dialog open={!!target} onOpenChange={() => setTarget(null)}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Code2 className="h-5 w-5" />
              代码评审 — {target?.student_name}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="p-3 bg-muted rounded-lg">
              <p className="text-xs text-muted-foreground mb-1">{target?.file} 第 {target?.line} 行</p>
              <code className="text-sm font-mono">{target?.code_snippet}</code>
            </div>
            <div className="space-y-1.5">
              <Label>评审意见</Label>
              <Textarea rows={4} placeholder="建议使用 Stream API 优化此循环..." value={comment} onChange={e => setComment(e.target.value)} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setTarget(null)}>关闭</Button>
            {target?.status === 'pending' && (
              <Button onClick={handleSubmit} disabled={submitting || !comment} className="bg-blue-600 hover:bg-blue-700">
                {submitting ? '提交中...' : '提交评审'}
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
