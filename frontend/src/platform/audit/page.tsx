import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import TenantList from './components/TenantList';
import EnterpriseAudit from './components/EnterpriseAudit';
import ProjectAudit from './components/ProjectAudit';

export default function AuditPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">机构审核管理</h1>
      <Tabs defaultValue="tenants">
        <TabsList>
          <TabsTrigger value="tenants">高校租户管理</TabsTrigger>
          <TabsTrigger value="enterprise">企业入驻审核</TabsTrigger>
          <TabsTrigger value="project">实训项目审核</TabsTrigger>
        </TabsList>
        <TabsContent value="tenants" className="mt-4">
          <TenantList />
        </TabsContent>
        <TabsContent value="enterprise" className="mt-4">
          <EnterpriseAudit />
        </TabsContent>
        <TabsContent value="project" className="mt-4">
          <ProjectAudit />
        </TabsContent>
      </Tabs>
    </div>
  );
}
