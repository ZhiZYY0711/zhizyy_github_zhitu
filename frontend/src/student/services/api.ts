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

export const fetchDashboardStats = () =>
  fetchWithAuth(`${BASE}/dashboard`, MockGenerator.getMockDashboardStats);

export const fetchRadarData = () =>
  fetchWithAuth(`${BASE}/capability/radar`, MockGenerator.getMockRadarData);

export const fetchTasks = (status: 'pending' | 'completed' = 'pending') =>
  fetchWithAuth(`${BASE}/tasks?status=${status}`, MockGenerator.getMockTasks);

export const fetchRecommendations = (type: 'all' | 'project' | 'job' | 'course' = 'all') =>
  fetchWithAuth(`${BASE}/recommendations?type=${type}`, MockGenerator.getMockRecommendations);

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

export const fetchJobs = () =>
  fetchWithAuth(`${BASE}/internship/jobs`, MockGenerator.getMockJobs);

export const fetchReports = () =>
  fetchWithAuth(`${BASE}/internship/reports/my`, MockGenerator.getMockReports);

export const fetchEvaluation = () =>
  fetchWithAuth(`${BASE}/growth/evaluation`, MockGenerator.getMockEvaluation);

export const fetchCertificates = () =>
  fetchWithAuth(`${BASE}/growth/certificates`, MockGenerator.getMockCertificates);

export const fetchBadges = () =>
  fetchWithAuth(`${BASE}/growth/badges`, MockGenerator.getMockBadges);
