import { useMutation, useQueryClient } from '@tanstack/react-query';
import { saveJob, unsaveJob } from '@/api/jobs';
import { QUERY_KEYS } from '@/constants/queryKey';

export function useSaveJob() {
  const queryClient = useQueryClient();
  const invalidateSaves = () =>
    queryClient.invalidateQueries({ queryKey: QUERY_KEYS.SAVED_JOBS.LIST_BASE() });

  const { mutateAsync: save } = useMutation({ mutationFn: saveJob, onSuccess: invalidateSaves });
  const { mutateAsync: unsave } = useMutation({
    mutationFn: unsaveJob,
    onSuccess: invalidateSaves,
  });

  return { save, unsave };
}
