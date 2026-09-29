import type { JobSummary } from '@/api/types/jobs.types';
import type { ExploreJobSummary } from '@/features/jobs/types/exploreJob';
import { formatDDayLabel } from '@/utils/formatDDayLabel';
import { formatEmploymentType } from '@/utils/formatEmploymentType';

function formatMatchScoreLabel(fitScore: number | null): string | undefined {
  if (fitScore === null) return undefined;
  return `적합도 ${Math.round(fitScore)}%`;
}

export function mapJobSummary(job: JobSummary): ExploreJobSummary {
  const location = job.locationDistrict
    ? `${job.locationCity} ${job.locationDistrict}`
    : job.locationCity;

  return {
    id: job.id,
    thumbnailUrl: job.thumbnailUrl,
    dDayLabel: formatDDayLabel(job.dueTime),
    matchScoreLabel: formatMatchScoreLabel(job.fitScore),
    title: job.position,
    companyName: job.companyName,
    employmentInfo: `${location} · ${formatEmploymentType(job.employmentType)} · ${job.careerLevel}`,
    isRemote: job.remoteAvailable,
  };
}
