import { api } from '@/api/base/request';
import type { PageResponse } from '@/api/types';
import type {
  GetHistoryParams,
  GetSavedJobsParams,
  HistoryItem,
  SavedJobList,
} from './types/savedJobs.types';

export async function getSavedJobs(params: GetSavedJobsParams = {}) {
  return api.get<SavedJobList>('/api/saves', { params });
}

export async function getHistory(params: GetHistoryParams = {}) {
  return api.get<PageResponse<HistoryItem>>('/api/history', {
    params,
  });
}
