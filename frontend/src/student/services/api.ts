import { fetchWithAuth } from '@/lib/http';
import * as MockGenerator from '../mock/generator';
import type { ProjectApiResponse, PaginatedResponse, Project } from '../mock/generator';

const BASE = import.meta.env.VITE_API_BASE_URL || '/api/student-portal/v1';

// Helper function to convert backend API response to frontend Project type
function mapProjectApiToProject(apiProject: ProjectApiResponse): Project {
  const statusMap: Record<number, 'recruiting' | 'ongoing' | 'finished'> = {
    1: 'recruiting',
    2: 'ongoing',
    3: 'finished',
  };

  return {
    id: String(apiProject.id),
    name: apiProject.projectName,
    provider: apiProject.industry,
    difficulty: 3, // Default difficulty, can be enhanced later
    tech_stack: apiProject.techStack,
    status: statusMap[apiProject.status] || 'recruiting',
    team_size: apiProject.maxMembers,
    current_members: 0, // Not provided by backend, default to 0
    description: apiProject.description,
  };
}

// Helper function to convert backend job response to frontend Job type
function mapJobApiToJob(apiJob: any): any {
  return {
    id: String(apiJob.id),
    title: apiJob.jobTitle,
    company: apiJob.enterpriseName,
    city: apiJob.city,
    salary_min: apiJob.salaryMin,
    salary_max: apiJob.salaryMax,
    tags: apiJob.techStack || [],
    match_score: 85, // Default match score, can be enhanced later
    match_reason: '技能匹配度高，岗位要求与你的能力相符',
    type: apiJob.jobType,
    description: apiJob.description,
    requirements: apiJob.requirements,
    applicationStatus: apiJob.applicationStatus,
  };
}

// Helper function to convert backend evaluation response to frontend EvaluationResult type
function mapEvaluationApiToEvaluation(apiEval: any): any {
  // Backend returns: { averageScore, evaluations: [{evaluatorName, sourceType, evaluationDate, score, comment}] }
  // Frontend expects: { enterprise_score, school_score, bonus_score, total_score, comments: [...] }
  
  const evaluations = apiEval.evaluations || [];
  const enterpriseEvals = evaluations.filter((e: any) => e.sourceType === 'enterprise');
  const collegeEvals = evaluations.filter((e: any) => e.sourceType === 'college');
  
  const enterpriseScore = enterpriseEvals.length > 0 
    ? Math.round(enterpriseEvals.reduce((sum: number, e: any) => sum + e.score, 0) / enterpriseEvals.length)
    : 0;
  const schoolScore = collegeEvals.length > 0
    ? Math.round(collegeEvals.reduce((sum: number, e: any) => sum + e.score, 0) / collegeEvals.length)
    : 0;
  
  return {
    enterprise_score: enterpriseScore,
    school_score: schoolScore,
    bonus_score: 0, // Not provided by backend
    total_score: apiEval.averageScore ? Math.round(parseFloat(apiEval.averageScore)) : 0,
    comments: evaluations.map((e: any) => ({
      id: String(Math.random()),
      source: e.sourceType === 'enterprise' ? 'enterprise' : 'school',
      author: e.evaluatorName,
      content: e.comment || '',
      date: e.evaluationDate,
    })),
  };
}

// Helper function to convert backend certificate response to frontend Certificate type
function mapCertificateApiToCertificate(apiCert: any): any {
  return {
    id: String(apiCert.id),
    title: apiCert.name,
    issuer: '智图平台', // Default issuer
    issue_date: apiCert.issueDate,
    hash: apiCert.blockchainHash || '',
    status: 'valid' as const,
  };
}

// Helper function to convert backend badge response to frontend Badge type
function mapBadgeApiToBadge(apiBadge: any): any {
  return {
    id: String(apiBadge.id),
    name: apiBadge.name,
    description: apiBadge.name, // Use name as description if not provided
    category: 'tech' as const, // Default category
    unlocked_at: apiBadge.issueDate,
  };
}

// Helper function to convert backend dashboard stats to frontend format
function mapDashboardStatsApiToStats(apiStats: any): any {
  return {
    training_project_count: apiStats.trainingProjectCount || 0,
    internship_job_count: apiStats.internshipJobCount || 0,
    pending_tasks_count: apiStats.pendingTaskCount || 0,
    growth_score: apiStats.growthScore || 0,
    radar_summary: apiStats.radar_summary || {
      labels: [],
      data: [],
    },
  };
}

// Helper function to convert backend radar data to frontend format
function mapRadarDataApiToRadar(apiRadar: any): any {
  const dimensions = apiRadar.dimensions || [];
  return {
    dimensions: dimensions.map((d: any) => ({
      key: d.name,
      label: d.name,
      score: d.score || 0,
      max: 100,
    })),
    peer_average: dimensions.map((d: any) => 70), // Default peer average
    history: [],
  };
}

export const fetchDashboardStats = async () => {
  try {
    const response = await fetchWithAuth<any>(
      `${BASE}/dashboard`,
      MockGenerator.getMockDashboardStats
    );
    
    console.log('Dashboard stats response:', response);
    
    if (response && typeof response === 'object') {
      const mapped = mapDashboardStatsApiToStats(response);
      console.log('Mapped dashboard stats:', mapped);
      return mapped;
    }
    
    return response;
  } catch (error) {
    console.error('Failed to fetch dashboard stats:', error);
    return MockGenerator.getMockDashboardStats();
  }
};

export const fetchRadarData = async () => {
  try {
    const response = await fetchWithAuth<any>(
      `${BASE}/capability/radar`,
      MockGenerator.getMockRadarData
    );
    
    if (response && typeof response === 'object') {
      return mapRadarDataApiToRadar(response);
    }
    
    return response;
  } catch (error) {
    console.error('Failed to fetch radar data:', error);
    return MockGenerator.getMockRadarData();
  }
};

export const fetchTasks = async (status: 'pending' | 'completed' = 'pending') => {
  try {
    const response = await fetchWithAuth<PaginatedResponse<any>>(
      `${BASE}/tasks?status=${status}`,
      MockGenerator.getMockTasks
    );
    
    // Handle paginated response from backend
    if (response && typeof response === 'object' && 'records' in response) {
      return {
        total: response.total,
        records: response.records.map((task: any) => ({
          id: String(task.id),
          type: task.taskType || 'training_submit',
          title: task.title,
          deadline: task.dueDate,
          priority: task.priority === 1 ? 'high' : task.priority === 2 ? 'medium' : 'low',
          jump_url: '#', // Default jump URL
        })),
      };
    }
    
    // Handle direct response (mock data)
    return response;
  } catch (error) {
    console.error('Failed to fetch tasks:', error);
    return MockGenerator.getMockTasks();
  }
};

export const fetchRecommendations = async (type: 'all' | 'project' | 'job' | 'course' = 'all') => {
  try {
    const response = await fetchWithAuth<any>(
      `${BASE}/recommendations?type=${type}`,
      MockGenerator.getMockRecommendations
    );
    
    console.log('Recommendations response:', response);
    
    // Handle direct array response from backend (not paginated)
    if (Array.isArray(response)) {
      const mapped = response.map((rec: any) => ({
        rec_id: String(rec.id),
        type: rec.recType === 'job' ? 'job' : rec.recType === 'project' ? 'project' : 'course',
        title: rec.title || `推荐 #${rec.id}`,
        company_name: rec.companyName,
        tags: rec.tags || [],
        match_score: rec.score ? rec.score / 100 : 0.85,
        match_reason: rec.reason || '推荐给您',
      }));
      console.log('Mapped recommendations:', mapped);
      return mapped;
    }
    
    // Handle paginated response from backend
    if (response && typeof response === 'object' && 'records' in response) {
      return response.records.map((rec: any) => ({
        rec_id: String(rec.id),
        type: rec.recType === 'job' ? 'job' : rec.recType === 'project' ? 'project' : 'course',
        title: rec.title || `推荐 #${rec.id}`,
        company_name: rec.companyName,
        tags: rec.tags || [],
        match_score: rec.score ? rec.score / 100 : 0.85,
        match_reason: rec.reason || '推荐给您',
      }));
    }
    
    return [];
  } catch (error) {
    console.error('Failed to fetch recommendations:', error);
    return MockGenerator.getMockRecommendations();
  }
};

export const fetchProjects = async (): Promise<Project[]> => {
  try {
    const response = await fetchWithAuth<PaginatedResponse<ProjectApiResponse>>(
      `${BASE}/training/projects`,
      MockGenerator.getMockProjects
    );
    
    // Handle paginated response from backend
    if (response && typeof response === 'object' && 'records' in response) {
      return response.records.map(mapProjectApiToProject);
    }
    
    // Handle direct array response (mock data)
    if (Array.isArray(response)) {
      return response;
    }
    
    return [];
  } catch (error) {
    console.error('Failed to fetch projects:', error);
    return MockGenerator.getMockProjects();
  }
};

export const fetchScrumBoard = (projectId?: string) =>
  fetchWithAuth(`${BASE}/training/projects/${projectId}/board`, MockGenerator.getMockScrumBoard);

export const fetchJobs = async () => {
  try {
    const response = await fetchWithAuth<PaginatedResponse<any>>(
      `${BASE}/internship/jobs`,
      MockGenerator.getMockJobs
    );
    
    // Handle paginated response from backend
    if (response && typeof response === 'object' && 'records' in response) {
      return response.records.map(mapJobApiToJob);
    }
    
    // Handle direct array response (mock data)
    if (Array.isArray(response)) {
      return response;
    }
    
    return [];
  } catch (error) {
    console.error('Failed to fetch jobs:', error);
    return MockGenerator.getMockJobs();
  }
};

export const fetchReports = async () => {
  try {
    const response = await fetchWithAuth<PaginatedResponse<any>>(
      `${BASE}/internship/reports/my`,
      MockGenerator.getMockReports
    );
    
    // Handle paginated response from backend
    if (response && typeof response === 'object' && 'records' in response) {
      return response.records;
    }
    
    // Handle direct array response (mock data)
    if (Array.isArray(response)) {
      return response;
    }
    
    return [];
  } catch (error) {
    console.error('Failed to fetch reports:', error);
    return MockGenerator.getMockReports();
  }
};

export const fetchEvaluation = async () => {
  try {
    const response = await fetchWithAuth<any>(
      `${BASE}/growth/evaluation`,
      MockGenerator.getMockEvaluation
    );
    
    // Backend returns evaluation summary directly (not paginated)
    if (response && typeof response === 'object') {
      return mapEvaluationApiToEvaluation(response);
    }
    
    return response;
  } catch (error) {
    console.error('Failed to fetch evaluation:', error);
    return MockGenerator.getMockEvaluation();
  }
};

export const fetchCertificates = async () => {
  try {
    const response = await fetchWithAuth<PaginatedResponse<any>>(
      `${BASE}/growth/certificates`,
      MockGenerator.getMockCertificates
    );
    
    // Handle paginated response from backend
    if (response && typeof response === 'object' && 'records' in response) {
      return response.records.map(mapCertificateApiToCertificate);
    }
    
    // Handle direct array response (mock data)
    if (Array.isArray(response)) {
      return response;
    }
    
    return [];
  } catch (error) {
    console.error('Failed to fetch certificates:', error);
    return MockGenerator.getMockCertificates();
  }
};

export const fetchBadges = async () => {
  try {
    const response = await fetchWithAuth<PaginatedResponse<any>>(
      `${BASE}/growth/badges`,
      MockGenerator.getMockBadges
    );
    
    // Handle paginated response from backend
    if (response && typeof response === 'object' && 'records' in response) {
      return response.records.map(mapBadgeApiToBadge);
    }
    
    // Handle direct array response (mock data)
    if (Array.isArray(response)) {
      return response;
    }
    
    return [];
  } catch (error) {
    console.error('Failed to fetch badges:', error);
    return MockGenerator.getMockBadges();
  }
};
