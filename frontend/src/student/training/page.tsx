import React, { useState, useEffect } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Search, Filter, GitBranch, Github, Building2, Code } from "lucide-react";
import ProjectCard from "./components/ProjectCard";
import ProjectDetailDialog from "./components/ProjectDetailDialog";
import PeerReviewDialog from "./components/PeerReviewDialog";
import ScrumBoardComponent from "./components/ScrumBoard";
import { fetchProjects, fetchScrumBoard } from "../services/api";
import type { Project, ScrumBoard } from "../mock/generator";

const TrainingPage = () => {
  const [activeTab, setActiveTab] = useState("square");
  const [projects, setProjects] = useState<Project[]>([]);
  const [scrumBoard, setScrumBoard] = useState<ScrumBoard | null>(null);
  const [loading, setLoading] = useState(false);

  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [reviewOpen, setReviewOpen] = useState(false);

  // Mock "Joined" state
  const [hasJoinedProject, setHasJoinedProject] = useState(false);

  useEffect(() => {
    loadProjects();
  }, []);

  useEffect(() => {
    if (activeTab === 'my-project' && hasJoinedProject) {
      loadScrumBoard();
    }
  }, [activeTab, hasJoinedProject]);

  const loadProjects = async () => {
    setLoading(true);
    try {
      const data = await fetchProjects();
      setProjects(data);
    } catch (error) {
      console.error("Failed to load projects", error);
    } finally {
      setLoading(false);
    }
  };

  const loadScrumBoard = async () => {
    setLoading(true);
    try {
      const data = await fetchScrumBoard();
      setScrumBoard(data);
    } catch (error) {
      console.error("Failed to load scrum board", error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetails = (project: Project) => {
    setSelectedProject(project);
    setDetailOpen(true);
  };

  const handleApply = (project: Project) => {
    // Mock application logic
    alert(`申请已提交: ${project.name}`);
    setDetailOpen(false);
    // Auto switch to my project for demo
    setHasJoinedProject(true);
    setActiveTab("my-project");
  };

  return (
    <div className="container mx-auto p-6 space-y-6 h-full flex flex-col">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">智能实训</h1>
          <p className="text-muted-foreground mt-1">
            参与企业真实项目，体验敏捷开发流程
          </p>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col">
        <TabsList className="grid w-[400px] grid-cols-2">
          <TabsTrigger value="square">项目广场</TabsTrigger>
          <TabsTrigger value="my-project">我的实训</TabsTrigger>
        </TabsList>

        <TabsContent value="square" className="flex-1 mt-6">
          <div className="flex gap-4 mb-6">
            <div className="relative flex-1 max-w-md">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="search"
                placeholder="搜索项目、技术栈..."
                className="pl-8"
              />
            </div>
            <Button variant="outline">
              <Filter className="mr-2 h-4 w-4" />
              筛选
            </Button>
          </div>

          {loading && !projects.length ? (
            <div className="text-center py-12 text-muted-foreground">加载中...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {projects.map(project => (
                <ProjectCard
                  key={project.id}
                  project={project}
                  onViewDetails={handleViewDetails}
                />
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="my-project" className="flex-1 mt-6 h-full">
          {!hasJoinedProject ? (
            <div className="flex flex-col items-center justify-center h-[400px] border-2 border-dashed rounded-lg bg-gray-50">
              <div className="text-center space-y-4">
                <div className="bg-white p-4 rounded-full inline-block shadow-sm">
                  <Building2 className="h-8 w-8 text-muted-foreground" />
                </div>
                <h3 className="text-lg font-semibold">尚未加入任何实训项目</h3>
                <p className="text-sm text-muted-foreground max-w-sm">
                  请前往项目广场浏览并申请感兴趣的项目，加入后即可开启敏捷开发之旅。
                </p>
                <Button onClick={() => setActiveTab("square")}>
                  去浏览项目
                </Button>
              </div>
            </div>
          ) : (
            <div className="h-full space-y-6">
              <div className="flex items-center justify-between bg-white p-4 rounded-lg border shadow-sm">
                <div className="flex items-center gap-4">
                  <div className="h-10 w-10 bg-primary/10 rounded-lg flex items-center justify-center">
                    <Code className="h-6 w-6 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-bold">银行核心交易系统仿真</h3>
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      <GitBranch className="h-3 w-3" />
                      <span>feature/order-service</span>
                      <span className="w-1 h-1 rounded-full bg-gray-300" />
                      <Github className="h-3 w-3" />
                      <a href="#" className="hover:underline">zhitu-training/bank-core</a>
                    </div>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm">代码仓库</Button>
                  <Button variant="outline" size="sm">PRD文档</Button>
                  <Button size="sm" onClick={() => setReviewOpen(true)}>360°互评</Button>
                </div>
              </div>

              <div className="flex-1 min-h-[500px]">
                {scrumBoard ? (
                  <ScrumBoardComponent board={scrumBoard} loading={loading} />
                ) : (
                  <div>Loading board...</div>
                )}
              </div>
            </div>
          )}
        </TabsContent>
      </Tabs>

      <ProjectDetailDialog
        project={selectedProject}
        open={detailOpen}
        onOpenChange={setDetailOpen}
        onApply={handleApply}
      />

      <PeerReviewDialog
        open={reviewOpen}
        onOpenChange={setReviewOpen}
      />
    </div>
  );
};

export default TrainingPage;
