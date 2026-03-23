import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import type { EvaluationResult } from '../../mock/generator';
import { Trophy } from 'lucide-react';

interface EvaluationChartProps {
  data: EvaluationResult | null;
}

const EvaluationChart: React.FC<EvaluationChartProps> = ({ data }) => {
  if (!data) return null;

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Trophy className="h-5 w-5 text-yellow-500" />
          综合评价结果
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="flex items-center justify-between">
          <span className="text-3xl font-bold">{data.total_score}</span>
          <span className="text-sm text-muted-foreground">总分 / 100</span>
        </div>

        <div className="space-y-4">
          <div className="space-y-1">
            <div className="flex justify-between text-sm">
              <span>企业评价 (40%)</span>
              <span className="font-medium">{data.enterprise_score}</span>
            </div>
            <Progress value={data.enterprise_score} className="h-2 bg-slate-100" indicatorClassName="bg-blue-500" />
          </div>

          <div className="space-y-1">
            <div className="flex justify-between text-sm">
              <span>校方评价 (30%)</span>
              <span className="font-medium">{data.school_score}</span>
            </div>
            <Progress value={data.school_score} className="h-2 bg-slate-100" indicatorClassName="bg-green-500" />
          </div>

          <div className="space-y-1">
            <div className="flex justify-between text-sm">
              <span>增值评价 (30%)</span>
              <span className="font-medium">{data.bonus_score}</span>
            </div>
            <Progress value={data.bonus_score} className="h-2 bg-slate-100" indicatorClassName="bg-purple-500" />
          </div>
        </div>

        <div className="mt-6 pt-4 border-t">
          <h4 className="text-sm font-semibold mb-2">导师评语</h4>
          <div className="space-y-3">
            {data.comments && Array.isArray(data.comments) && data.comments.map((comment) => (
              <div key={comment.id} className="bg-muted/50 p-3 rounded-lg text-sm">
                <div className="flex justify-between items-center mb-1">
                  <span className="font-medium text-xs">{comment.author}</span>
                  <span className="text-xs text-muted-foreground">{comment.date}</span>
                </div>
                <p className="text-muted-foreground leading-relaxed">{comment.content}</p>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default EvaluationChart;
