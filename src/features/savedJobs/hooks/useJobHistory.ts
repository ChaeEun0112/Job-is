import { useInfiniteQuery } from '@tanstack/react-query';
import { getHistory } from '@/api/savedJobs';
import type { HistoryFilter } from '@/api/types/savedJobs.types';
import { QUERY_KEYS } from '@/constants/queryKey';

const HISTORY_PAGE_SIZE = 24;

export function useJobHistory(filter: HistoryFilter) {
  return useInfiniteQuery({
    queryKey: QUERY_KEYS.SAVED_JOBS.HISTORY(filter),
    queryFn: ({ pageParam }) => getHistory({ page: pageParam, size: HISTORY_PAGE_SIZE, filter }),
    initialPageParam: 1,
    getNextPageParam: (lastPage) => (lastPage.isLast ? undefined : lastPage.page + 1),
  });
}
