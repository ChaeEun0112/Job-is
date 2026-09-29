import type {
  NotificationSnooze,
  NotificationSnoozeDuration,
} from '@/api/types/notification.types';
import useNotificationSnooze from './useNotificationSnooze';
import useNotificationSnoozeCancel from './useNotificationSnoozeCancel';

export type PausePeriod = 7 | 30 | 'indefinite';

interface PauseState {
  period: PausePeriod;
  until: string | null;
}

const SNOOZE_DURATION_BY_PERIOD: Record<PausePeriod, NotificationSnoozeDuration> = {
  7: 'SEVEN_DAYS',
  30: 'THIRTY_DAYS',
  indefinite: 'INDEFINITE',
};

const PAUSE_PERIOD_BY_DURATION: Record<NotificationSnoozeDuration, PausePeriod> = {
  SEVEN_DAYS: 7,
  THIRTY_DAYS: 30,
  INDEFINITE: 'indefinite',
};

function formatResumeDate(until: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'long',
    day: 'numeric',
  }).format(new Date(until));
}

function getInitialPauseState(snooze: NotificationSnooze): PauseState | null {
  if (!snooze.snoozed) return null;
  if (snooze.indefinite) return { period: 'indefinite', until: null };
  if (!snooze.until) return null;

  const until = new Date(snooze.until);
  if (Number.isNaN(until.getTime()) || until.getTime() <= Date.now()) return null;

  const remainingDays = Math.ceil((until.getTime() - Date.now()) / (1000 * 60 * 60 * 24));
  return { period: remainingDays <= 7 ? 7 : 30, until: snooze.until };
}

function createPauseState(period: PausePeriod): PauseState {
  if (period === 'indefinite') return { period, until: null };

  const until = new Date();
  until.setDate(until.getDate() + period);
  return { period, until: until.toISOString() };
}

export default function useNotificationPause(initialSnooze: NotificationSnooze) {
  const snoozeMutation = useNotificationSnooze();
  const snoozeCancelMutation = useNotificationSnoozeCancel();
  const isUpdating = snoozeMutation.isPending || snoozeCancelMutation.isPending;
  const pauseState = snoozeMutation.isPending
    ? createPauseState(PAUSE_PERIOD_BY_DURATION[snoozeMutation.variables.duration])
    : snoozeCancelMutation.isPending
      ? null
      : getInitialPauseState(initialSnooze);

  const handlePausePeriodChange = (nextPausePeriod: PausePeriod) => {
    snoozeCancelMutation.reset();
    snoozeMutation.mutate({ duration: SNOOZE_DURATION_BY_PERIOD[nextPausePeriod] });
  };

  const handleSnoozeCancel = () => {
    snoozeMutation.reset();
    snoozeCancelMutation.mutate();
  };

  const pauseMessage =
    pauseState?.period === 'indefinite'
      ? '직접 다시 켤 때까지 쉬는 중이에요.'
      : pauseState?.until
        ? `${formatResumeDate(pauseState.until)}까지 쉬는 중이에요.`
        : '현재 알림을 정상적으로 받고 있어요.';

  return {
    pauseState,
    pauseMessage,
    isUpdating,
    isSnoozeError: snoozeMutation.isError,
    isCancelError: snoozeCancelMutation.isError,
    handlePausePeriodChange,
    handleSnoozeCancel,
  };
}
