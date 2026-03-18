import { Code2, FileText, Calendar } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import type { MentorDashboard } from '../../types';

interface Props { data: MentorDashboard }

export default function MentorStats({ data }: Props) {
  const cards = [
    { title: '待代码评审', value: data.pending_code_reviews, icon: Code2, color: 'text-purple-600', bg: 'bg-purple-50' },
    { title: '待周报批阅', value: data.pending_weekly_reports, icon: FileText, color: 'text-green-600', bg: 'bg-green-50' },
    { title: '即将面试', value: data.upcoming_interviews, icon: Calendar, color: 'text-blue-600', bg: 'bg-blue-50' },
  ];

  return (
    <div className="grid grid-cols-3 gap-4">
      {cards.map(card => (
        <Card key={card.title}>
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">{card.title}</p>
                <p className="text-3xl font-bold mt-1">{card.value}</p>
              </div>
              <div className={`h-11 w-11 rounded-full ${card.bg} flex items-center justify-center`}>
                <card.icon className={`h-5 w-5 ${card.color}`} />
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
