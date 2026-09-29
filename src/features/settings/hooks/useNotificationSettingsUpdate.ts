import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateNotificationSettings } from '@/api/notification';
import type { NotificationSettingsResponse } from '@/api/types/notification.types';
import { showAlert } from '@/components/feedback';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useNotificationSettingsUpdate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateNotificationSettings,
    onSuccess: (settings) => {
      queryClient.setQueryData<NotificationSettingsResponse>(
        QUERY_KEYS.SETTINGS.NOTIFICATION(),
        settings,
      );
      showAlert('success', '설정을 저장했어요.');
    },
    onError: () => {
      showAlert('danger', '알림 설정을 저장하지 못했어요. 다시 시도해주세요.');
    },
  });
}
