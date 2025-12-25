// f:\projects\zhitu\frontend\src\student\services\api.ts
import * as MockGenerator from '../mock/generator';

// Base API URL - environment variable or default
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/student-portal/v1';

/**
 * Generic fetch wrapper with fallback to mock data
 */
async function fetchWithFallback<T>(
  endpoint: string, 
  mockFn: () => T,
  options?: RequestInit
): Promise<T> {
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }
    const data = await response.json();
    return data;
  } catch (error) {
    console.warn(`Fetch failed for ${endpoint}, falling back to mock data.`, error);
    // Simulate network delay for realistic feel even with mock data
    await new Promise(resolve => setTimeout(resolve, 500));
    return mockFn();
  }
}

export const fetchDashboardStats = async () => {
  return fetchWithFallback('/dashboard', MockGenerator.getMockDashboardStats);
};

export const fetchRadarData = async () => {
  return fetchWithFallback('/capability/radar', MockGenerator.getMockRadarData);
};

export const fetchTasks = async (status: 'pending' | 'completed' = 'pending') => {
  return fetchWithFallback(`/tasks?status=${status}`, MockGenerator.getMockTasks);
};

export const fetchRecommendations = async (type: 'all' | 'project' | 'job' | 'course' = 'all') => {
  return fetchWithFallback(`/recommendations?type=${type}`, MockGenerator.getMockRecommendations);
};
