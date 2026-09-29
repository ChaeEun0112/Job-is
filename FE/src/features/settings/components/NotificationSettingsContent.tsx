import { useState } from 'react';
import type {
  NotificationSettingsResponse,
  NotificationSettingsUpdateRequest,
} from '@/api/types/notification.types';
import { Spinner } from '@/components/feedback';
import DailyBriefingSettingsCard from '@/features/settings/components/DailyBriefingSettingsCard';
import MarketingConsentCard from '@/features/settings/components/MarketingConsentCard';
import NotificationPauseCard from '@/features/settings/components/NotificationPauseCard';
import useNotificationSettings from '@/features/settings/hooks/useNotificationSettings';
import useNotificationSettingsUpdate from '@/features/settings/hooks/useNotificationSettingsUpdate';

function NotificationSettingsForm({
  initialSettings,
}: {
  initialSettings: NotificationSettingsResponse;
}) {
  const settingsUpdateMutation = useNotificationSettingsUpdate();
  const [pendingField, setPendingField] = useState<keyof NotificationSettingsUpdateRequest | null>(
    null,
  );
  const settings = settingsUpdateMutation.isPending
    ? { ...initialSettings, ...settingsUpdateMutation.variables }
    : initialSettings;

  const updateField = <Key extends keyof NotificationSettingsUpdateRequest>(
    key: Key,
    value: NotificationSettingsUpdateRequest[Key],
  ) => {
    setPendingField(key);
    settingsUpdateMutation.mutate(
      {
        briefingEnabled: settings.briefingEnabled,
        sendSlot: settings.sendSlot,
        marketingSubscribed: settings.marketingSubscribed,
        [key]: value,
      },
      { onSettled: () => setPendingField(null) },
    );
  };

  return (
    <>
      {settingsUpdateMutation.isError && (
        <p
          className="rounded-xs border border-danger-500 bg-white px-6 py-4 text-label-medium font-medium text-danger-500"
          role="alert"
        >
          알림 설정을 저장하지 못했어요. 다시 시도해주세요.
        </p>
      )}
      <DailyBriefingSettingsCard
        isEnabled={settings.briefingEnabled}
        deliveryTime={settings.sendSlot}
        disabled={pendingField === 'briefingEnabled' || pendingField === 'sendSlot'}
        onEnabledChange={(isEnabled) => updateField('briefingEnabled', isEnabled)}
        onDeliveryTimeChange={(deliveryTime) => updateField('sendSlot', deliveryTime)}
      />
      <NotificationPauseCard initialSnooze={settings.snooze} />
      <MarketingConsentCard
        isEnabled={settings.marketingSubscribed}
        disabled={pendingField === 'marketingSubscribed'}
        onEnabledChange={(isEnabled) => updateField('marketingSubscribed', isEnabled)}
      />
    </>
  );
}

export default function NotificationSettingsContent() {
  const { data: settings, isPending, isError, refetch } = useNotificationSettings();

  return (
    <div className="flex min-w-0 max-w-185 flex-1 flex-col gap-5">
      {isPending ? (
        <div
          className="flex min-h-185 items-center justify-center rounded-md border border-gray-200 bg-white p-6"
          role="status"
        >
          <Spinner />
        </div>
      ) : isError || !settings ? (
        <div className="rounded-md border border-gray-200 bg-white p-6" role="alert">
          <p className="text-label-medium font-medium text-danger-500">
            알림 설정을 불러오지 못했어요.
          </p>
          <button
            type="button"
            onClick={() => refetch()}
            className="mt-3 cursor-pointer text-label-medium font-medium text-text-primary underline decoration-from-font [text-underline-position:from-font]"
          >
            다시 시도
          </button>
        </div>
      ) : (
        <NotificationSettingsForm initialSettings={settings} />
      )}
    </div>
  );
}
