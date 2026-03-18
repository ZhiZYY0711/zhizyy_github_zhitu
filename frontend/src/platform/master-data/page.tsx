import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import TagManager from './components/TagManager';
import SkillTreeEditor from './components/SkillTreeEditor';
import TemplateManager from './components/TemplateManager';

export default function MasterDataPage() {
  return (
    <div className="container mx-auto p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">基础主数据管理</h1>
        <p className="text-muted-foreground">管理标签、技能树及证书/合同模板</p>
      </div>

      <Tabs defaultValue="tags">
        <TabsList>
          <TabsTrigger value="tags">标签管理</TabsTrigger>
          <TabsTrigger value="skills">技能树</TabsTrigger>
          <TabsTrigger value="templates">模板管理</TabsTrigger>
        </TabsList>
        <TabsContent value="tags" className="mt-4">
          <TagManager />
        </TabsContent>
        <TabsContent value="skills" className="mt-4">
          <SkillTreeEditor />
        </TabsContent>
        <TabsContent value="templates" className="mt-4">
          <TemplateManager />
        </TabsContent>
      </Tabs>
    </div>
  );
}
