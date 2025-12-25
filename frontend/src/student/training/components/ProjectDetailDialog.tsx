import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Building2, Users, Code, Calendar, FileText } from "lucide-react";
import type { Project } from "../../mock/generator";

interface ProjectDetailDialogProps {
  project: Project | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onApply: (project: Project) => void;
}

const ProjectDetailDialog: React.FC<ProjectDetailDialogProps> = ({ project, open, onOpenChange, onApply }) => {
  if (!project) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-start justify-between gap-4">
            <div>
              <DialogTitle className="text-2xl font-bold">{project.name}</DialogTitle>
              <div className="flex items-center text-muted-foreground mt-2">
                <Building2 className="w-4 h-4 mr-2" />
                {project.provider}
              </div>
            </div>
            <Badge variant={project.status === 'recruiting' ? 'default' : 'secondary'} className="mt-1">
              {project.status === 'recruiting' ? '招募中' : project.status === 'ongoing' ? '进行中' : '已结束'}
            </Badge>
          </div>
        </DialogHeader>

        <div className="space-y-6 py-4">
          <div className="grid grid-cols-2 gap-4">
             <div className="space-y-1">
               <span className="text-sm text-muted-foreground">难度等级</span>
               <div className="flex text-yellow-500">
                 {Array.from({ length: 5 }).map((_, i) => (
                    <span key={i} className={i < project.difficulty ? "text-yellow-500" : "text-gray-200"}>★</span>
                 ))}
               </div>
             </div>
             <div className="space-y-1">
               <span className="text-sm text-muted-foreground">团队规模</span>
               <div className="flex items-center">
                 <Users className="w-4 h-4 mr-2 text-gray-400" />
                 <span>{project.current_members} / {project.team_size} 人</span>
               </div>
             </div>
          </div>

          <Separator />

          <div>
            <h3 className="font-semibold mb-2 flex items-center">
              <FileText className="w-4 h-4 mr-2 text-primary" />
              项目描述
            </h3>
            <p className="text-sm text-gray-600 leading-relaxed">
              {project.description || "暂无详细描述。该项目旨在帮助学生掌握企业级开发流程，从需求分析到最终交付。"}
            </p>
          </div>

          <div>
             <h3 className="font-semibold mb-2 flex items-center">
              <Code className="w-4 h-4 mr-2 text-primary" />
              技术栈
            </h3>
            <div className="flex flex-wrap gap-2">
              {project.tech_stack.map((stack) => (
                <Badge key={stack} variant="secondary">
                  {stack}
                </Badge>
              ))}
            </div>
          </div>
          
           <div>
             <h3 className="font-semibold mb-2 flex items-center">
              <Calendar className="w-4 h-4 mr-2 text-primary" />
              实训周期
            </h3>
            <p className="text-sm text-gray-600">
               预计 8 周，每周需投入约 10-15 小时。
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>关闭</Button>
          <Button onClick={() => onApply(project)} disabled={project.status !== 'recruiting'}>
            {project.status === 'recruiting' ? '立即申请' : '报名已截止'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default ProjectDetailDialog;
