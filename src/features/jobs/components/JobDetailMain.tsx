import { Link } from 'react-router';
import ChevronLeftIcon from '@/assets/icons/icon-chevron-left.svg?react';
import JobDetailAtAGlance from '@/features/jobs/components/JobDetailAtAGlance';
import JobDetailContent from '@/features/jobs/components/JobDetailContent';
import JobDetailFitCriteria from '@/features/jobs/components/JobDetailFitCriteria';
import JobDetailMatchReasons from '@/features/jobs/components/JobDetailMatchReasons';
import JobDetailSummary from '@/features/jobs/components/JobDetailSummary';
import type { JobDetail } from '@/features/jobs/types/jobDetail';

interface JobDetailMainProps {
  job: JobDetail;
  backTo: string;
  backLabel: string;
}

export default function JobDetailMain({ job, backTo, backLabel }: JobDetailMainProps) {
  return (
    <div className="flex w-full flex-col gap-6 rounded-sm border border-gray-300 bg-white p-6">
      <Link
        to={backTo}
        className="inline-flex w-fit items-center gap-1 rounded-sm border border-primary-400 bg-white px-4 py-2 text-body-small font-medium text-text-secondary hover:bg-primary-50"
      >
        <ChevronLeftIcon className="size-4" />
        {backLabel}
      </Link>
      <JobDetailSummary job={job} />
      <JobDetailAtAGlance items={job.glanceItems} />
      {job.fitCriteria.length > 0 && <JobDetailFitCriteria items={job.fitCriteria} />}
      {job.matchScore !== undefined && (
        <JobDetailMatchReasons matchScore={job.matchScore} reasons={job.matchReasons} />
      )}
      <JobDetailContent sections={job.contentSections} techStack={job.techStack} />
    </div>
  );
}
