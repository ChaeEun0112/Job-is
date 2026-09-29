import { useNavigate } from 'react-router';
import type { NotificationSnooze } from '@/api/types/notification.types';
import useNotificationPause, {
  type PausePeriod,
} from '@/features/settings/hooks/useNotificationPause';
import { cn } from '@/utils/cn';

const PAUSE_OPTIONS: { label: string; value: PausePeriod }[] = [
  { label: '7일', value: 7 },
  { label: '30일', value: 30 },
  { label: '직접 해제', value: 'indefinite' },
];

interface NotificationPauseCardProps {
  initialSnooze: NotificationSnooze;
}

export default function NotificationPauseCard({ initialSnooze }: NotificationPauseCardProps) {
  const navigate = useNavigate();
  const {
    pauseState,
    pauseMessage,
    isUpdating,
    isSnoozeError,
    isCancelError,
    handlePausePeriodChange,
    handleSnoozeCancel,
  } = useNotificationPause(initialSnooze);

  return (
    <section className="flex flex-col gap-5 rounded-md border border-gray-200 bg-white p-6">
      <h2 className="text-heading-medium text-text-primary">브리핑 일시 정지</h2>

      <div className="flex items-center justify-between gap-5 max-sm:flex-col max-sm:items-start">
        <p className="text-label-medium font-medium text-text-primary">
          오늘의 공고 추천을 잠시 멈춰요
        </p>
        <div className="flex flex-wrap gap-2.5 px-2.5" aria-busy={isUpdating}>
          {PAUSE_OPTIONS.map((option) => (
            <button
              key={option.label}
              type="button"
              aria-pressed={pauseState?.period === option.value}
              onClick={() => {
                if (isUpdating) return;

                if (option.value === 'indefinite') {
                  navigate('/unsubscribe', { state: { transition: 'settings-flow' } });
                  return;
                }

                handlePausePeriodChange(option.value);
              }}
              className={cn(
                'h-10 cursor-pointer rounded-full border border-gray-200 bg-white px-3 text-label-large font-normal text-text-primary transition-colors hover:bg-gray-50',
                pauseState?.period === option.value &&
                  'border-primary-600 bg-primary-600 hover:bg-primary-600',
              )}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {isSnoozeError && (
        <p className="text-label-small font-medium text-danger-500" role="alert">
          알림 일시 정지를 설정하지 못했어요. 다시 시도해주세요.
        </p>
      )}

      {isCancelError && (
        <p className="text-label-small font-medium text-danger-500" role="alert">
          알림 일시 정지를 해제하지 못했어요. 다시 시도해주세요.
        </p>
      )}

      <div className="flex min-h-18 items-center justify-between gap-5 rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6">
        <span className="text-label-medium font-medium text-text-tertiary">{pauseMessage}</span>
        {pauseState && (
          <button
            type="button"
            disabled={isUpdating}
            onClick={handleSnoozeCancel}
            className="shrink-0 cursor-pointer text-label-medium font-medium text-text-primary underline decoration-from-font [text-underline-position:from-font] disabled:cursor-not-allowed disabled:text-gray-500"
          >
            지금 다시 받기
          </button>
        )}
      </div>
    </section>
  );
}
