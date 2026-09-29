import StarIcon from '@/assets/icons/icon-star-solid.svg?react';
import { Badge, Tag } from '@/components/common';
import type { JobDetail } from '@/features/jobs/types/jobDetail';

interface JobDetailSummaryProps {
  job: Pick<
    JobDetail,
    | 'sourceName'
    | 'postedDate'
    | 'rating'
    | 'title'
    | 'subtitle'
    | 'employmentType'
    | 'location'
    | 'dDayLabel'
  >;
}

export default function JobDetailSummary({ job }: JobDetailSummaryProps) {
  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-col gap-2">
        <div className="flex items-center gap-1 text-body-small font-medium text-text-tertiary">
          <span>
            {job.sourceName}
            {job.postedDate && ` · ${job.postedDate}`}
          </span>
          {job.rating !== undefined && (
            <>
              <StarIcon className="ml-1 size-4" />
              <span>{job.rating.toFixed(1)}</span>
            </>
          )}
        </div>
        <h1 className="text-heading-medium font-bold text-text-primary">{job.title}</h1>
        <p className="text-body-medium font-medium text-text-secondary">{job.subtitle}</p>
      </div>
      <div className="h-px w-full bg-gray-200" />
      <div className="flex items-center gap-2">
        <Tag variant="plain" label={job.employmentType} />
        <Tag variant="plain" label={job.location} />
        <Badge type="outline" color="primary">
          {job.dDayLabel}
        </Badge>
      </div>
    </div>
  );
}
