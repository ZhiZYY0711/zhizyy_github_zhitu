import { useEffect, useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle, MapPin } from 'lucide-react';
import { fetchEnterprises, fetchCrmAudits, fetchVisitRecords, auditEnterprise } from '../services/api';
import type { CooperativeEnterprise, CrmAudit, VisitRecord } from '../types';

const levelMap: Record<CooperativeEnterprise['level'], { label: string; className: string }> = {
  strategic: { label: '战略合作', className: 'border-transparent bg-purple-500 text-white' },
  core: { label: '核心合作', className: 'border-transparent bg-blue-500 text-white' },
  normal: { label: '普通合作', className: 'border-transparent bg-gray-400 text-white' },
};

const CrmPage = () => {
  const [enterprises, setEnterprises] = useState<CooperativeEnterprise[]>([]);
  const [audits, setAudits] = useState<CrmAudit[]>([]);
  const [visits, setVisits] = useState<VisitRecord[]>([]);
  const [tab, setTab] = useState('enterprises');

  useEffect(() => {
    fetchEnterprises().then(d => setEnterprises(d.records));
    fetchCrmAudits().then(setAudits);
    fetchVisitRecords().then(setVisits);
  }, []);

  const handleAudit = async (id: string, action: 'approve' | 'reject') => {
    await auditEnterprise(id, action);
    setAudits(prev => prev.filter(a => a.id !== id));
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold">企业CRM管理</h1>
        <p className="text-muted-foreground text-sm mt-1">合作企业 · 资质审核 · 走访记录</p>
      </div>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="enterprises">合作企业</TabsTrigger>
          <TabsTrigger value="audits">
            资质审核
            {audits.length > 0 && (
              <span className="ml-1.5 h-4 w-4 rounded-full bg-red-500 text-white text-xs flex items-center justify-center">
                {audits.length}
              </span>
            )}
          </TabsTrigger>
          <TabsTrigger value="visits">走访记录</TabsTrigger>
        </TabsList>

        <TabsContent value="enterprises" className="mt-4">
          <div className="rounded-md border overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-muted/50">
                <tr>
                  <th className="text-left px-4 py-3 font-medium">企业名称</th>
                  <th className="text-left px-4 py-3 font-medium">行业</th>
                  <th className="text-left px-4 py-3 font-medium">合作等级</th>
                  <th className="text-left px-4 py-3 font-medium">联系人</th>
                  <th className="text-left px-4 py-3 font-medium">在岗实习生</th>
                  <th className="text-left px-4 py-3 font-medium">累计录用</th>
                </tr>
              </thead>
              <tbody>
                {enterprises.map(e => {
                  const lv = levelMap[e.level];
                  return (
                    <tr key={e.id} className="border-t hover:bg-muted/30 transition-colors">
                      <td className="px-4 py-3 font-medium">{e.name}</td>
                      <td className="px-4 py-3 text-muted-foreground">{e.industry}</td>
                      <td className="px-4 py-3">
                        <Badge className={lv.className}>{lv.label}</Badge>
                      </td>
                      <td className="px-4 py-3 text-muted-foreground">{e.contact_person}</td>
                      <td className="px-4 py-3 font-medium text-blue-600">{e.active_interns}</td>
                      <td className="px-4 py-3 text-muted-foreground">{e.total_hired}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </TabsContent>

        <TabsContent value="audits" className="mt-4">
          {audits.length === 0 ? (
            <p className="text-center py-12 text-muted-foreground">暂无待审核企业</p>
          ) : (
            <div className="space-y-3">
              {audits.map(a => (
                <div key={a.id} className="border rounded-lg p-4 flex items-center justify-between gap-4">
                  <div>
                    <p className="font-medium">{a.enterprise_name}</p>
                    <p className="text-sm text-muted-foreground">{a.industry} · 联系人: {a.contact_person} · 提交: {a.submit_time}</p>
                  </div>
                  <div className="flex gap-2 shrink-0">
                    <Button
                      size="sm"
                      variant="outline"
                      className="gap-1 text-green-600 border-green-300 hover:bg-green-50"
                      onClick={() => handleAudit(a.id, 'approve')}
                    >
                      <CheckCircle className="h-3 w-3" />
                      通过
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      className="gap-1 text-red-600 border-red-300 hover:bg-red-50"
                      onClick={() => handleAudit(a.id, 'reject')}
                    >
                      <XCircle className="h-3 w-3" />
                      拒绝
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="visits" className="mt-4">
          <div className="space-y-3">
            {visits.map(v => (
              <div key={v.id} className="border rounded-lg p-4">
                <div className="flex items-start gap-3">
                  <div className="h-8 w-8 rounded-full bg-green-100 text-green-700 flex items-center justify-center shrink-0">
                    <MapPin className="h-4 w-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-medium">{v.enterprise_name}</span>
                      <span className="text-xs text-muted-foreground">{v.visit_date}</span>
                    </div>
                    <p className="text-sm text-muted-foreground mt-1">{v.content}</p>
                    <div className="mt-1 flex flex-wrap gap-1">
                      {v.visitors.map(visitor => (
                        <span key={visitor} className="text-xs bg-muted px-2 py-0.5 rounded">{visitor}</span>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default CrmPage;
