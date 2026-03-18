import { useEffect, useState } from 'react';
import React from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Label } from '@/components/ui/label';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { fetchProjectAuditList, auditProject } from '../../services/api';
import type { ProjectAuditItem } from '../../types';

const STATUS_LABELS: Record<ProjectAuditItem['status'], string> = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
};

const STATUS_COLORS: Record<ProjectAuditItem['status'], string> = {
  pending: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  approved: 'bg-green-100 text-green-700 border-green-200',
  rejected: 'bg-red-100 text-red-700 border-red-200',
};

const TABS = [
  { value: 'all', label: '全部' },
  { value: 'pending', label: '待审核' },
  { value: 'approved', label: '已通过' },
  { value: 'rejected', label: '已拒绝' },
];

const STARS = (n: number) => '★'.repeat(n) + '☆'.repeat(5 - n);

export default function ProjectAudit() {
  const [items, setItems] = useState<ProjectAuditItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('all');
  const [selected, setSelected] = useState<ProjectAuditItem | null>(null);
  const [rating, setRating] = useState<string>('');
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await fetchProjectAuditList(tab !== 'all' ? tab : undefined);
      setItems(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [tab]);

  const handleAudit = async (action: 'pass' | 'reject') => {
    if (!selected) return;
    setSubmitting(true);
    try {
      await auditProject(selected.id, action, rating || undefined, comment || undefined);
      setItems(prev => prev.map(item =>
        item.id === selected.id
          ? {
              ...item,
              status: action === 'pass' ? 'approved' : 'rejected',
              quality_rating: (rating as ProjectAuditItem['quality_rating']) || item.quality_rating,
              audit_comment: comment || item.audit_comment,
            }
          : item
      ));
      setSelected(null);
      setRating('');
      setComment('');
    } finally {
      setSubmitting(false);
    }
  };

  const filtered = tab === 'all' ? items : items.filter(i => i.status === tab);

  return (
    <div className="space-y-4">
      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          {TABS.map(t => (
            <TabsTrigger key={t.value} value={t.value}>{t.label}</TabsTrigger>
          ))}
        </TabsList>

        {TABS.map(t => (
          <TabsContent key={t.value} value={t.value}>
            {loading ? (
              <div className="space-y-3 mt-4">
                {[1, 2, 3].map(i => <Skeleton key={i} className="h-28 w-full rounded-lg" />)}
              </div>
            ) : filtered.length === 0 ? (
              <p className="text-center text-muted-foreground py-10">暂无数据</p>
            ) : (
              <div className="grid gap-3 mt-4">
                {filtered.map(item => (
                  <div key={item.id} className="border rounded-lg p-4 flex items-start justify-between gap-4">
                    <div className="space-y-1.5 flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-medium">{item.name}</span>
                        <Badge className={STATUS_COLORS[item.status]}>{STATUS_LABELS[item.status]}</Badge>
                        {item.quality_rating && (
                          <Badge variant="outline">评级 {item.quality_rating}</Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground">
                        提供方：{item.provider}
                        <span className="mx-2">·</span>
                        难度：<span className="text-yellow-500">{STARS(item.difficulty)}</span>
                      </p>
                      <div className="flex flex-wrap gap-1">
                        {item.tech_stack.map(tech => (
                          <Badge key={tech} variant="secondary" className="text-xs">{tech}</Badge>
                        ))}
                      </div>
                      <p className="text-xs text-muted-foreground">申请时间：{item.apply_time.slice(0, 10)}</p>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => { setSelected(item); setRating(item.quality_rating || ''); setComment(item.audit_comment || ''); }}
                    >
                      审核
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </TabsContent>
        ))}
      </Tabs>

      <Dialog open={!!selected} onOpenChange={open => { if (!open) { setSelected(null); setRating(''); setComment(''); } }}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>项目审核</DialogTitle>
          </DialogHeader>
          {selected && (
            <div className="space-y-3 py-1 text-sm">
              <div className="grid grid-cols-2 gap-2">
                <div className="col-span-2"><span className="text-muted-foreground">项目名称：</span>{selected.name}</div>
                <div><span className="text-muted-foreground">提供方：</span>{selected.provider}</div>
                <div><span className="text-muted-foreground">难度：</span>
                  <span className="text-yellow-500">{STARS(selected.difficulty)}</span>
                </div>
                <div><span className="text-muted-foreground">申请时间：</span>{selected.apply_time.slice(0, 10)}</div>
                <div><span className="text-muted-foreground">状态：</span>
                  <Badge className={STATUS_COLORS[selected.status]}>{STATUS_LABELS[selected.status]}</Badge>
                </div>
              </div>
              <div>
                <span className="text-muted-foreground">技术栈：</span>
                <span className="inline-flex flex-wrap gap-1 ml-1">
                  {selected.tech_stack.map(t => (
                    <Badge key={t} variant="secondary" className="text-xs">{t}</Badge>
                  ))}
                </span>
              </div>
              {selected.description && (
                <div><span className="text-muted-foreground">描述：</span>{selected.description}</div>
              )}
              <div className="space-y-1 pt-1">
                <Label>质量评级</Label>
                <Select value={rating} onValueChange={setRating}>
                  <SelectTrigger>
                    <SelectValue placeholder="请选择评级" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="S">S 级</SelectItem>
                    <SelectItem value="A">A 级</SelectItem>
                    <SelectItem value="B">B 级</SelectItem>
                    <SelectItem value="C">C 级</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1">
                <Label>审核意见</Label>
                <textarea
                  value={comment}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setComment(e.target.value)}
                  placeholder="请输入审核意见..."
                  rows={3}
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-ring"
                />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setSelected(null)}>关闭</Button>
            {selected?.status === 'pending' && (
              <>
                <Button variant="destructive" onClick={() => handleAudit('reject')} disabled={submitting}>
                  {submitting ? '处理中...' : '拒绝'}
                </Button>
                <Button onClick={() => handleAudit('pass')} disabled={submitting}>
                  {submitting ? '处理中...' : '通过'}
                </Button>
              </>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
