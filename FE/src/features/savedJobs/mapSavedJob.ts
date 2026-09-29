import type { SavedJob } from '@/api/types/savedJobs.types';
import type { SavedJobBadge, SavedJobListing } from '@/features/savedJobs/types/savedJob';
import { formatDDayLabel } from '@/utils/formatDDayLabel';
import { formatEmploymentType } from '@/utils/formatEmploymentType';

const RECENT_SAVE_DAYS = 7;
const DEADLINE_SOON_DAYS = 3;
const MS_PER_DAY = 1000 * 60 * 60 * 24;

function formatSavedAtLabel(savedAt: string): string {
  const saved = new Date(savedAt);
  const savedDay = new Date(saved).setHours(0, 0, 0, 0);
  const today = new Date().setHours(0, 0, 0, 0);
  const diffDays = Math.floor((today - savedDay) / MS_PER_DAY);

  if (diffDays <= 0) return '오늘 저장';
  if (diffDays < RECENT_SAVE_DAYS) return `${diffDays}일 전 저장`;
  return `${saved.getMonth() + 1}/${saved.getDate()} 저장`;
}

export function mapSavedJob(job: SavedJob): SavedJobListing {
  const badges: SavedJobBadge[] = [{ label: formatSavedAtLabel(job.savedAt), color: 'primary' }];

  if (job.expired) {
    badges.push({ label: '마감됨', color: 'disabled' });
  } else {
    const dDayLabel = formatDDayLabel(job.deadlineAt);
    const isDueSoon =
      dDayLabel === 'D-day' ||
      (dDayLabel.startsWith('D-') && Number(dDayLabel.slice(2)) <= DEADLINE_SOON_DAYS);
    badges.push({
      label: isDueSoon ? `${dDayLabel} · 마감 임박` : dDayLabel,
      color: isDueSoon ? 'warn' : 'primary',
    });
  }

  badges.push(
    job.applyIntent ? { label: '지원 의향', color: 'warn' } : { label: '저장만', color: 'primary' },
  );

  return {
    id: String(job.jobId),
    title: `${job.companyName} · ${job.title}`,
    meta: `${job.locationFull} · ${job.careerLevel} · ${formatEmploymentType(job.employmentType)}`,
    badges,
    closed: job.expired,
  };
}
