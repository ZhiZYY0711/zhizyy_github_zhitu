import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { UserCheck, FileText } from 'lucide-react';
import InternList from './components/InternList';
import WeeklyReportList from './components/WeeklyReportList';

export default function InternshipPage() {
  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">实习生管理</h1>
        <p className="text-muted-foreground mt-1">管理实习生全周期，从入职到离职</p>
      </div>

      <Tabs defaultValue="interns">
        <TabsList>
          <TabsTrigger value="interns" className="flex items-center gap-2">
            <UserCheck className="h-4 w-4" />实习生列表
          </TabsTrigger>
          <TabsTrigger value="reports" className="flex items-center gap-2">
            <FileText className="h-4 w-4" />周报管理
          </TabsTrigger>
        </TabsList>
        <TabsContent value="interns" className="mt-6"><InternList /></TabsContent>
        <TabsContent value="reports" className="mt-6"><WeeklyReportList /></TabsContent>
      </Tabs>
    </div>
  );
}
