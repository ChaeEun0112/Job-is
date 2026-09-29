import { DetailListCard } from '@/components/common';
import JobDetailApplyPanel from '@/features/jobs/components/JobDetailApplyPanel';
import JobDetailSourceCard from '@/features/jobs/components/JobDetailSourceCard';
import type { JobDetail } from '@/features/jobs/types/jobDetail';

interface JobDetailSidebarProps {
  job: JobDetail;
  onApply?: () => void;
  onIntendToApply?: () => void;
  onSave?: () => void;
  isSaved?: boolean;
  isSavePending?: boolean;
  onNotInterested?: () => void;
  isIntendedToApply?: boolean;
}

export default function JobDetailSidebar({
  job,
  onApply,
  onIntendToApply,
  onSave,
  isSaved,
  isSavePending,
  onNotInterested,
  isIntendedToApply,
}: JobDetailSidebarProps) {
  return (
    <div className="flex w-full flex-col gap-4">
      <JobDetailApplyPanel
        onApply={onApply}
        onIntendToApply={onIntendToApply}
        onSave={onSave}
        isSaved={isSaved}
        isSavePending={isSavePending}
        onNotInterested={onNotInterested}
        isIntendedToApply={isIntendedToApply}
      />
      <DetailListCard
        title="기업 정보"
        rows={[
          { label: '업종', value: job.company.industry },
          { label: '사원수', value: job.company.employeeCount },
          { label: '기업 규모', value: job.company.companySize },
          { label: '상장 여부', value: job.company.isListed },
        ]}
      />
      <JobDetailSourceCard source={job.source} />
    </div>
  );
}
