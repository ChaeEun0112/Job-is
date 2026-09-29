import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { searchJobs } from '@/api/jobs';
import type { SearchJobsParams } from '@/api/types/jobs.types';
import { QUERY_KEYS } from '@/constants/queryKey';

export function useExploreJobs(params: SearchJobsParams) {
  return useQuery({
    queryKey: QUERY_KEYS.JOBS.SEARCH(params),
    queryFn: () => searchJobs(params),
    placeholderData: keepPreviousData,
  });
}
