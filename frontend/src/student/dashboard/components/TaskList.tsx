// f:\projects\zhitu\frontend\src\student\dashboard\components\TaskList.tsx
import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { CalendarClock, FileText, Briefcase, ChevronRight } from "lucide-react";
import type { TaskItem } from "../../mock/generator";

interface TaskListProps {
  tasks: TaskItem[];
  loading: boolean;
}

const TaskList: React.FC<TaskListProps> = ({ tasks, loading }) => {
  if (loading) {
    return (
      <Card className="col-span-1 h-[400px] animate-pulse">
        <CardHeader>
          <div className="h-6 w-32 bg-gray-200 rounded"></div>
        </CardHeader>
        <CardContent className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-16 bg-gray-200 rounded"></div>
          ))}
        </CardContent>
      </Card>
    );
  }

  const getIcon = (type: TaskItem['type']) => {
    switch (type) {
      case 'weekly_report': return <FileText className="h-4 w-4" />;
      case 'interview_invite': return <Briefcase className="h-4 w-4" />;
      case 'training_submit': return <CalendarClock className="h-4 w-4" />;
      default: return <FileText className="h-4 w-4" />;
    }
  };

  const getPriorityColor = (priority: TaskItem['priority']) => {
    switch (priority) {
      case 'high': return "destructive";
      case 'medium': return "default"; // or yellow if custom variant exists
      case 'low': return "secondary";
      default: return "secondary";
    }
  };

  return (
    <Card className="col-span-1 h-full flex flex-col">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>任务指挥中心</CardTitle>
            <CardDescription>待办事项与提醒</CardDescription>
          </div>
          <Badge variant="outline">{tasks.length} 待办</Badge>
        </div>
      </CardHeader>
      <CardContent className="flex-1 overflow-auto">
        {tasks.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
            <p>暂无待办事项</p>
          </div>
        ) : (
          <div className="space-y-4">
            {tasks.map((task) => (
              <div key={task.id} className="flex items-start justify-between p-3 border rounded-lg hover:bg-accent transition-colors">
                <div className="flex items-start gap-3">
                  <div className="mt-1 p-2 bg-muted rounded-full">
                    {getIcon(task.type)}
                  </div>
                  <div className="space-y-1">
                    <p className="font-medium leading-none">{task.title}</p>
                    <p className="text-xs text-muted-foreground">
                      截止: {new Date(task.deadline).toLocaleDateString()}
                    </p>
                  </div>
                </div>
                <div className="flex flex-col items-end gap-2">
                  <Badge variant={getPriorityColor(task.priority) as any} className="text-[10px] px-1.5 py-0.5 h-auto">
                    {task.priority.toUpperCase()}
                  </Badge>
                  <Button variant="ghost" size="icon" className="h-6 w-6" asChild>
                    <a href={task.jump_url}>
                      <ChevronRight className="h-4 w-4" />
                    </a>
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default TaskList;
