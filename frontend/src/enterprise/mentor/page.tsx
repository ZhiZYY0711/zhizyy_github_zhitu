import { useState, useEffect } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Code2, FileText, GraduationCap } from 'lucide-react';
import MentorStats from './components/MentorStats';
import CodeReviewList from './components/CodeReviewList';
import WeeklyReportList from '../internship/components/WeeklyReportList';
import { fetchMentorDashboard } from '../services/api';
import type { MentorDashboard } from '../types';

export default function MentorPage() {
  const [dashboard, setDashboard] = useState<MentorDashboard | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMentorDashboard().then(setDashboard).finally(() => setLoading(false));
  }, []);

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">导师工作站</h1>
        <p className="text-muted-foreground mt-1">指导学生进度，完成代码评审与周报批阅</p>
      </div>

      {loading ? (
        <div className="grid grid-cols-3 gap-4">{Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="h-24 bg-muted animate-pulse rounded-lg" />
        ))}</div>
      ) : dashboard && <MentorStats data={dashboard} />}

      {/* Student List */}
      {dashboard && dashboard.students?.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <GraduationCap className="h-4 w-4" />
              指导学生 ({dashboard.students?.length || 0})
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {dashboard.students?.map(s => (
                <div key={s.student_id} className="border rounded-lg p-3 flex items-center gap-3">
                  <div className="h-9 w-9 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                    {s.student_name.slice(0, 2)}
                  </div>
                  <div className="min-w-0">
                    <p className="font-medium text-sm truncate">{s.student_name}</p>
                    <p className="text-xs text-muted-foreground truncate">{s.project_name ?? s.position}</p>
                  </div>
                  <Badge variant="outline" className="shrink-0 text-xs">
                    {s.type === 'training' ? '实训' : '实习'}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <Tabs defaultValue="code-review">
        <TabsList>
          <TabsTrigger value="code-review" className="flex items-center gap-2">
            <Code2 className="h-4 w-4" />代码评审
          </TabsTrigger>
          <TabsTrigger value="weekly-reports" className="flex items-center gap-2">
            <FileText className="h-4 w-4" />周报批阅
          </TabsTrigger>
        </TabsList>
        <TabsContent value="code-review" className="mt-6"><CodeReviewList /></TabsContent>
        <TabsContent value="weekly-reports" className="mt-6"><WeeklyReportList /></TabsContent>
      </Tabs>
    </div>
  );
}
