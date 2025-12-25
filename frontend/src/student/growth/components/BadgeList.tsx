import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge as BadgeUI } from "@/components/ui/badge";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { Award, Zap, MessageCircle, Star } from 'lucide-react';
import type { Badge } from '../../mock/generator';

interface BadgeListProps {
  badges: Badge[];
}

const BadgeList: React.FC<BadgeListProps> = ({ badges }) => {
  const getIcon = (category: string) => {
    switch (category) {
      case 'tech': return <Zap className="h-5 w-5" />;
      case 'soft': return <MessageCircle className="h-5 w-5" />;
      case 'special': return <Star className="h-5 w-5" />;
      default: return <Award className="h-5 w-5" />;
    }
  };

  const getColor = (category: string) => {
    switch (category) {
      case 'tech': return "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300";
      case 'soft': return "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300";
      case 'special': return "bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-300";
      default: return "bg-gray-100 text-gray-700";
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Award className="h-5 w-5 text-purple-500" />
          技能徽章墙
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <TooltipProvider>
            {badges.map((badge) => (
              <Tooltip key={badge.id}>
                <TooltipTrigger asChild>
                  <div className="flex flex-col items-center text-center p-4 rounded-lg border border-dashed hover:border-solid hover:border-purple-200 transition-all bg-slate-50/50 dark:bg-slate-900/20 cursor-help">
                    <div className={`p-3 rounded-full mb-3 ${getColor(badge.category)}`}>
                      {getIcon(badge.category)}
                    </div>
                    <h4 className="font-semibold text-sm mb-1">{badge.name}</h4>
                    <p className="text-xs text-muted-foreground line-clamp-2">
                      {badge.description}
                    </p>
                    <div className="mt-3">
                      <BadgeUI variant="secondary" className="text-[10px] h-5">
                        {badge.unlocked_at.split('-').slice(0, 2).join('.')}
                      </BadgeUI>
                    </div>
                  </div>
                </TooltipTrigger>
                <TooltipContent>
                  <p className="text-xs max-w-[200px]">{badge.description}</p>
                </TooltipContent>
              </Tooltip>
            ))}
          </TooltipProvider>

          {/* Empty slot placeholder */}
          <div className="flex flex-col items-center justify-center p-4 rounded-lg border border-dashed text-muted-foreground opacity-50">
            <div className="p-3 bg-slate-100 rounded-full mb-3 dark:bg-slate-800">
              <Award className="h-5 w-5" />
            </div>
            <span className="text-xs">更多徽章待解锁</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default BadgeList;
