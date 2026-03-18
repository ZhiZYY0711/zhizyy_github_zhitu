import { useEffect, useState } from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { fetchTrainingPlans } from '../../services/api';
import type { TrainingPlan } from '../../types';

const statusMap: Record<TrainingPlan['status'], { label: string; className: string }> = {
  draft: { label: '草稿', className: 'border-transparent bg-gray-400 text-white' },
  published: { label: '已发布', className: 'border-transparent bg-blue-500 text-white' },
  ongoing: { label: '进行中', className: 'border-transparent bg-green-500 text-white' },
  closed: { label: '已结束', className: 'border-transparent bg-gray-300 text-gray-700' },
};

const TrainingPlanList = () => {
  const [plans, setPlans] = useState<TrainingPlan[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTrainingPlans().then(data => {
      setPlans(data);
      setLoading(false);
    });
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <span className="text-sm text-muted-foreground">共 {plans.length} 个实训计划</span>
        <Button size="sm" className="gap-1">
          <Plus className="h-4 w-4" />
          新建计划
        </Button>
      </div>

      <div className="space-y-3">
        {loading ? (
          <p className="text-center py-8 text-muted-foreground">加载中...</p>
        ) : plans.map(plan => {
          const st = statusMap[plan.status];
          return (
            <div key={plan.id} className="border rounded-lg p-4 hover:bg-muted/30 transition-colors">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-medium">{plan.course_name}</span>
                    <Badge className={st.className}>{st.label}</Badge>
                    {plan.credits && (
                      <span className="text-xs text-muted-foreground">{plan.credits} 学分</span>
                    )}
                  </div>
                  <div className="mt-1 text-sm text-muted-foreground">
                    {plan.start_date} ~ {plan.end_date}
                  </div>
                  <div className="mt-1 flex flex-wrap gap-1">
                    {plan.target_majors.map(m => (
                      <span key={m} className="text-xs bg-muted px-2 py-0.5 rounded">{m}</span>
                    ))}
                  </div>
                </div>
                <Button variant="outline" size="sm">查看详情</Button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default TrainingPlanList;
