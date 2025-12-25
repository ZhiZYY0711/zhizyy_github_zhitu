import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Users, Code, Building2 } from "lucide-react";
import type { Project } from "../../mock/generator";

interface ProjectCardProps {
  project: Project;
  onViewDetails: (project: Project) => void;
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project, onViewDetails }) => {
  return (
    <Card className="flex flex-col h-full hover:shadow-lg transition-shadow">
      <CardHeader>
        <div className="flex justify-between items-start">
          <div>
            <CardTitle className="text-lg font-bold line-clamp-1">{project.name}</CardTitle>
            <div className="flex items-center text-sm text-muted-foreground mt-1">
              <Building2 className="w-4 h-4 mr-1" />
              {project.provider}
            </div>
          </div>
          <Badge variant={project.status === 'recruiting' ? 'default' : 'secondary'}>
            {project.status === 'recruiting' ? '招募中' : project.status === 'ongoing' ? '进行中' : '已结束'}
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="flex-1 space-y-4">
        <p className="text-sm text-gray-600 line-clamp-2 min-h-[40px]">
          {project.description}
        </p>
        
        <div className="space-y-2">
          <div className="flex items-center text-sm">
            <span className="font-semibold w-16">难度:</span>
            <div className="flex">
              {Array.from({ length: 5 }).map((_, i) => (
                <span key={i} className={i < project.difficulty ? "text-yellow-500" : "text-gray-300"}>★</span>
              ))}
            </div>
          </div>
          
          <div className="flex items-center text-sm">
            <Users className="w-4 h-4 mr-2 text-muted-foreground" />
            <span>{project.current_members} / {project.team_size} 人</span>
          </div>

          <div className="flex flex-wrap gap-1 mt-2">
            <Code className="w-4 h-4 mr-1 text-muted-foreground self-center" />
            {project.tech_stack.map((stack) => (
              <Badge key={stack} variant="outline" className="text-xs">
                {stack}
              </Badge>
            ))}
          </div>
        </div>
      </CardContent>
      <CardFooter>
        <Button className="w-full" onClick={() => onViewDetails(project)}>查看详情</Button>
      </CardFooter>
    </Card>
  );
};

export default ProjectCard;
