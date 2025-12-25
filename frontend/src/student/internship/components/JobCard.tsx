import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { MapPin, Briefcase, Banknote, Building2 } from "lucide-react";
import type { Job } from "../../mock/generator";

interface JobCardProps {
  job: Job;
  onApply: (job: Job) => void;
}

const JobCard: React.FC<JobCardProps> = ({ job, onApply }) => {
  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardHeader className="pb-3">
        <div className="flex justify-between items-start">
          <div className="flex items-start gap-3">
            <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <Building2 className="w-6 h-6 text-primary" />
            </div>
            <div>
              <CardTitle className="text-base font-bold">{job.title}</CardTitle>
              <div className="text-sm text-muted-foreground mt-1">{job.company}</div>
            </div>
          </div>
          <Badge variant="outline" className="text-primary border-primary/20 bg-primary/5">
             {job.match_score}% 匹配
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="pb-3 space-y-3">
        <div className="flex items-center gap-4 text-sm text-gray-600">
          <div className="flex items-center gap-1">
            <MapPin className="w-4 h-4" />
            {job.city}
          </div>
          <div className="flex items-center gap-1">
            <Briefcase className="w-4 h-4" />
            3-5天/周
          </div>
          <div className="flex items-center gap-1 text-orange-600 font-medium">
            <Banknote className="w-4 h-4" />
            {job.salary_min}-{job.salary_max}/天
          </div>
        </div>
        
        <div className="flex flex-wrap gap-2">
          {job.tags.map(tag => (
            <span key={tag} className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">
              {tag}
            </span>
          ))}
        </div>

        <div className="bg-yellow-50 text-yellow-800 text-xs p-2 rounded border border-yellow-100">
           推荐理由: {job.match_reason}
        </div>
      </CardContent>
      <CardFooter>
        <Button className="w-full h-8" onClick={() => onApply(job)}>立即投递</Button>
      </CardFooter>
    </Card>
  );
};

export default JobCard;
