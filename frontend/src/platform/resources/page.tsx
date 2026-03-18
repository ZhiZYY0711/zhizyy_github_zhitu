import RecommendationManager from './components/RecommendationManager';

export default function ResourcesPage() {
  return (
    <div className="container mx-auto p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">全局资源调度</h1>
        <p className="text-muted-foreground">管理首页推荐位与置顶榜单</p>
      </div>
      <RecommendationManager />
    </div>
  );
}
