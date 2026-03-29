// f:\projects\zhitu\frontend\src\student\dashboard\components\CapabilityRadar.tsx
import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Legend } from 'recharts';
import type { RadarData } from "../../mock/generator";

interface CapabilityRadarProps {
  data: RadarData | null;
  loading: boolean;
}

const CapabilityRadar: React.FC<CapabilityRadarProps> = ({ data, loading }) => {
  if (loading) {
    return (
      <Card className="col-span-1 h-full flex flex-col animate-pulse min-h-0">
        <CardHeader className="shrink-0">
          <div className="h-6 w-32 bg-gray-200 rounded"></div>
        </CardHeader>
        <CardContent className="flex-1 flex justify-center items-center">
          <div className="h-48 w-48 rounded-full bg-gray-200"></div>
        </CardContent>
      </Card>
    );
  }

  if (!data || !data.dimensions || data.dimensions.length === 0) {
    return (
      <Card className="col-span-1 h-full flex flex-col min-h-0">
        <CardHeader className="shrink-0">
          <CardTitle>能力雷达画像</CardTitle>
          <CardDescription>六维能力评估与同专业对比</CardDescription>
        </CardHeader>
        <CardContent className="flex-1 flex items-center justify-center pb-4 min-h-0">
          <div className="text-gray-400">
            暂无能力数据
          </div>
        </CardContent>
      </Card>
    );
  }

  const hasPeerData = data.peer_average && Array.isArray(data.peer_average) && data.peer_average.length > 0;

  const chartData = data.dimensions.map((dim, index) => ({
    subject: dim.label,
    student: dim.score,
    peer: data.peer_average?.[index] ?? 0,
    fullMark: dim.max
  }));

  return (
    <Card className="col-span-1 h-full flex flex-col min-h-0">
      <CardHeader className="shrink-0">
        <CardTitle>能力雷达画像</CardTitle>
        <CardDescription>六维能力评估与同专业对比</CardDescription>
      </CardHeader>
      <CardContent className="flex-1 pb-4 min-h-0">
        <div className="w-full h-full">
          <ResponsiveContainer width="100%" height="100%">
            <RadarChart cx="50%" cy="50%" outerRadius="70%" data={chartData}>
              <PolarGrid />
              <PolarAngleAxis dataKey="subject" tick={{ fontSize: 12 }} />
              <PolarRadiusAxis angle={30} domain={[0, 100]} />
              <Radar
                name="我的能力"
                dataKey="student"
                stroke="#8884d8"
                fill="#8884d8"
                fillOpacity={0.6}
              />
              {hasPeerData && (
                <Radar
                  name="专业平均"
                  dataKey="peer"
                  stroke="#82ca9d"
                  fill="#82ca9d"
                  fillOpacity={0.3}
                />
              )}
              <Legend />
            </RadarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

export default CapabilityRadar;
