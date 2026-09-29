import { useQuery } from '@tanstack/react-query';
import { getNotificationSettings } from '@/api/notification';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useNotificationSettings() {
  return useQuery({
    queryKey: QUERY_KEYS.SETTINGS.NOTIFICATION(),
    queryFn: getNotificationSettings,
  });
}
