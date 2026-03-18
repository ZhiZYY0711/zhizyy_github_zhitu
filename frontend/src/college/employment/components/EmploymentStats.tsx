import { Card, CardContent } from '@/components/ui/card';
import type { EmploymentStats as EmploymentStatsType } from '../../types';

interface Props {
  stats: EmploymentStatsType | null;
}

const EmploymentStats = ({ stats }: Props) => {
  if (!stats) return null;

  const items = [
    { label: '就业率', value: `${(stats.employment_rate * 100).toFixed(1)}%`, color: 'text-green-600' },
    { label: '实习率', value: `${(stats.internship_rate * 100).toFixed(1)}%`, color: 'text-blue-600' },
    { label: '灵活就业率', value: `${(stats.flexible_employment_rate * 100).toFixed(1)}%`, color: 'text-orange-500' },
    { label: '平均薪资', value: `¥${stats.avg_salary.toLocaleString()}`, color: 'text-purple-600' },
  ];

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {items.map(item => (
        <Card key={item.label}>
          <CardContent className="p-4 text-center">
            <p className={`text-2xl font-bold ${item.color}`}>{item.value}</p>
            <p className="text-sm text-muted-foreground mt-1">{item.label}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default EmploymentStats;
