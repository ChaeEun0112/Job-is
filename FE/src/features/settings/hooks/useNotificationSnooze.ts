import { useMutation, useQueryClient } from '@tanstack/react-query';
import { getNotificationSettings, snoozeNotification } from '@/api/notification';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useNotificationSnooze() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: Parameters<typeof snoozeNotification>[0]) => {
      await snoozeNotification(request);
      return queryClient.fetchQuery({
        queryKey: QUERY_KEYS.SETTINGS.NOTIFICATION(),
        queryFn: getNotificationSettings,
        staleTime: 0,
      });
    },
  });
}
