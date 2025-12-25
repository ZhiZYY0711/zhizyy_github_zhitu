import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollText, Download } from "lucide-react";
import { Button } from '@/components/ui/button';
import { Skeleton } from "@/components/ui/skeleton";

import { fetchEvaluation, fetchCertificates, fetchBadges } from '../services/api';
import type { EvaluationResult, Certificate, Badge } from '../mock/generator';

import EvaluationChart from './components/EvaluationChart';
import CertificateCard from './components/CertificateCard';
import BadgeList from './components/BadgeList';

const GrowthPage = () => {
  const [evaluation, setEvaluation] = useState<EvaluationResult | null>(null);
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [badges, setBadges] = useState<Badge[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const [evalData, certData, badgeData] = await Promise.all([
          fetchEvaluation(),
          fetchCertificates(),
          fetchBadges()
        ]);
        setEvaluation(evalData);
        setCertificates(certData);
        setBadges(badgeData);
      } catch (error) {
        console.error("Failed to load growth data:", error);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  if (loading) {
    return (
      <div className="container mx-auto p-4 lg:p-6 space-y-6 max-w-7xl">
        <div className="space-y-2">
          <Skeleton className="h-9 w-[200px]" />
          <Skeleton className="h-5 w-[300px]" />
        </div>
        <div className="grid gap-6 md:grid-cols-12">
          <div className="md:col-span-12 lg:col-span-4 space-y-6">
            <Skeleton className="h-[400px] w-full rounded-xl" />
            <Skeleton className="h-[150px] w-full rounded-xl" />
          </div>
          <div className="md:col-span-12 lg:col-span-8 space-y-6">
            <Skeleton className="h-[200px] w-full rounded-xl" />
            <div className="space-y-4">
              <Skeleton className="h-8 w-[200px]" />
              <Skeleton className="h-[120px] w-full rounded-xl" />
              <Skeleton className="h-[120px] w-full rounded-xl" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-4 lg:p-6 space-y-6 max-w-7xl">
      <div className="flex flex-col space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">职业成长与评价</h1>
        <p className="text-muted-foreground">Growth & Evaluation - 成果沉淀与数字化认证</p>
      </div>

      <div className="grid gap-6 md:grid-cols-12">
        {/* Left Column: Evaluation Score (4 cols) */}
        <div className="md:col-span-12 lg:col-span-4 space-y-6">
          <EvaluationChart data={evaluation} />

          <Card className="bg-gradient-to-br from-indigo-500 to-purple-600 text-white border-none shadow-lg">
            <CardHeader>
              <CardTitle className="text-lg">校友服务通道</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-indigo-100 text-sm mb-4">
                毕业后账号将自动转为校友账号，享受终身技能更新与内推服务。
              </p>
              <Button variant="secondary" size="sm" className="w-full">
                了解详情
              </Button>
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Certificates & Badges (8 cols) */}
        <div className="md:col-span-12 lg:col-span-8 space-y-6">
          {/* Badges Section */}
          <BadgeList badges={badges} />

          {/* Certificates Section */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-xl font-semibold flex items-center gap-2">
                <ScrollText className="h-5 w-5 text-blue-600" />
                数字凭证 ({certificates.length})
              </h3>
              <Button variant="outline" size="sm" className="gap-2">
                <Download className="h-4 w-4" />
                批量导出
              </Button>
            </div>

            <div className="grid gap-4">
              {certificates.map((cert) => (
                <CertificateCard key={cert.id} certificate={cert} />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GrowthPage;
