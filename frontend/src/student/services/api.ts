import { fetchWithAuth } from '@/lib/http';
import * as MockGenerator from '../mock/generator';

const BASE = import.meta.env.VITE_API_BASE_URL || '/api/student-portal/v1';

export const fetchDashboardStats = () =>
  fetchWithAuth(`${BASE}/dashboard`, MockGenerator.getMockDashboardStats);

export const fetchRadarData = () =>
  fetchWithAuth(`${BASE}/capability/radar`, MockGenerator.getMockRadarData);

export const fetchTasks = (status: 'pending' | 'completed' = 'pending') =>
  fetchWithAuth(`${BASE}/tasks?status=${status}`, MockGenerator.getMockTasks);

export const fetchRecommendations = (type: 'all' | 'project' | 'job' | 'course' = 'all') =>
  fetchWithAuth(`${BASE}/recommendations?type=${type}`, MockGenerator.getMockRecommendations);

export const fetchProjects = () =>
  fetchWithAuth(`${BASE}/training/projects`, MockGenerator.getMockProjects);

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
