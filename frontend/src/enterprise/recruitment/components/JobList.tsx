import { useState, useEffect } from 'react';
import { Plus, MapPin, DollarSign, Users, XCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { fetchJobs, createJob, closeJob } from '../../services/api';
import type { Job } from '../../types';

const STATUS_CONFIG = {
  active: { label: '招聘中', className: 'bg-green-100 text-green-700 border-green-200' },
  closed: { label: '已关闭', className: 'bg-gray-100 text-gray-600 border-gray-200' },
  draft: { label: '草稿', className: 'bg-yellow-100 text-yellow-700 border-yellow-200' },
};

export default function JobList() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('all');
  const [showCreate, setShowCreate] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', requirements: '', salary_range: '', location: '' });

  const load = async () => {
    setLoading(true);
    try {
      const data = await fetchJobs(statusFilter !== 'all' ? statusFilter : undefined);
      setJobs(data);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [statusFilter]);

  const handleCreate = async () => {
    if (!form.title || !form.description) return;
    setSubmitting(true);
    try {
      await createJob({ ...form, type: 'internship', requirements: form.requirements.split('\n').filter(Boolean) });
      setShowCreate(false);
      setForm({ title: '', description: '', requirements: '', salary_range: '', location: '' });
      load();
    } finally { setSubmitting(false); }
  };

  const handleClose = async (id: string) => {
    await closeJob(id);
    setJobs(prev => prev.map(j => j.id === id ? { ...j, status: 'closed' as const } : j));
  };

  const filtered = jobs.filter(j => statusFilter === 'all' || j.status === statusFilter);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-36">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="active">招聘中</SelectItem>
            <SelectItem value="closed">已关闭</SelectItem>
            <SelectItem value="draft">草稿</SelectItem>
          </SelectContent>
        </Select>
        <Button onClick={() => setShowCreate(true)} className="bg-blue-600 hover:bg-blue-700">
          <Plus className="h-4 w-4 mr-2" />发布职位
        </Button>
      </div>

      {loading ? (
        <div className="space-y-3">{Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="h-24 bg-muted animate-pulse rounded-lg" />
        ))}</div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-12 text-muted-foreground">
          <Users className="h-12 w-12 mx-auto mb-3 opacity-30" />
          <p>暂无职位，点击"发布职位"开始招聘</p>
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map(job => (
            <div key={job.id} className="border rounded-lg p-4 hover:bg-muted/20 transition-colors">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <h3 className="font-semibold">{job.title}</h3>
                    <Badge className={STATUS_CONFIG[job.status].className}>{STATUS_CONFIG[job.status].label}</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground mt-1 line-clamp-2">{job.description}</p>
                  <div className="flex items-center gap-4 mt-2 text-xs text-muted-foreground flex-wrap">
                    <span className="flex items-center gap-1"><MapPin className="h-3 w-3" />{job.location}</span>
                    <span className="flex items-center gap-1"><DollarSign className="h-3 w-3" />{job.salary_range}</span>
                    <span className="flex items-center gap-1"><Users className="h-3 w-3" />{job.applicant_count ?? 0} 人投递</span>
                    <span>发布于 {job.created_at}</span>
                  </div>
                </div>
                {job.status === 'active' && (
                  <Button variant="outline" size="sm" onClick={() => handleClose(job.id)} className="shrink-0">
                    <XCircle className="h-4 w-4 mr-1" />关闭
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <Dialog open={showCreate} onOpenChange={setShowCreate}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>发布新职位</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1.5">
              <Label>职位名称 *</Label>
              <Input placeholder="如：Java 后端实习生" value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>薪资范围</Label>
                <Input placeholder="如：200-300/天" value={form.salary_range} onChange={e => setForm(f => ({ ...f, salary_range: e.target.value }))} />
              </div>
              <div className="space-y-1.5">
                <Label>工作地点</Label>
                <Input placeholder="如：北京" value={form.location} onChange={e => setForm(f => ({ ...f, location: e.target.value }))} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>职位描述 *</Label>
              <Textarea rows={3} placeholder="描述工作内容..." value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>任职要求（每行一条）</Label>
              <Textarea rows={3} placeholder="熟悉 Spring Boot&#10;了解 MySQL&#10;有项目经验" value={form.requirements} onChange={e => setForm(f => ({ ...f, requirements: e.target.value }))} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowCreate(false)}>取消</Button>
            <Button onClick={handleCreate} disabled={submitting || !form.title || !form.description} className="bg-blue-600 hover:bg-blue-700">
              {submitting ? '发布中...' : '发布'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
