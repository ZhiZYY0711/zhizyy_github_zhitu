import { useEffect, useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  fetchRecommendationBanners,
  saveRecommendationBanner,
  fetchTopListItems,
  saveTopListItems,
} from '../../services/api';
import type { RecommendationBanner, TopListItem } from '../../types';

// ── Banner Section ────────────────────────────────────────────────────────────

const TARGET_TYPE_STYLES: Record<RecommendationBanner['target_type'], string> = {
  project: 'bg-blue-100 text-blue-700 border-blue-200',
  enterprise: 'bg-purple-100 text-purple-700 border-purple-200',
  course: 'bg-orange-100 text-orange-700 border-orange-200',
};

const TARGET_TYPE_LABELS: Record<RecommendationBanner['target_type'], string> = {
  project: '项目',
  enterprise: '企业',
  course: '课程',
};

const STATUS_STYLES: Record<RecommendationBanner['status'], string> = {
  active: 'bg-green-100 text-green-700 border-green-200',
  inactive: 'bg-gray-100 text-gray-600 border-gray-200',
};

const STATUS_LABELS: Record<RecommendationBanner['status'], string> = {
  active: '启用',
  inactive: '停用',
};

interface BannerFormData {
  target_type: RecommendationBanner['target_type'] | '';
  target_id: string;
  title: string;
  image_url: string;
  order: string;
}

const DEFAULT_FORM: BannerFormData = {
  target_type: '',
  target_id: '',
  title: '',
  image_url: '',
  order: '1',
};

function BannerSection() {
  const [banners, setBanners] = useState<RecommendationBanner[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState<BannerFormData>(DEFAULT_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchRecommendationBanners().then((data: RecommendationBanner[]) => {
      setBanners(data.sort((a, b) => a.order - b.order));
      setLoading(false);
    });
  }, []);

  const moveItem = (index: number, direction: 'up' | 'down') => {
    const next = [...banners];
    const swapIndex = direction === 'up' ? index - 1 : index + 1;
    if (swapIndex < 0 || swapIndex >= next.length) return;
    [next[index], next[swapIndex]] = [next[swapIndex], next[index]];
    // update order field to reflect new positions
    setBanners(next.map((b, i) => ({ ...b, order: i + 1 })));
  };

  const handleSave = async () => {
    if (!form.target_type || !form.target_id || !form.title) return;
    setSaving(true);
    await saveRecommendationBanner({
      target_type: form.target_type,
      target_id: form.target_id,
      title: form.title,
      image_url: form.image_url,
      order: Number(form.order),
      status: 'active',
    });
    setSaving(false);
    setDialogOpen(false);
    setForm(DEFAULT_FORM);
    // refresh
    setLoading(true);
    fetchRecommendationBanners().then((data: RecommendationBanner[]) => {
      setBanners(data.sort((a, b) => a.order - b.order));
      setLoading(false);
    });
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button size="sm" onClick={() => setDialogOpen(true)}>+ 新增推荐位</Button>
      </div>

      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => <Skeleton key={i} className="h-20 w-full rounded-lg" />)}
        </div>
      ) : banners.length === 0 ? (
        <p className="text-center text-muted-foreground py-8">暂无推荐位</p>
      ) : (
        <div className="space-y-3">
          {banners.map((banner, index) => (
            <div key={banner.id} className="flex items-center gap-4 border rounded-lg p-4">
              {banner.image_url && (
                <div className="w-16 h-12 rounded border overflow-hidden flex-shrink-0 bg-muted">
                  <img
                    src={banner.image_url}
                    alt={banner.title}
                    className="w-full h-full object-cover"
                    onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
                  />
                </div>
              )}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <p className="font-medium text-sm">{banner.title}</p>
                  <Badge className={TARGET_TYPE_STYLES[banner.target_type]}>
                    {TARGET_TYPE_LABELS[banner.target_type]}
                  </Badge>
                  <Badge className={STATUS_STYLES[banner.status]}>
                    {STATUS_LABELS[banner.status]}
                  </Badge>
                </div>
                <p className="text-xs text-muted-foreground mt-1">排序：{banner.order}</p>
              </div>
              <div className="flex gap-1 flex-shrink-0">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={index === 0}
                  onClick={() => moveItem(index, 'up')}
                >
                  上移
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={index === banners.length - 1}
                  onClick={() => moveItem(index, 'down')}
                >
                  下移
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>新增推荐位</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1">
              <Label>目标类型</Label>
              <Select
                value={form.target_type}
                onValueChange={v => setForm(f => ({ ...f, target_type: v as RecommendationBanner['target_type'] }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="请选择目标类型" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="project">项目</SelectItem>
                  <SelectItem value="enterprise">企业</SelectItem>
                  <SelectItem value="course">课程</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1">
              <Label>目标 ID</Label>
              <Input
                value={form.target_id}
                onChange={e => setForm(f => ({ ...f, target_id: e.target.value }))}
                placeholder="请输入目标 ID"
              />
            </div>
            <div className="space-y-1">
              <Label>标题</Label>
              <Input
                value={form.title}
                onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
                placeholder="请输入标题"
              />
            </div>
            <div className="space-y-1">
              <Label>图片 URL</Label>
              <Input
                value={form.image_url}
                onChange={e => setForm(f => ({ ...f, image_url: e.target.value }))}
                placeholder="请输入图片 URL"
              />
            </div>
            <div className="space-y-1">
              <Label>排序</Label>
              <Input
                type="number"
                min={1}
                value={form.order}
                onChange={e => setForm(f => ({ ...f, order: e.target.value }))}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>取消</Button>
            <Button onClick={handleSave} disabled={saving}>
              {saving ? '保存中...' : '保存'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

// ── Top List Section ──────────────────────────────────────────────────────────

type TopListType = 'mentor' | 'course' | 'project';

const TOP_LIST_LABELS: Record<TopListType, string> = {
  mentor: '导师榜',
  course: '课程榜',
  project: '项目榜',
};

function TopListPanel({ listType }: { listType: TopListType }) {
  const [items, setItems] = useState<TopListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setLoading(true);
    fetchTopListItems(listType).then((data: TopListItem[]) => {
      setItems(data.sort((a, b) => a.order - b.order));
      setLoading(false);
    });
  }, [listType]);

  const moveItem = (index: number, direction: 'up' | 'down') => {
    const next = [...items];
    const swapIndex = direction === 'up' ? index - 1 : index + 1;
    if (swapIndex < 0 || swapIndex >= next.length) return;
    [next[index], next[swapIndex]] = [next[swapIndex], next[index]];
    setItems(next.map((item, i) => ({ ...item, order: i + 1 })));
  };

  const handleSave = async () => {
    setSaving(true);
    await saveTopListItems(listType, items.map(i => i.item_id));
    setSaving(false);
  };

  if (loading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3].map(i => <Skeleton key={i} className="h-12 w-full rounded-lg" />)}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {items.length === 0 ? (
        <p className="text-center text-muted-foreground py-8">暂无置顶项目</p>
      ) : (
        <div className="space-y-2">
          {items.map((item, index) => (
            <div key={item.id} className="flex items-center gap-3 border rounded-lg px-4 py-3">
              <span className="text-sm font-medium text-muted-foreground w-6 text-center">
                {index + 1}
              </span>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{item.item_name}</p>
                {item.reason && (
                  <p className="text-xs text-muted-foreground truncate">{item.reason}</p>
                )}
              </div>
              <div className="flex gap-1 flex-shrink-0">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={index === 0}
                  onClick={() => moveItem(index, 'up')}
                >
                  上移
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={index === items.length - 1}
                  onClick={() => moveItem(index, 'down')}
                >
                  下移
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
      <div className="flex justify-end">
        <Button size="sm" onClick={handleSave} disabled={saving}>
          {saving ? '保存中...' : '保存'}
        </Button>
      </div>
    </div>
  );
}

function TopListSection() {
  return (
    <Tabs defaultValue="mentor">
      <TabsList>
        {(Object.keys(TOP_LIST_LABELS) as TopListType[]).map(type => (
          <TabsTrigger key={type} value={type}>{TOP_LIST_LABELS[type]}</TabsTrigger>
        ))}
      </TabsList>
      {(Object.keys(TOP_LIST_LABELS) as TopListType[]).map(type => (
        <TabsContent key={type} value={type} className="mt-4">
          <TopListPanel listType={type} />
        </TabsContent>
      ))}
    </Tabs>
  );
}

// ── Main Component ────────────────────────────────────────────────────────────

export default function RecommendationManager() {
  return (
    <Tabs defaultValue="banner">
      <TabsList>
        <TabsTrigger value="banner">首页推荐位（Banner）</TabsTrigger>
        <TabsTrigger value="toplist">置顶榜单</TabsTrigger>
      </TabsList>
      <TabsContent value="banner" className="mt-4">
        <BannerSection />
      </TabsContent>
      <TabsContent value="toplist" className="mt-4">
        <TopListSection />
      </TabsContent>
    </Tabs>
  );
}
