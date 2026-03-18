import { useEffect, useState } from 'react';
import React from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Label } from '@/components/ui/label';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { fetchEnterpriseAuditList, auditEnterprise } from '../../services/api';
import type { EnterpriseAuditItem } from '../../types';

const STATUS_LABELS: Record<EnterpriseAuditItem['status'], string> = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
};

const STATUS_COLORS: Record<EnterpriseAuditItem['status'], string> = {
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

export default function EnterpriseAudit() {
  const [items, setItems] = useState<EnterpriseAuditItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('all');
  const [selected, setSelected] = useState<EnterpriseAuditItem | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectError, setRejectError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await fetchEnterpriseAuditList(tab !== 'all' ? tab : undefined);
      setItems(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [tab]);

  const handleAudit = async (action: 'pass' | 'reject') => {
    if (!selected) return;
    if (action === 'reject' && !rejectReason.trim()) {
      setRejectError('请填写拒绝原因');
      return;
    }
    setSubmitting(true);
    try {
      await auditEnterprise(selected.id, action, action === 'reject' ? rejectReason : undefined);
      setItems(prev => prev.map(item =>
        item.id === selected.id
          ? { ...item, status: action === 'pass' ? 'approved' : 'rejected', reject_reason: rejectReason }
          : item
      ));
      setSelected(null);
      setRejectReason('');
      setRejectError('');
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
                    <div className="space-y-1 flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-medium">{item.name}</span>
                        <Badge className={STATUS_COLORS[item.status]}>{STATUS_LABELS[item.status]}</Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        联系人：{item.contact_person} · {item.contact_phone}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        申请时间：{item.apply_time.slice(0, 10)}
                      </p>
                      {item.reject_reason && (
                        <p className="text-xs text-red-600">拒绝原因：{item.reject_reason}</p>
                      )}
                    </div>
                    <Button variant="outline" size="sm" onClick={() => { setSelected(item); setRejectReason(''); setRejectError(''); }}>
                      查看详情
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </TabsContent>
        ))}
      </Tabs>

      <Dialog open={!!selected} onOpenChange={open => { if (!open) { setSelected(null); setRejectReason(''); setRejectError(''); } }}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>企业审核详情</DialogTitle>
          </DialogHeader>
          {selected && (
            <div className="space-y-3 py-1 text-sm">
              <div className="grid grid-cols-2 gap-2">
                <div><span className="text-muted-foreground">企业名称：</span>{selected.name}</div>
                <div><span className="text-muted-foreground">状态：</span>
                  <Badge className={STATUS_COLORS[selected.status]}>{STATUS_LABELS[selected.status]}</Badge>
                </div>
                <div><span className="text-muted-foreground">联系人：</span>{selected.contact_person}</div>
                <div><span className="text-muted-foreground">联系电话：</span>{selected.contact_phone}</div>
                <div><span className="text-muted-foreground">申请时间：</span>{selected.apply_time.slice(0, 10)}</div>
                {selected.audit_time && (
                  <div><span className="text-muted-foreground">审核时间：</span>{selected.audit_time.slice(0, 10)}</div>
                )}
              </div>
              <div>
                <span className="text-muted-foreground">营业执照：</span>
                <a href={selected.license_url} target="_blank" rel="noreferrer" className="text-blue-600 underline ml-1">
                  查看执照
                </a>
              </div>
              {selected.reject_reason && (
                <div className="text-red-600">
                  <span className="text-muted-foreground">拒绝原因：</span>{selected.reject_reason}
                </div>
              )}
              {selected.status === 'pending' && (
                <div className="space-y-1 pt-2">
                  <Label>拒绝原因（拒绝时必填）</Label>
                  <textarea
                    value={rejectReason}
                    onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => { setRejectReason(e.target.value); setRejectError(''); }}
                    placeholder="请输入拒绝原因..."
                    rows={3}
                    className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                  {rejectError && <p className="text-xs text-destructive">{rejectError}</p>}
                </div>
              )}
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
