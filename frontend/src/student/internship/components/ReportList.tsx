import React from 'react';
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FileText, CheckCircle2 } from "lucide-react";
import type { InternshipReport } from "../../mock/generator";

interface ReportListProps {
  reports: InternshipReport[];
  onCreate: () => void;
}

const ReportList: React.FC<ReportListProps> = ({ reports, onCreate }) => {
  const getStatusBadge = (status: InternshipReport['status']) => {
    switch (status) {
      case 'reviewed': return <Badge variant="default" className="bg-green-600">已批阅</Badge>;
      case 'submitted': return <Badge variant="secondary" className="bg-blue-100 text-blue-700">已提交</Badge>;
      default: return <Badge variant="outline" className="text-gray-500">草稿</Badge>;
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <FileText className="w-5 h-5" />
          我的周报
        </h3>
        <Button size="sm" onClick={onCreate}>写周报</Button>
      </div>

      <div className="space-y-3">
        {reports.map(report => (
          <Card key={report.id} className="cursor-pointer hover:border-primary/50 transition-colors">
            <CardHeader className="py-3">
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <span className="font-bold">第 {report.week_number} 周实习周报</span>
                  <span className="text-xs text-muted-foreground">
                    ({report.start_date} ~ {report.end_date})
                  </span>
                </div>
                {getStatusBadge(report.status)}
              </div>
            </CardHeader>
            <CardContent className="py-3 pt-0 space-y-2">
              <div className="text-sm text-gray-600 line-clamp-2">
                <span className="font-semibold text-gray-900">本周产出: </span>
                {report.content_done}
              </div>
              {report.status === 'reviewed' && (
                <div className="bg-green-50 p-2 rounded text-xs text-green-800 flex gap-2 items-start mt-2">
                  <CheckCircle2 className="w-4 h-4 mt-0.5 shrink-0" />
                  <div>
                    <span className="font-bold">导师评语: </span>
                    {report.mentor_comment}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default ReportList;
