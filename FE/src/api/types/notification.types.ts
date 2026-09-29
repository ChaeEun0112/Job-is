export interface NotificationSnooze {
  snoozed: boolean;
  until: string | null;
  indefinite: boolean;
}

export interface NotificationSettingsResponse {
  briefingEnabled: boolean;
  sendSlot: string;
  marketingSubscribed: boolean;
  snooze: NotificationSnooze;
}

export interface NotificationSettingsUpdateRequest {
  briefingEnabled: boolean;
  sendSlot: string;
  marketingSubscribed: boolean;
}

export type NotificationSnoozeDuration = 'SEVEN_DAYS' | 'THIRTY_DAYS' | 'INDEFINITE';

export interface NotificationSnoozeRequest {
  duration: NotificationSnoozeDuration;
}
