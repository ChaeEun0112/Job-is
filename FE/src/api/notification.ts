import { api } from '@/api/base/request';
import type {
  NotificationSettingsResponse,
  NotificationSettingsUpdateRequest,
  NotificationSnoozeRequest,
} from './types/notification.types';

export async function getNotificationSettings() {
  return api.get<NotificationSettingsResponse>('/api/settings/notification');
}

export async function updateNotificationSettings(request: NotificationSettingsUpdateRequest) {
  return api.patch<NotificationSettingsResponse>('/api/settings/notification', request);
}

export async function snoozeNotification(request: NotificationSnoozeRequest) {
  await api.post<void>('/api/settings/notification/snooze', request);
}

export async function cancelNotificationSnooze() {
  await api.delete<void>('/api/settings/notification/snooze');
}
