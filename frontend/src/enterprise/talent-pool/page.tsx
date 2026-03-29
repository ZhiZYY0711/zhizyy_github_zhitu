import { useState, useEffect } from 'react';
import { Trash2, Send, GraduationCap, ChevronLeft, ChevronRight } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { fetchTalentPool, removeFromTalentPool } from '../services/api';
import type { TalentPoolItem } from '../types';

const ALL_TAGS = ['潜力股', '25届', '26届', '技术强', '沟通好', '有实习经验', '竞赛获奖'];

const PAGE_SIZE_OPTIONS = [9, 18, 27];

export default function TalentPoolPage() {
  const [students, setStudents] = useState<TalentPoolItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [tagFilter, setTagFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(9);
  const [total, setTotal] = useState(0);

  const load = async () => {
    setLoading(true);
    try {
      const result = await fetchTalentPool({ page: currentPage, size: pageSize });
      setStudents(result.records);
      setTotal(result.total);
    } finally { setLoading(false); }
  };

  useEffect(() => {
    setCurrentPage(1);
  }, [tagFilter]);

  useEffect(() => { load(); }, [currentPage, pageSize]);

  const handleRemove = async (id: string) => {
    await removeFromTalentPool(id);
    setStudents(prev => prev.filter(s => s.id !== id));
    setTotal(prev => prev - 1);
  };

  const filtered = tagFilter === 'all' ? students : students.filter(s => s.tags.includes(tagFilter));

  const totalPages = Math.ceil(total / pageSize);

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">人才蓄水池</h1>
        <p className="text-muted-foreground mt-1">储备优质候选人，建立企业专属人才库</p>
      </div>

      <div className="flex items-center gap-3">
        <Select value={tagFilter} onValueChange={setTagFilter}>
          <SelectTrigger className="w-40">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">全部标签</SelectItem>
            {ALL_TAGS.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}
          </SelectContent>
        </Select>
        <span className="text-sm text-muted-foreground">共 {total} 位候选人</span>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-40 bg-muted animate-pulse rounded-lg" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-16 text-muted-foreground">
          <GraduationCap className="h-12 w-12 mx-auto mb-3 opacity-30" />
          <p>暂无候选人，在简历筛选中收藏优质候选人</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map(student => (
            <Card key={student.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-5">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-sm">
                      {student.student_name.slice(0, 2)}
                    </div>
                    <div>
                      <p className="font-semibold">{student.student_name}</p>
                      <p className="text-xs text-muted-foreground">{student.school} · {student.major}</p>
                    </div>
                  </div>
                  <Button variant="ghost" size="icon" className="h-7 w-7 text-red-400 hover:text-red-600" onClick={() => handleRemove(student.id)}>
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </div>

                <div className="mt-3 flex flex-wrap gap-1.5">
                  {student.tags.map(tag => (
                    <Badge key={tag} variant="outline" className="text-xs">{tag}</Badge>
                  ))}
                </div>

                <div className="mt-3 flex flex-wrap gap-1">
                  {student.skills.slice(0, 4).map(skill => (
                    <span key={skill} className="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded">{skill}</span>
                  ))}
                </div>

                {student.notes && (
                  <p className="mt-3 text-xs text-muted-foreground border-t pt-2 line-clamp-2">{student.notes}</p>
                )}

                <div className="mt-3 flex items-center justify-between">
                  <span className="text-xs text-muted-foreground">收藏于 {student.collect_time.slice(0, 10)}</span>
                  <Button variant="outline" size="sm" className="h-7 text-xs">
                    <Send className="h-3 w-3 mr-1" />发送邀约
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
          <div className="text-sm text-muted-foreground">
            共 {total} 条记录，第 {currentPage} / {totalPages} 页
          </div>
          <div className="flex items-center gap-2">
            <Select value={String(pageSize)} onValueChange={(v) => { setPageSize(Number(v)); setCurrentPage(1); }}>
              <SelectTrigger className="w-20 h-8">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {PAGE_SIZE_OPTIONS.map(size => (
                  <SelectItem key={size} value={String(size)}>{size} 条/页</SelectItem>
                ))}
              </SelectContent>
            </Select>
            <div className="flex items-center gap-1">
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(p => p - 1)}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  let pageNum;
                  if (totalPages <= 5) {
                    pageNum = i + 1;
                  } else if (currentPage <= 3) {
                    pageNum = i + 1;
                  } else if (currentPage >= totalPages - 2) {
                    pageNum = totalPages - 4 + i;
                  } else {
                    pageNum = currentPage - 2 + i;
                  }

                  return (
                    <Button
                      key={pageNum}
                      variant={currentPage === pageNum ? "default" : "outline"}
                      size="icon"
                      className="h-8 w-8"
                      onClick={() => setCurrentPage(pageNum)}
                    >
                      {pageNum}
                    </Button>
                  );
                })}
              </div>
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(p => p + 1)}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
