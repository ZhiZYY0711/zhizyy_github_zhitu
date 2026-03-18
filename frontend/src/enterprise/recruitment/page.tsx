import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Briefcase, FileText } from 'lucide-react';
import JobList from './components/JobList';
import ApplicationList from './components/ApplicationList';

export default function RecruitmentPage() {
  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">招聘管理</h1>
        <p className="text-muted-foreground mt-1">管理职位发布与候选人简历筛选</p>
      </div>

      <Tabs defaultValue="jobs">
        <TabsList>
          <TabsTrigger value="jobs" className="flex items-center gap-2">
            <Briefcase className="h-4 w-4" />职位管理
          </TabsTrigger>
          <TabsTrigger value="applications" className="flex items-center gap-2">
            <FileText className="h-4 w-4" />简历筛选
          </TabsTrigger>
        </TabsList>
        <TabsContent value="jobs" className="mt-6"><JobList /></TabsContent>
        <TabsContent value="applications" className="mt-6"><ApplicationList /></TabsContent>
      </Tabs>
    </div>
  );
}
