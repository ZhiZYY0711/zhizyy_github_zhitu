import { useEffect, useState } from 'react';
import { PlusIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
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
  DialogTrigger,
} from '@/components/ui/dialog';
import { fetchTenantList } from '../../services/api';
import type { Tenant } from '../../types';

const STATUS_LABELS: Record<Tenant['status'], string> = {
  active: '正常',
  inactive: '停用',
  pending: '待审核',
};

const STATUS_COLORS: Record<Tenant['status'], string> = {
  active: 'bg-green-100 text-green-700 border-green-200',
  inactive: 'bg-gray-100 text-gray-600 border-gray-200',
  pending: 'bg-yellow-100 text-yellow-700 border-yellow-200',
};

const TYPE_LABELS: Record<Tenant['type'], string> = {
  college: '高校',
  enterprise: '企业',
};

const TYPE_COLORS: Record<Tenant['type'], string> = {
  college: 'bg-blue-100 text-blue-700 border-blue-200',
  enterprise: 'bg-purple-100 text-purple-700 border-purple-200',
};

interface FormData {
  name: string;
  domain: string;
  admin_username: string;
  admin_email: string;
  max_students: string;
  expire_date: string;
}

const EMPTY_FORM: FormData = {
  name: '',
  domain: '',
  admin_username: '',
  admin_email: '',
  max_students: '',
  expire_date: '',
};

export default function TenantList() {
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState<FormData>(EMPTY_FORM);
  const [errors, setErrors] = useState<Partial<FormData>>({});
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await fetchTenantList(statusFilter !== 'all' ? { status: statusFilter } : undefined);
      setTenants(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [statusFilter]);

  const validate = (): boolean => {
    const e: Partial<FormData> = {};
    if (!form.name.trim()) e.name = '请输入名称';
    if (!form.domain.trim()) e.domain = '请输入域名';
    if (!form.admin_username.trim()) e.admin_username = '请输入管理员用户名';
    if (!form.admin_email.trim()) e.admin_email = '请输入管理员邮箱';
    if (!form.max_students.trim()) e.max_students = '请输入最大学生数';
    if (!form.expire_date.trim()) e.expire_date = '请选择到期日期';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    if (!validate()) return;
    setSubmitting(true);
    try {
      // API call would go here
      await new Promise(r => setTimeout(r, 500));
      setDialogOpen(false);
      setForm(EMPTY_FORM);
      setErrors({});
      await load();
    } finally {
      setSubmitting(false);
    }
  };

  const handleOpenChange = (open: boolean) => {
    setDialogOpen(open);
    if (!open) { setForm(EMPTY_FORM); setErrors({}); }
  };

  const field = (key: keyof FormData, label: string, type = 'text') => (
    <div className="space-y-1">
      <Label>{label} <span className="text-destructive">*</span></Label>
      <Input
        type={type}
        value={form[key]}
        onChange={e => setForm(prev => ({ ...prev, [key]: e.target.value }))}
        placeholder={`请输入${label}`}
      />
      {errors[key] && <p className="text-xs text-destructive">{errors[key]}</p>}
    </div>
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-36">
            <SelectValue placeholder="全部状态" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部状态</SelectItem>
            <SelectItem value="active">正常</SelectItem>
            <SelectItem value="inactive">停用</SelectItem>
            <SelectItem value="pending">待审核</SelectItem>
          </SelectContent>
        </Select>

        <Dialog open={dialogOpen} onOpenChange={handleOpenChange}>
          <DialogTrigger asChild>
            <Button size="sm"><PlusIcon className="mr-1 h-4 w-4" />新增租户</Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-lg">
            <DialogHeader>
              <DialogTitle>新增高校租户</DialogTitle>
            </DialogHeader>
            <div className="grid grid-cols-2 gap-3 py-2">
              <div className="col-span-2">{field('name', '名称')}</div>
              <div className="col-span-2">{field('domain', '域名')}</div>
              {field('admin_username', '管理员用户名')}
              {field('admin_email', '管理员邮箱', 'email')}
              {field('max_students', '最大学生数', 'number')}
              {field('expire_date', '到期日期', 'date')}
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setDialogOpen(false)}>取消</Button>
              <Button onClick={handleSubmit} disabled={submitting}>
                {submitting ? '提交中...' : '确认创建'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {loading ? (
        <div className="space-y-2">
          {[1, 2, 3, 4].map(i => <Skeleton key={i} className="h-12 w-full rounded" />)}
        </div>
      ) : (
        <div className="rounded-lg border overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">名称</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">类型</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">状态</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">管理员</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">到期时间</th>
                <th className="text-right px-4 py-3 font-medium text-muted-foreground">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {tenants.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center text-muted-foreground py-8">暂无租户数据</td>
                </tr>
              ) : tenants.map(t => (
                <tr key={t.id} className="hover:bg-muted/30 transition-colors">
                  <td className="px-4 py-3 font-medium">{t.name}</td>
                  <td className="px-4 py-3">
                    <Badge className={TYPE_COLORS[t.type]}>{TYPE_LABELS[t.type]}</Badge>
                  </td>
                  <td className="px-4 py-3">
                    <Badge className={STATUS_COLORS[t.status]}>{STATUS_LABELS[t.status]}</Badge>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">{t.admin_username}</td>
                  <td className="px-4 py-3 text-muted-foreground">
                    {t.expire_date ? t.expire_date.slice(0, 10) : '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Button variant="ghost" size="sm">编辑</Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
