import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { MoreHorizontal, Plus, Clock, CheckCircle2, Circle } from "lucide-react";
import type { ScrumBoard, ScrumTask } from "../../mock/generator";

interface ScrumBoardProps {
  board: ScrumBoard;
  loading?: boolean;
}

const ScrumBoardComponent: React.FC<ScrumBoardProps> = ({ board, loading }) => {
  if (loading) return <div>Loading board...</div>;

  const getPriorityColor = (priority: ScrumTask['priority']) => {
    switch (priority) {
      case 'high': return 'text-red-500 bg-red-50 border-red-200';
      case 'medium': return 'text-yellow-600 bg-yellow-50 border-yellow-200';
      case 'low': return 'text-blue-500 bg-blue-50 border-blue-200';
      default: return 'text-gray-500';
    }
  };

  const Column = ({ title, tasks, icon }: { title: string, tasks: ScrumTask[], icon: React.ReactNode }) => (
    <div className="flex flex-col w-80 min-w-[320px] bg-gray-50/50 rounded-lg p-3 h-full max-h-[calc(100vh-250px)]">
      <div className="flex items-center justify-between mb-3 px-1">
        <div className="flex items-center gap-2 font-semibold text-sm">
          {icon}
          {title}
          <span className="bg-gray-200 text-gray-600 px-2 py-0.5 rounded-full text-xs">
            {tasks.length}
          </span>
        </div>
        <Button variant="ghost" size="icon" className="h-6 w-6">
          <Plus className="h-4 w-4" />
        </Button>
      </div>
      
      <div className="flex-1 overflow-y-auto space-y-3 pr-1">
        {tasks.map(task => (
          <Card key={task.id} className="cursor-pointer hover:shadow-md transition-shadow">
            <CardContent className="p-3 space-y-3">
              <div className="flex justify-between items-start gap-2">
                <h4 className="text-sm font-medium leading-tight">{task.title}</h4>
                <Button variant="ghost" size="icon" className="h-6 w-6 -mr-2 -mt-2">
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Avatar className="h-6 w-6">
                    <AvatarFallback className="text-[10px] bg-primary/10 text-primary">
                      {task.assignee.slice(0, 2)}
                    </AvatarFallback>
                  </Avatar>
                  <span className="text-xs text-muted-foreground">{task.assignee}</span>
                </div>
                <Badge variant="outline" className={`text-[10px] px-1.5 py-0 ${getPriorityColor(task.priority)}`}>
                  {task.priority === 'high' ? '高' : task.priority === 'medium' ? '中' : '低'}
                </Badge>
              </div>
              
              <div className="flex items-center justify-between pt-2 border-t border-gray-100">
                <span className="text-xs text-gray-400">ID: {task.id}</span>
                <span className="text-xs bg-gray-100 px-2 py-0.5 rounded text-gray-600">
                  {task.story_points} SP
                </span>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );

  return (
    <div className="h-full flex flex-col">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold">{board.sprint_name}</h2>
        <div className="flex gap-2">
           <Button variant="outline" size="sm">燃尽图</Button>
           <Button size="sm">新建任务</Button>
        </div>
      </div>
      
      <div className="flex gap-4 overflow-x-auto pb-4 h-full items-start">
        <Column 
          title="待办 (Todo)" 
          tasks={board.columns.todo} 
          icon={<Circle className="w-4 h-4 text-gray-400" />} 
        />
        <Column 
          title="进行中 (In Progress)" 
          tasks={board.columns.in_progress} 
          icon={<Clock className="w-4 h-4 text-blue-500" />} 
        />
        <Column 
          title="审核 (Review)" 
          tasks={board.columns.review} 
          icon={<CheckCircle2 className="w-4 h-4 text-orange-500" />} 
        />
        <Column 
          title="已完成 (Done)" 
          tasks={board.columns.done} 
          icon={<CheckCircle2 className="w-4 h-4 text-green-500" />} 
        />
      </div>
    </div>
  );
};

export default ScrumBoardComponent;
