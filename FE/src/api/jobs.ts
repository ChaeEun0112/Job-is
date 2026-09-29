import { api } from '@/api/base/request';
import type { PageResponse } from '@/api/types';
import type {
  CareerLevelMetadata,
  JobCareerRange,
  JobCategory,
  JobDetail,
  JobInteraction,
  JobLinkValidation,
  JobSummary,
  Region,
  SearchJobsParams,
  TechStack,
} from './types/jobs.types';

const JOB_CAREER_RANGES: readonly JobCareerRange[] = ['NEWCOMER', 'JUNIOR', 'SENIOR'];

export function isJobCareerRange(value: string): value is JobCareerRange {
  return (JOB_CAREER_RANGES as readonly string[]).includes(value);
}

export async function searchJobs(params: SearchJobsParams = {}) {
  return api.get<PageResponse<JobSummary>>('/api/jobs/search', {
    params,
    // 기본 직렬화는 배열을 cities[]=... 형태로 보내는데, 백엔드는 cities=...&cities=... 형태를 기대함
    paramsSerializer: { indexes: null },
  });
}

export async function getJobCategories() {
  return api.get<JobCategory[]>('/api/jobs/filters/job-categories');
}

export async function getTechStacks() {
  return api.get<TechStack[]>('/api/jobs/filters/tech-stacks');
}

export async function getRegions() {
  return api.get<Region[]>('/api/jobs/filters/regions');
}

export async function getCareerLevels() {
  return api.get<CareerLevelMetadata[]>('/api/jobs/filters/career-levels');
}

export async function getEmploymentTypes() {
  return api.get<string[]>('/api/jobs/filters/employment-types');
}

export async function getJobDetail(jobId: number) {
  return api.get<JobDetail>(`/api/jobs/${jobId}`);
}

export async function checkJobLink(jobId: number) {
  return api.get<JobLinkValidation>(`/api/jobs/${jobId}/source-check`);
}

export async function saveJob(jobId: number) {
  await api.post<void>(`/api/jobs/${jobId}/save`);
}

export async function unsaveJob(jobId: number) {
  await api.delete<void>(`/api/jobs/${jobId}/save`);
}

export async function recordJobView(jobId: number) {
  return api.post<JobInteraction>(`/api/jobs/${jobId}/view`);
}

export async function toggleApplyIntent(jobId: number) {
  return api.post<JobInteraction>(`/api/jobs/${jobId}/apply-intent`);
}

export async function recordJobApply(jobId: number, applyIntent?: boolean) {
  return api.post<JobInteraction>(
    `/api/jobs/${jobId}/apply`,
    applyIntent === undefined ? undefined : { applyIntent },
  );
}
