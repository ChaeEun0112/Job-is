const MS_PER_DAY = 1000 * 60 * 60 * 24;

export function formatDDayLabel(dueDateIso: string | null): string {
  if (!dueDateIso) return '상시';

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const due = new Date(dueDateIso);
  due.setHours(0, 0, 0, 0);

  const diffDays = Math.round((due.getTime() - today.getTime()) / MS_PER_DAY);

  if (Number.isNaN(diffDays)) return '상시';
  if (diffDays < 0) return '마감';
  if (diffDays === 0) return 'D-day';
  return `D-${diffDays}`;
}
