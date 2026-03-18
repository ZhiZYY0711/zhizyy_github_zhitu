import { useState, useEffect } from 'react';
import { Plus, Star, ChevronDown, ChevronUp, Users } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { fetchTrainingProjects, createTrainingProject, fetchProjectTeams } from '../services/api';
import type { TrainingProject, ProjectTeam } from '../types';

const STATUS_CONFIG = {
  draft: { label: '草稿', className: 'bg-gray-100 text-gray-600 border-gray-200' },
  recruiting: { label: '招募中', className: 'bg-blue-100 text-blue-700 border-blue-200' },
  in_progress: { label: '进行中', className: 'bg-green-100 text-green-700 border-green-200' },
  completed: { label: '已完成', className: 'bg-purple-100 text-purple-700 border-purple-200' },
};

function StarRating({ value }: { value: number }) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: 5 }).map((_, i) => (
        <Star key={i} className={`h-3.5 w-3.5 ${i < value ? 'fill-yellow-400 text-yellow-400' : 'text-gray-200'}`} />
      ))}
    </div>
  );
}

export default function TrainingPage() {
  const [projects, setProjects] = useState<TrainingProject[]>([]);
  const [loading, setLoading] = useState(false);
  const [showCreate, setShowCreate] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [teams, setTeams] = useState<Record<string, ProjectTeam[]>>({});
  const [form, setForm] = useState({ name: '', description: '', difficulty: '3', tech_stack: '', max_teams: '10' });

  const load = async () => {
    setLoading(true);
    try { setProjects(await fetchTrainingProjects()); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const toggleExpand = async (id: string) => {
    if (expandedId === id) { setExpandedId(null); return; }
    setExpandedId(id);
    if (!teams[id]) {
      const data = await fetchProjectTeams(id);
      setTeams(prev => ({ ...prev, [id]: data }));
    }
  };

  const handleCreate = async () => {
    setSubmitting(true);
    try {
      await createTrainingProject({
        ...form,
        difficulty: parseInt(form.difficulty),
        max_teams: parseInt(form.max_teams),
        tech_stack: form.tech_stack.split(',').map(s => s.trim()).filter(Boolean),
      });
      setShowCreate(false);
      setForm({ name: '', description: '', difficulty: '3', tech_stack: '', max_teams: '10' });
      load();
    } finally { setSubmitting(false); }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">实训项目管理</h1>
          <p className="text-muted-foreground mt-1">发布企业实训项目，通过实训选拔人才</p>
        </div>
        <Button onClick={() => setShowCreate(true)} className="bg-blue-600 hover:bg-blue-700">
          <Plus className="h-4 w-4 mr-2" />发布项目
        </Button>
      </div>

      {loading ? (
        <div className="space-y-4">{Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="h-32 bg-muted animate-pulse rounded-lg" />
        ))}</div>
      ) : projects.map(project => (
        <Card key={project.id}>
          <CardHeader className="pb-3">
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <CardTitle className="text-base">{project.name}</CardTitle>
                  <Badge className={STATUS_CONFIG[project.status].className}>{STATUS_CONFIG[project.status].label}</Badge>
                </div>
                <p className="text-sm text-muted-foreground mt-1 line-clamp-2">{project.description}</p>
              </div>
              <Button variant="ghost" size="sm" onClick={() => toggleExpand(project.id)}>
                {expandedId === project.id ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                团队列表
              </Button>
            </div>
          </CardHeader>
          <CardContent className="pt-0">
            <div className="flex items-center gap-6 flex-wrap text-sm">
              <div className="flex items-center gap-2">
                <span className="text-muted-foreground">难度：</span>
                <StarRating value={project.difficulty} />
              </div>
              <div className="flex items-center gap-2">
                <span className="text-muted-foreground">参与团队：</span>
                <span className="font-medium">{project.current_teams} / {project.max_teams}</span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {project.tech_stack.map(t => (
                  <Badge key={t} variant="outline" className="text-xs">{t}</Badge>
                ))}
              </div>
            </div>

            {expandedId === project.id && (
              <div className="mt-4 border-t pt-4 space-y-3">
                <p className="text-sm font-medium text-muted-foreground">参与团队</p>
                {(teams[project.id] ?? []).map(team => (
                  <div key={team.team_id} className="border rounded-lg p-3">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <Users className="h-4 w-4 text-muted-foreground" />
                        <span className="font-medium text-sm">{team.team_name}</span>
                        {team.mentor_name && <Badge variant="outline" className="text-xs">导师：{team.mentor_name}</Badge>}
                      </div>
                      <span className="text-xs text-muted-foreground">{team.progress}%</span>
                    </div>
                    <Progress value={team.progress} className="h-1.5 mb-2" />
                    <div className="flex flex-wrap gap-1.5">
                      {team.members.map(m => (
                        <span key={m.student_id} className={`text-xs px-2 py-0.5 rounded ${m.role === 'leader' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-600'}`}>
                          {m.student_name}{m.role === 'leader' ? '（组长）' : ''}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      ))}

      <Dialog open={showCreate} onOpenChange={setShowCreate}>
        <DialogContent className="max-w-lg">
          <DialogHeader><DialogTitle>发布实训项目</DialogTitle></DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1.5">
              <Label>项目名称</Label>
              <Input placeholder="如：电商高并发秒杀系统" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>项目描述</Label>
              <Textarea rows={3} placeholder="描述项目背景和目标..." value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>难度等级</Label>
                <Select value={form.difficulty} onValueChange={v => setForm(f => ({ ...f, difficulty: v }))}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {[1, 2, 3, 4, 5].map(n => <SelectItem key={n} value={String(n)}>{'★'.repeat(n)}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>最大团队数</Label>
                <Input type="number" min={1} max={50} value={form.max_teams} onChange={e => setForm(f => ({ ...f, max_teams: e.target.value }))} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>技术栈（逗号分隔）</Label>
              <Input placeholder="Spring Boot, Redis, MySQL" value={form.tech_stack} onChange={e => setForm(f => ({ ...f, tech_stack: e.target.value }))} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowCreate(false)}>取消</Button>
            <Button onClick={handleCreate} disabled={submitting || !form.name} className="bg-blue-600 hover:bg-blue-700">
              {submitting ? '发布中...' : '发布'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
