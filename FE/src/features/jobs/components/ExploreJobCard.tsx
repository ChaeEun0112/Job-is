import { useState } from 'react';
import { Link } from 'react-router';
import MoreVerticalIcon from '@/assets/icons/icon-more-vertical.svg?react';
import { showAlert } from '@/components/feedback';
import JobCard from '@/features/jobs/components/JobCard';
import { useSaveJob } from '@/features/jobs/hooks/useSaveJob';
import type { ExploreJobSummary } from '@/features/jobs/types/exploreJob';
import useDismissableOpen from '@/hooks/useDismissableOpen';

interface ExploreJobCardProps {
  job: ExploreJobSummary;
}

export default function ExploreJobCard({ job }: ExploreJobCardProps) {
  const { isOpen: isMenuOpen, setIsOpen: setIsMenuOpen, containerRef } = useDismissableOpen(false);
  const [isSaved, setIsSaved] = useState(false);
  const [isSavePending, setIsSavePending] = useState(false);
  const { save, unsave } = useSaveJob();

  async function handleSave() {
    if (isSaved || isSavePending) return;

    setIsSavePending(true);
    try {
      await save(job.id);
      setIsSaved(true);
      setIsMenuOpen(false);
      showAlert('success', '공고를 저장했어요.');
    } catch {
      showAlert('danger', '공고를 저장하지 못했어요. 다시 시도해주세요.');
    } finally {
      setIsSavePending(false);
    }
  }

  async function handleUnsave() {
    if (!isSaved || isSavePending) return;

    setIsSavePending(true);
    try {
      await unsave(job.id);
      setIsSaved(false);
      setIsMenuOpen(false);
      showAlert('success', '공고 저장을 해제했어요.');
    } catch {
      showAlert('danger', '공고 저장을 해제하지 못했어요. 다시 시도해주세요.');
    } finally {
      setIsSavePending(false);
    }
  }

  return (
    <div ref={containerRef} className="relative w-full">
      <Link to={`/jobs/${job.id}`} className="block w-full">
        <JobCard
          className="w-full"
          thumbnailUrl={job.thumbnailUrl}
          dDayLabel={job.dDayLabel}
          matchScoreLabel={job.matchScoreLabel}
          title={job.title}
          companyName={job.companyName}
          employmentInfo={job.employmentInfo}
        />
      </Link>
      <button
        type="button"
        onClick={() => setIsMenuOpen((prev) => !prev)}
        aria-label="더보기"
        aria-expanded={isMenuOpen}
        aria-haspopup="menu"
        className="absolute top-51.25 right-1.5 flex size-8 items-center justify-center rounded-full text-text-primary transition-[background-color,transform] duration-150 ease-out hover:bg-gray-100 active:scale-95 active:bg-gray-200 aria-expanded:bg-gray-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-400 motion-reduce:transition-none"
      >
        <MoreVerticalIcon className="size-5" />
      </button>
      {isMenuOpen && (
        <div className="absolute top-9 right-2.5 z-10 flex w-36 origin-bottom-right flex-col gap-3 rounded-sm border border-gray-200 bg-white p-4 shadow-md motion-safe:animate-[explore-job-menu-enter_180ms_cubic-bezier(0.16,1,0.3,1)_both]">
          <button
            type="button"
            onClick={handleSave}
            disabled={isSaved || isSavePending}
            className="rounded-xs px-1 py-0.5 text-left text-body-medium font-medium text-text-primary transition-colors duration-150 ease-out hover:bg-gray-100 disabled:cursor-not-allowed disabled:text-gray-400 disabled:hover:bg-transparent"
          >
            저장
          </button>
          <div className="h-0 w-full border-t border-dashed border-gray-300" />
          <button
            type="button"
            disabled={!isSaved || isSavePending}
            onClick={handleUnsave}
            className="rounded-xs px-1 py-0.5 text-left text-body-medium font-medium text-text-primary transition-colors duration-150 ease-out hover:bg-gray-100 disabled:cursor-not-allowed disabled:text-gray-400 disabled:hover:bg-transparent"
          >
            저장 해제
          </button>
          <div className="h-0 w-full border-t border-dashed border-gray-300" />
          <button
            type="button"
            onClick={() => setIsMenuOpen(false)}
            className="rounded-xs px-1 py-0.5 text-left text-body-medium font-medium text-text-primary transition-colors duration-150 ease-out hover:bg-gray-100"
          >
            관심 없음
          </button>
        </div>
      )}
    </div>
  );
}
