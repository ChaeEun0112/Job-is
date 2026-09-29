import { useQuery } from '@tanstack/react-query';
import { getJobDetail } from '@/api/jobs';
import { QUERY_KEYS } from '@/constants/queryKey';

export function useJobDetail(jobId: number | null) {
  return useQuery({
    queryKey: QUERY_KEYS.JOBS.DETAIL(jobId),
    queryFn: () => getJobDetail(jobId as number),
    enabled: jobId !== null,
  });
}
