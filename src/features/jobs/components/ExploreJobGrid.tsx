import ExploreJobCard from '@/features/jobs/components/ExploreJobCard';
import type { ExploreJobSummary } from '@/features/jobs/types/exploreJob';

interface ExploreJobGridProps {
  jobs: ExploreJobSummary[];
}

export default function ExploreJobGrid({ jobs }: ExploreJobGridProps) {
  return (
    <div className="grid w-full grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-2 lg:grid-cols-3">
      {jobs.map((job) => (
        <ExploreJobCard key={job.id} job={job} />
      ))}
    </div>
  );
}
