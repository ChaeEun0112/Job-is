import { useQuery } from '@tanstack/react-query';
import { getSavedJobs } from '@/api/savedJobs';
import type { GetSavedJobsParams } from '@/api/types/savedJobs.types';
import { QUERY_KEYS } from '@/constants/queryKey';

export function useSavedJobs(params: GetSavedJobsParams) {
  return useQuery({
    queryKey: QUERY_KEYS.SAVED_JOBS.LIST(params),
    queryFn: () => getSavedJobs(params),
  });
}
