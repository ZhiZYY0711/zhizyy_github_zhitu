import { useEffect, useState } from 'react';
import { PlusIcon, Trash2Icon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
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
import { fetchTags, createTag, deleteTag } from '../../services/api';
import type { Tag } from '../../types';

const CATEGORY_LABELS: Record<Tag['category'], string> = {
  industry: '行业',
  tech_stack: '技术栈',
  skill: '技能',
};

const CATEGORY_COLORS: Record<Tag['category'], string> = {
  industry: 'bg-blue-100 text-blue-700 border-blue-200',
  tech_stack: 'bg-purple-100 text-purple-700 border-purple-200',
  skill: 'bg-green-100 text-green-700 border-green-200',
};

export default function TagManager() {
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterCategory, setFilterCategory] = useState<string>('all');
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [formCategory, setFormCategory] = useState<Tag['category']>('industry');
  const [formName, setFormName] = useState('');
  const [formErrors, setFormErrors] = useState<{ category?: string; name?: string }>({});
  const [submitting, setSubmitting] = useState(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);

  const loadTags = async () => {
    setLoading(true);
    try {
      const data = await fetchTags();
      setTags(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadTags(); }, []);

  const filteredTags = tags.filter(tag => {
    const matchCategory = filterCategory === 'all' || tag.category === filterCategory;
    const matchSearch = tag.name.toLowerCase().includes(search.toLowerCase());
    return matchCategory && matchSearch;
  });

  const grouped = filteredTags.reduce<Record<string, Tag[]>>((acc, tag) => {
    if (!acc[tag.category]) acc[tag.category] = [];
    acc[tag.category].push(tag);
    return acc;
  }, {});

  const validateForm = () => {
    const errors: { category?: string; name?: string } = {};
    if (!formCategory) errors.category = '请选择分类';
    if (!formName.trim()) errors.name = '请输入标签名称';
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleCreate = async () => {
    if (!validateForm()) return;
    setSubmitting(true);
    try {
      await createTag({ category: formCategory, name: formName.trim() });
      console.log(`标签 "${formName}" 创建成功`);
      setDialogOpen(false);
      setFormName('');
      setFormCategory('industry');
      setFormErrors({});
      await loadTags();
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    await deleteTag(id);
    console.log(`标签删除成功`);
    setDeleteConfirmId(null);
    setTags(prev => prev.filter(t => t.id !== id));
  };

  return (
    <div className="space-y-4">
      {/* 工具栏 */}
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div className="flex gap-2 flex-1">
          <Input
            placeholder="搜索标签..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="max-w-xs"
          />
          <Select value={filterCategory} onValueChange={setFilterCategory}>
            <SelectTrigger className="w-32">
              <SelectValue placeholder="全部分类" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部分类</SelectItem>
              <SelectItem value="industry">行业</SelectItem>
              <SelectItem value="tech_stack">技术栈</SelectItem>
              <SelectItem value="skill">技能</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <Dialog open={dialogOpen} onOpenChange={open => { setDialogOpen(open); if (!open) { setFormErrors({}); setFormName(''); setFormCategory('industry'); } }}>
          <DialogTrigger asChild>
            <Button size="sm"><PlusIcon className="mr-1 h-4 w-4" />新增标签</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>新增标签</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 py-2">
              <div className="space-y-1">
                <Label>分类 <span className="text-destructive">*</span></Label>
                <Select value={formCategory} onValueChange={v => setFormCategory(v as Tag['category'])}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="industry">行业</SelectItem>
                    <SelectItem value="tech_stack">技术栈</SelectItem>
                    <SelectItem value="skill">技能</SelectItem>
                  </SelectContent>
                </Select>
                {formErrors.category && <p className="text-xs text-destructive">{formErrors.category}</p>}
              </div>
              <div className="space-y-1">
                <Label>名称 <span className="text-destructive">*</span></Label>
                <Input
                  value={formName}
                  onChange={e => setFormName(e.target.value)}
                  placeholder="请输入标签名称"
                />
                {formErrors.name && <p className="text-xs text-destructive">{formErrors.name}</p>}
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setDialogOpen(false)}>取消</Button>
              <Button onClick={handleCreate} disabled={submitting}>
                {submitting ? '创建中...' : '确认创建'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* 标签列表 */}
      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => <Skeleton key={i} className="h-24 w-full rounded-lg" />)}
        </div>
      ) : (
        <div className="space-y-4">
          {(Object.keys(CATEGORY_LABELS) as Tag['category'][])
            .filter(cat => filterCategory === 'all' || cat === filterCategory)
            .map(cat => (
              grouped[cat]?.length ? (
                <div key={cat} className="border rounded-lg p-4 space-y-3">
                  <h3 className="text-sm font-semibold text-muted-foreground">{CATEGORY_LABELS[cat]}</h3>
                  <div className="flex flex-wrap gap-2">
                    {grouped[cat].map(tag => (
                      <div key={tag.id} className="flex items-center gap-1">
                        <Badge className={CATEGORY_COLORS[tag.category]}>{tag.name}</Badge>
                        <Dialog open={deleteConfirmId === tag.id} onOpenChange={open => !open && setDeleteConfirmId(null)}>
                          <DialogTrigger asChild>
                            <button
                              className="text-muted-foreground hover:text-destructive transition-colors"
                              onClick={() => setDeleteConfirmId(tag.id)}
                            >
                              <Trash2Icon className="h-3.5 w-3.5" />
                            </button>
                          </DialogTrigger>
                          <DialogContent>
                            <DialogHeader>
                              <DialogTitle>确认删除</DialogTitle>
                            </DialogHeader>
                            <p className="text-sm text-muted-foreground">确定要删除标签 <strong>{tag.name}</strong> 吗？此操作不可撤销。</p>
                            <DialogFooter>
                              <Button variant="outline" onClick={() => setDeleteConfirmId(null)}>取消</Button>
                              <Button variant="destructive" onClick={() => handleDelete(tag.id)}>确认删除</Button>
                            </DialogFooter>
                          </DialogContent>
                        </Dialog>
                      </div>
                    ))}
                  </div>
                </div>
              ) : null
            ))}
          {filteredTags.length === 0 && (
            <p className="text-center text-muted-foreground py-8">暂无标签数据</p>
          )}
        </div>
      )}
    </div>
  );
}
