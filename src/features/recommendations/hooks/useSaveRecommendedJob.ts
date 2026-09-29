import { useMutation, useQueryClient } from '@tanstack/react-query';
import { saveJob } from '@/api/jobs';
import { QUERY_KEYS } from '@/constants/queryKey';

export function useSaveRecommendedJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: saveJob,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: QUERY_KEYS.SAVED_JOBS.LIST_BASE() }),
  });
}
