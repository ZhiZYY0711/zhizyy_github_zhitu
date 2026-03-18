import { useState, useEffect } from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { fetchWeeklyReports, reviewWeeklyReport } from '../../services/api';
import type { WeeklyReport } from '../../types';

export default function WeeklyReportList() {
  const [reports, setReports] = useState<WeeklyReport[]>([]);
  const [loading, setLoading] = useState(false);
  const [reviewTarget, setReviewTarget] = useState<WeeklyReport | null>(null);
  const [score, setScore] = useState('');
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try { setReports(await fetchWeeklyReports()); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleReview = async () => {
    if (!reviewTarget || !score) return;
    setSubmitting(true);
    try {
      await reviewWeeklyReport(reviewTarget.id, parseInt(score), comment);
      setReports(prev => prev.map(r => r.id === reviewTarget.id
        ? { ...r, status: 'reviewed' as const, score: parseInt(score), mentor_comment: comment }
        : r
      ));
      setReviewTarget(null);
    } finally { setSubmitting(false); }
  };

  const pending = reports.filter(r => r.status === 'pending');
  const reviewed = reports.filter(r => r.status === 'reviewed');

  const ReportTable = ({ items }: { items: WeeklyReport[] }) => (
    <div className="rounded-md border overflow-x-auto">
      <table className="w-full text-sm">
        <thead className="bg-muted/50">
          <tr>
            <th className="px-4 py-3 text-left font-medium text-muted-foreground">实习生</th>
            <th className="px-4 py-3 text-left font-medium text-muted-foreground">周次</th>
            <th className="px-4 py-3 text-left font-medium text-muted-foreground">提交时间</th>
            <th className="px-4 py-3 text-left font-medium text-muted-foreground">评分</th>
            <th className="px-4 py-3 text-left font-medium text-muted-foreground">操作</th>
          </tr>
        </thead>
        <tbody>
          {items.length === 0 ? (
            <tr><td colSpan={5} className="px-4 py-6 text-center text-muted-foreground">暂无数据</td></tr>
          ) : items.map(r => (
            <tr key={r.id} className="border-t hover:bg-muted/20 transition-colors">
              <td className="px-4 py-3 font-medium">{r.intern_name}</td>
              <td className="px-4 py-3 text-muted-foreground">第 {r.week} 周</td>
              <td className="px-4 py-3 text-muted-foreground">{r.submit_time.slice(0, 10)}</td>
              <td className="px-4 py-3">
                {r.score != null ? (
                  <span className={`font-medium ${r.score >= 90 ? 'text-green-600' : r.score >= 70 ? 'text-yellow-600' : 'text-red-600'}`}>
                    {r.score} 分
                  </span>
                ) : <Badge variant="outline" className="text-orange-600 border-orange-300">待批阅</Badge>}
              </td>
              <td className="px-4 py-3">
                <Button variant="outline" size="sm" onClick={() => { setReviewTarget(r); setScore(r.score?.toString() ?? ''); setComment(r.mentor_comment ?? ''); }}>
                  {r.status === 'pending' ? '批阅' : '查看'}
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-medium mb-3 text-orange-600">待批阅 ({pending.length})</p>
        {loading ? <div className="h-24 bg-muted animate-pulse rounded-lg" /> : <ReportTable items={pending} />}
      </div>
      <div>
        <p className="text-sm font-medium mb-3 text-muted-foreground">已批阅 ({reviewed.length})</p>
        <ReportTable items={reviewed} />
      </div>

      <Dialog open={!!reviewTarget} onOpenChange={() => setReviewTarget(null)}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{reviewTarget?.intern_name} — 第 {reviewTarget?.week} 周周报</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="p-3 bg-muted/50 rounded-lg text-sm max-h-40 overflow-y-auto">
              {reviewTarget?.content}
            </div>
            <div className="space-y-1.5">
              <Label>评分（0-100）</Label>
              <Input type="number" min={0} max={100} placeholder="如：85" value={score} onChange={e => setScore(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>评语</Label>
              <Textarea rows={3} placeholder="进度符合预期，继续保持..." value={comment} onChange={e => setComment(e.target.value)} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setReviewTarget(null)}>关闭</Button>
            {reviewTarget?.status === 'pending' && (
              <Button onClick={handleReview} disabled={submitting || !score} className="bg-blue-600 hover:bg-blue-700">
                {submitting ? '提交中...' : '提交批阅'}
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
