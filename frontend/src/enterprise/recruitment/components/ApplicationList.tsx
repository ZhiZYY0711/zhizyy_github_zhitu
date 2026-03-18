import { useState, useEffect } from 'react';
import { ExternalLink, Calendar, Star } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { fetchApplications, scheduleInterview, rejectApplication, addToTalentPool } from '../../services/api';
import type { Application } from '../../types';

const STATUS_CONFIG = {
  pending: { label: '待处理', className: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
  interview: { label: '面试中', className: 'bg-blue-100 text-blue-700 border-blue-200' },
  offered: { label: '已发 Offer', className: 'bg-green-100 text-green-700 border-green-200' },
  rejected: { label: '已拒绝', className: 'bg-red-100 text-red-700 border-red-200' },
  withdrawn: { label: '已撤回', className: 'bg-gray-100 text-gray-600 border-gray-200' },
};

export default function ApplicationList() {
  const [apps, setApps] = useState<Application[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('all');
  const [interviewTarget, setInterviewTarget] = useState<Application | null>(null);
  const [interviewForm, setInterviewForm] = useState({ time: '', type: 'online', link: '' });
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await fetchApplications({ status: statusFilter !== 'all' ? statusFilter : undefined });
      setApps(data);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [statusFilter]);

  const handleInterview = async () => {
    if (!interviewTarget) return;
    setSubmitting(true);
    try {
      await scheduleInterview({ application_id: interviewTarget.application_id, ...interviewForm });
      setApps(prev => prev.map(a => a.application_id === interviewTarget.application_id ? { ...a, status: 'interview' as const } : a));
      setInterviewTarget(null);
    } finally { setSubmitting(false); }
  };

  const handleReject = async (id: string) => {
    await rejectApplication(id);
    setApps(prev => prev.map(a => a.application_id === id ? { ...a, status: 'rejected' as const } : a));
  };

  const handleAddToPool = async (app: Application) => {
    await addToTalentPool(app.student_id, ['潜力股']);
  };

  const filtered = apps.filter(a => statusFilter === 'all' || a.status === statusFilter);

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-36">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="pending">待处理</SelectItem>
            <SelectItem value="interview">面试中</SelectItem>
            <SelectItem value="offered">已发 Offer</SelectItem>
            <SelectItem value="rejected">已拒绝</SelectItem>
          </SelectContent>
        </Select>
        <span className="text-sm text-muted-foreground">共 {filtered.length} 份简历</span>
      </div>

      <div className="rounded-md border overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">候选人</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">应聘职位</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">匹配度</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">申请时间</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">状态</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground">操作</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} className="px-4 py-8 text-center text-muted-foreground">加载中...</td></tr>
            ) : filtered.length === 0 ? (
              <tr><td colSpan={6} className="px-4 py-8 text-center text-muted-foreground">暂无简历</td></tr>
            ) : filtered.map(app => (
              <tr key={app.application_id} className="border-t hover:bg-muted/20 transition-colors">
                <td className="px-4 py-3">
                  <div>
                    <p className="font-medium">{app.student_name}</p>
                    <p className="text-xs text-muted-foreground">{app.school} · {app.major}</p>
                  </div>
                </td>
                <td className="px-4 py-3 text-muted-foreground">{app.job_title}</td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2 min-w-24">
                    <Progress value={app.match_score * 100} className="h-1.5 flex-1" />
                    <span className={`text-xs font-medium ${app.match_score >= 0.8 ? 'text-green-600' : app.match_score >= 0.6 ? 'text-yellow-600' : 'text-gray-500'}`}>
                      {Math.round(app.match_score * 100)}%
                    </span>
                  </div>
                </td>
                <td className="px-4 py-3 text-muted-foreground">{app.apply_time}</td>
                <td className="px-4 py-3">
                  <Badge className={STATUS_CONFIG[app.status].className}>{STATUS_CONFIG[app.status].label}</Badge>
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-1.5 flex-wrap">
                    <Button variant="ghost" size="sm" onClick={() => window.open(app.resume_url, '_blank')}>
                      <ExternalLink className="h-3.5 w-3.5 mr-1" />简历
                    </Button>
                    {app.status === 'pending' && (
                      <>
                        <Button variant="outline" size="sm" onClick={() => setInterviewTarget(app)}>
                          <Calendar className="h-3.5 w-3.5 mr-1" />邀约
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleAddToPool(app)}>
                          <Star className="h-3.5 w-3.5 mr-1" />收藏
                        </Button>
                        <Button variant="ghost" size="sm" className="text-red-500 hover:text-red-600" onClick={() => handleReject(app.application_id)}>
                          拒绝
                        </Button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Dialog open={!!interviewTarget} onOpenChange={() => setInterviewTarget(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>发起面试邀约 — {interviewTarget?.student_name}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1.5">
              <Label>面试时间</Label>
              <Input type="datetime-local" value={interviewForm.time} onChange={e => setInterviewForm(f => ({ ...f, time: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>面试方式</Label>
              <Select value={interviewForm.type} onValueChange={v => setInterviewForm(f => ({ ...f, type: v }))}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="online">在线面试</SelectItem>
                  <SelectItem value="onsite">现场面试</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {interviewForm.type === 'online' && (
              <div className="space-y-1.5">
                <Label>会议链接</Label>
                <Input placeholder="https://meeting.tencent.com/..." value={interviewForm.link} onChange={e => setInterviewForm(f => ({ ...f, link: e.target.value }))} />
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setInterviewTarget(null)}>取消</Button>
            <Button onClick={handleInterview} disabled={submitting || !interviewForm.time} className="bg-blue-600 hover:bg-blue-700">
              {submitting ? '发送中...' : '发送邀约'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
