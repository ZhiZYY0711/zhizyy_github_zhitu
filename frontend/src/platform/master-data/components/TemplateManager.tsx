import { useEffect, useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { fetchCertificateTemplates, fetchContractTemplates } from '../../services/api';
import type { CertificateTemplate, ContractTemplate } from '../../types';

const CONTRACT_STATUS_STYLES: Record<ContractTemplate['status'], string> = {
  draft: 'bg-gray-100 text-gray-600 border-gray-200',
  active: 'bg-green-100 text-green-700 border-green-200',
  archived: 'bg-yellow-100 text-yellow-700 border-yellow-200',
};

const CONTRACT_STATUS_LABELS: Record<ContractTemplate['status'], string> = {
  draft: '草稿',
  active: '生效中',
  archived: '已归档',
};

function CertificateList() {
  const [templates, setTemplates] = useState<CertificateTemplate[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCertificateTemplates().then((data: CertificateTemplate[]) => {
      setTemplates(data);
      setLoading(false);
    });
  }, []);

  if (loading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3].map(i => <Skeleton key={i} className="h-20 w-full rounded-lg" />)}
      </div>
    );
  }

  if (templates.length === 0) {
    return <p className="text-center text-muted-foreground py-8">暂无证书模板</p>;
  }

  return (
    <div className="space-y-3">
      {templates.map(tpl => (
        <div key={tpl.id} className="flex items-center gap-4 border rounded-lg p-4">
          {/* 背景图预览 */}
          <div className="w-16 h-12 rounded border overflow-hidden flex-shrink-0 bg-muted">
            {tpl.background_url ? (
              <img
                src={tpl.background_url}
                alt={tpl.name}
                className="w-full h-full object-cover"
                onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }}
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-xs text-muted-foreground">无图</div>
            )}
          </div>
          <div className="flex-1 min-w-0">
            <p className="font-medium text-sm truncate">{tpl.name}</p>
            <p className="text-xs text-muted-foreground">
              创建于 {new Date(tpl.created_at).toLocaleDateString('zh-CN')}
            </p>
          </div>
          <div className="flex gap-2 flex-shrink-0">
            <Button variant="outline" size="sm">编辑</Button>
            <Button variant="outline" size="sm">预览</Button>
          </div>
        </div>
      ))}
    </div>
  );
}

function ContractList() {
  const [templates, setTemplates] = useState<ContractTemplate[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchContractTemplates().then((data: ContractTemplate[]) => {
      setTemplates(data);
      setLoading(false);
    });
  }, []);

  if (loading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3].map(i => <Skeleton key={i} className="h-20 w-full rounded-lg" />)}
      </div>
    );
  }

  if (templates.length === 0) {
    return <p className="text-center text-muted-foreground py-8">暂无合同模板</p>;
  }

  return (
    <div className="space-y-3">
      {templates.map(tpl => (
        <div key={tpl.id} className="flex items-center gap-4 border rounded-lg p-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <p className="font-medium text-sm">{tpl.name}</p>
              <span className="text-xs text-muted-foreground">v{tpl.version}</span>
              <Badge className={CONTRACT_STATUS_STYLES[tpl.status]}>
                {CONTRACT_STATUS_LABELS[tpl.status]}
              </Badge>
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              变量数量：{tpl.variables.length} 个
            </p>
          </div>
          <div className="flex gap-2 flex-shrink-0">
            <Button variant="outline" size="sm">编辑</Button>
            <Button variant="outline" size="sm">预览</Button>
          </div>
        </div>
      ))}
    </div>
  );
}

export default function TemplateManager() {
  return (
    <Tabs defaultValue="certificate">
      <TabsList>
        <TabsTrigger value="certificate">证书模板</TabsTrigger>
        <TabsTrigger value="contract">合同模板</TabsTrigger>
      </TabsList>
      <TabsContent value="certificate" className="mt-4">
        <CertificateList />
      </TabsContent>
      <TabsContent value="contract" className="mt-4">
        <ContractList />
      </TabsContent>
    </Tabs>
  );
}
