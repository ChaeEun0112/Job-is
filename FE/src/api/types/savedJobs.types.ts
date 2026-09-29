import type { PageResponse } from '@/api/types';

export interface SavedJob {
  jobId: number;
  companyName: string;
  title: string;
  locationFull: string;
  careerLevel: string;
  employmentType: string;
  savedAt: string;
  deadlineAt: string;
  expired: boolean;
  applyIntent: boolean;
}

export interface SavedJobList {
  totalSaved: number;
  totalApplyIntent: number;
  saves: PageResponse<SavedJob>;
}

export type SavedJobsSort = 'SAVED_DESC' | 'DEADLINE_ASC';

export interface GetSavedJobsParams {
  page?: number;
  size?: number;
  sort?: SavedJobsSort;
}

export type HistoryActionType = 'VIEWED' | 'SKIPPED' | 'APPLY_INTENT' | 'SAVED';
export type HistoryFilter = 'ALL' | HistoryActionType;

export interface HistoryItem {
  jobId: number;
  companyName: string;
  title: string;
  actionType: HistoryActionType;
  reasonCode: string | null;
  comment: string | null;
  actionAt: string;
  expired: boolean;
}

export interface GetHistoryParams {
  page?: number;
  size?: number;
  filter?: HistoryFilter;
}
