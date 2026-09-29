import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cancelNotificationSnooze, getNotificationSettings } from '@/api/notification';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useNotificationSnoozeCancel() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      await cancelNotificationSnooze();
      return queryClient.fetchQuery({
        queryKey: QUERY_KEYS.SETTINGS.NOTIFICATION(),
        queryFn: getNotificationSettings,
        staleTime: 0,
      });
    },
  });
}
