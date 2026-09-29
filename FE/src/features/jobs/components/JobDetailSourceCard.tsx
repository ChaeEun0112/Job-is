import { Link } from '@/components/common';
import type { JobDetailSource } from '@/features/jobs/types/jobDetail';

interface JobDetailSourceCardProps {
  source: JobDetailSource;
}

export default function JobDetailSourceCard({ source }: JobDetailSourceCardProps) {
  return (
    <div className="flex w-full flex-col gap-3 rounded-sm border border-gray-300 bg-white p-6">
      <p className="text-heading-xxsmall font-bold text-text-primary">출처</p>
      <div className="flex flex-col gap-1">
        <p className="text-body-small font-bold text-text-tertiary">
          {source.siteName}
          {source.collectedDate && ` · 수집일 ${source.collectedDate}`}
        </p>
        <p className="text-body-small font-medium text-text-tertiary">{source.notice}</p>
      </div>
      <Link href={source.originalUrl} target="_blank" rel="noreferrer" className="text-body-small">
        원문 보기
      </Link>
    </div>
  );
}
