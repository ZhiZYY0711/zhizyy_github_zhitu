// f:\projects\zhitu\frontend\src\student\dashboard\components\RecommendationFeed.tsx
import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Briefcase, Code, BookOpen, ArrowRight } from "lucide-react";
import type { RecommendationItem } from "../../mock/generator";

interface RecommendationFeedProps {
  recommendations: RecommendationItem[];
  loading: boolean;
}

const RecommendationFeed: React.FC<RecommendationFeedProps> = ({ recommendations, loading }) => {
  if (loading) {
    return (
      <Card className="col-span-1 h-full flex flex-col animate-pulse min-h-0">
        <CardHeader className="shrink-0">
          <div className="h-6 w-32 bg-gray-200 rounded"></div>
        </CardHeader>
        <CardContent className="flex-1 space-y-4">
          {[1, 2].map((i) => (
            <div key={i} className="h-24 bg-gray-200 rounded"></div>
          ))}
        </CardContent>
      </Card>
    );
  }

  const getTypeLabel = (type: RecommendationItem['type']) => {
    switch (type) {
      case 'project': return { label: '实训项目', icon: <Code className="h-4 w-4 mr-1" />, color: 'bg-blue-100 text-blue-800' };
      case 'job': return { label: '实习岗位', icon: <Briefcase className="h-4 w-4 mr-1" />, color: 'bg-green-100 text-green-800' };
      case 'course': return { label: '提升课程', icon: <BookOpen className="h-4 w-4 mr-1" />, color: 'bg-orange-100 text-orange-800' };
      default: return { label: '推荐', icon: null, color: 'bg-gray-100' };
    }
  };

  return (
    <Card className="col-span-1 h-full flex flex-col min-h-0">
      <CardHeader className="shrink-0">
        <CardTitle>个性化推荐流</CardTitle>
        <CardDescription>基于画像为您推荐</CardDescription>
      </CardHeader>
      <CardContent className="flex-1 overflow-auto space-y-4 min-h-0">
        {recommendations.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
            <p>暂无推荐</p>
          </div>
        ) : (
          <>
            {recommendations.map((item) => {
              const { label, icon, color } = getTypeLabel(item.type);
              return (
                <div key={item.rec_id} className="border rounded-lg p-4 space-y-3 hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start">
                  <div className={`flex items-center px-2 py-1 rounded text-xs font-medium ${color}`}>
                    {icon} {label}
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-muted-foreground font-medium">匹配度 {Math.round(item.match_score * 100)}%</span>
                    <div className="w-16">
                      <Progress value={item.match_score * 100} className="h-1.5" />
                    </div>
                  </div>
                </div>

                <div>
                  <h4 className="font-semibold text-lg">{item.title}</h4>
                  {item.company_name && <p className="text-sm text-muted-foreground">{item.company_name}</p>}
                </div>

                {item.tags && (
                  <div className="flex flex-wrap gap-1">
                    {item.tags.map(tag => (
                      <Badge key={tag} variant="secondary" className="text-xs font-normal">
                        {tag}
                      </Badge>
                    ))}
                  </div>
                )}

                <div className="bg-muted/50 p-2 rounded text-xs text-muted-foreground">
                  💡 {item.match_reason}
                </div>

                <Button className="w-full text-sm" variant="outline" size="sm">
                  查看详情 <ArrowRight className="ml-2 h-3 w-3" />
                </Button>
              </div>
            );
          })}
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default RecommendationFeed;
