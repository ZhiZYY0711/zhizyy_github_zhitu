import { useEffect, useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle } from 'lucide-react';
import EmploymentStats from './components/EmploymentStats';
import InternshipList from './components/InternshipList';
import { fetchEmploymentStats, fetchPendingContracts, auditContract } from '../services/api';
import type { EmploymentStats as EmploymentStatsType, Contract } from '../types';

const EmploymentPage = () => {
  const [stats, setStats] = useState<EmploymentStatsType | null>(null);
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [tab, setTab] = useState('interns');

  useEffect(() => {
    fetchEmploymentStats().then(setStats);
    fetchPendingContracts().then(data => {
      console.log('[EmploymentPage] Received contracts data:', data);
      console.log('[EmploymentPage] Is array?', Array.isArray(data));
      // 确保 data 是数组
      const contractsArray = Array.isArray(data) ? data : [];
      console.log('[EmploymentPage] Setting contracts:', contractsArray);
      setContracts(contractsArray);
    }).catch(err => {
      console.error('[EmploymentPage] Failed to fetch contracts:', err);
      setContracts([]);
    });
  }, []);

  const handleAudit = async (id: string, result: 'pass' | 'reject') => {
    await auditContract(id, result);
    setContracts(prev => prev.map(c => c.id === id ? { ...c, status: result === 'pass' ? 'approved' : 'rejected' } : c));
  };

  const contractStatusMap: Record<Contract['status'], { label: string; className: string }> = {
    pending: { label: '待审核', className: 'border-transparent bg-yellow-500 text-white' },
    approved: { label: '已通过', className: 'border-transparent bg-green-500 text-white' },
    rejected: { label: '已拒绝', className: 'border-transparent bg-red-500 text-white' },
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold">就业监测与实习管理</h1>
        <p className="text-muted-foreground text-sm mt-1">实习生状态 · 三方协议审核 · 云巡视</p>
      </div>

      <EmploymentStats stats={stats} />

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="interns">实习生状态</TabsTrigger>
          <TabsTrigger value="contracts">
            协议审核
            {contracts.filter(c => c.status === 'pending').length > 0 && (
              <span className="ml-1.5 h-4 w-4 rounded-full bg-red-500 text-white text-xs flex items-center justify-center">
                {contracts.filter(c => c.status === 'pending').length}
              </span>
            )}
          </TabsTrigger>
        </TabsList>

        <TabsContent value="interns" className="mt-4">
          <InternshipList />
        </TabsContent>

        <TabsContent value="contracts" className="mt-4">
          <div className="rounded-md border overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-muted/50">
                <tr>
                  <th className="text-left px-4 py-3 font-medium">学生</th>
                  <th className="text-left px-4 py-3 font-medium">企业</th>
                  <th className="text-left px-4 py-3 font-medium">岗位</th>
                  <th className="text-left px-4 py-3 font-medium">提交时间</th>
                  <th className="text-left px-4 py-3 font-medium">状态</th>
                  <th className="text-left px-4 py-3 font-medium">操作</th>
                </tr>
              </thead>
              <tbody>
                {contracts.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="text-center py-8 text-muted-foreground">
                      暂无待审核协议
                    </td>
                  </tr>
                ) : contracts.map(c => {
                  const st = contractStatusMap[c.status];
                  return (
                    <tr key={c.id} className="border-t hover:bg-muted/30 transition-colors">
                      <td className="px-4 py-3 font-medium">{c.studentName}</td>
                      <td className="px-4 py-3">{c.companyName}</td>
                      <td className="px-4 py-3 text-muted-foreground">{c.position}</td>
                      <td className="px-4 py-3 text-muted-foreground text-xs">{c.submitTime}</td>
                      <td className="px-4 py-3">
                        <Badge className={st.className}>{st.label}</Badge>
                      </td>
                      <td className="px-4 py-3">
                        {c.status === 'pending' && (
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              className="gap-1 text-green-600 border-green-300 hover:bg-green-50"
                              onClick={() => handleAudit(c.id, 'pass')}
                            >
                              <CheckCircle className="h-3 w-3" />
                              通过
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="gap-1 text-red-600 border-red-300 hover:bg-red-50"
                              onClick={() => handleAudit(c.id, 'reject')}
                            >
                              <XCircle className="h-3 w-3" />
                              拒绝
                            </Button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default EmploymentPage;
