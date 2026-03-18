import { useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import StudentList from './components/StudentList';
import TrainingPlanList from './components/TrainingPlanList';

const TeachingPage = () => {
  const [tab, setTab] = useState('students');

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold">教学教务管理</h1>
        <p className="text-muted-foreground text-sm mt-1">学生管理 · 实训课程排期 · 导师分配</p>
      </div>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="students">学生管理</TabsTrigger>
          <TabsTrigger value="plans">实训计划</TabsTrigger>
        </TabsList>

        <TabsContent value="students" className="mt-4">
          <StudentList />
        </TabsContent>

        <TabsContent value="plans" className="mt-4">
          <TrainingPlanList />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default TeachingPage;
