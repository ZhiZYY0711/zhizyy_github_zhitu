import { useState, useEffect } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { MapPin, Search, FileSignature, Upload } from "lucide-react";
import JobCard from "./components/JobCard";
import ReportList from "./components/ReportList";
import { fetchJobs, fetchReports } from "../services/api";
import type { Job, InternshipReport } from "../mock/generator";

const InternshipPage = () => {
  const [activeTab, setActiveTab] = useState("job-hunt");
  const [jobs, setJobs] = useState<Job[]>([]);
  const [reports, setReports] = useState<InternshipReport[]>([]);

  // Mock "Internship Active" state
  const [hasInternship, setHasInternship] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        if (activeTab === 'job-hunt') {
          const data = await fetchJobs();
          setJobs(data);
        } else if (activeTab === 'process') {
          const data = await fetchReports();
          setReports(data);
        }
      } catch (error) {
        console.error("Failed to load data", error);
      }
    };
    loadData();
  }, [activeTab]);

  const handleApply = (job: Job) => {
    alert(`已投递: ${job.title}`);
    // Demo: auto switch to process
    setHasInternship(true);
    setActiveTab("process");
  };

  return (
    <div className="container mx-auto p-6 space-y-6 h-full flex flex-col">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">实习之旅</h1>
          <p className="text-muted-foreground mt-1">
            从岗位求职到实习过程管理的全链路支持
          </p>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col">
        <TabsList className="grid w-[400px] grid-cols-2">
          <TabsTrigger value="job-hunt">求职与签约</TabsTrigger>
          <TabsTrigger value="process">过程管理</TabsTrigger>
        </TabsList>

        <TabsContent value="job-hunt" className="flex-1 mt-6">
          <div className="flex gap-4 mb-6">
            <div className="relative flex-1 max-w-md">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                id="job-search"
                name="job-search"
                type="search"
                placeholder="搜索职位、公司、城市..."
                className="pl-8"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {jobs.map(job => (
              <JobCard key={job.id} job={job} onApply={handleApply} />
            ))}
          </div>

          <div className="mt-8 p-4 bg-blue-50 rounded-lg border border-blue-100 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="bg-white p-2 rounded shadow-sm">
                <FileSignature className="w-8 h-8 text-blue-600" />
              </div>
              <div>
                <h3 className="font-semibold text-blue-900">三方/实习协议签署</h3>
                <p className="text-sm text-blue-700">已获得 Offer？点击发起线上签约流程。</p>
              </div>
            </div>
            <Button>发起签约</Button>
          </div>
        </TabsContent>

        <TabsContent value="process" className="flex-1 mt-6">
          {!hasInternship ? (
            <div className="flex flex-col items-center justify-center h-[400px] border-2 border-dashed rounded-lg bg-gray-50">
              <h3 className="text-lg font-semibold text-gray-700">暂无进行中的实习</h3>
              <p className="text-sm text-gray-500 mt-2">签约完成后即可开启过程管理功能</p>
              <Button variant="link" onClick={() => setActiveTab("job-hunt")}>去求职</Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-full">
              <div className="lg:col-span-2 space-y-6">
                <ReportList reports={reports} onCreate={() => alert("新建周报")} />
              </div>

              <div className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">LBS 考勤打卡</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="bg-gray-100 h-32 rounded flex items-center justify-center text-gray-400 text-sm">
                      <MapPin className="w-4 h-4 mr-1" />
                      地图组件占位
                    </div>
                    <div className="text-sm text-center">
                      当前位置: <span className="font-bold">字节跳动总部大楼</span>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <Button className="w-full" size="lg">上班打卡</Button>
                      <Button className="w-full" variant="secondary" size="lg">下班打卡</Button>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">快捷服务</CardTitle>
                  </CardHeader>
                  <CardContent className="grid grid-cols-2 gap-2">
                    <Button variant="outline" className="h-20 flex flex-col gap-2">
                      <Upload className="w-5 h-5" />
                      上传附件
                    </Button>
                    <Button variant="outline" className="h-20 flex flex-col gap-2">
                      <FileSignature className="w-5 h-5" />
                      请假申请
                    </Button>
                  </CardContent>
                </Card>
              </div>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default InternshipPage;
