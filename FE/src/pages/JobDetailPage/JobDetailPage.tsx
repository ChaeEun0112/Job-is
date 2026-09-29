import { useEffect } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router';
import { recordJobView } from '@/api/jobs';
import { Button, NoticePanel } from '@/components/common';
import { Spinner } from '@/components/feedback';
import JobDetailApplyInterstitialModal from '@/features/jobs/components/JobDetailApplyInterstitialModal';
import JobDetailMain from '@/features/jobs/components/JobDetailMain';
import JobDetailSidebar from '@/features/jobs/components/JobDetailSidebar';
import JobDetailSkipFeedbackModal from '@/features/jobs/components/JobDetailSkipFeedbackModal';
import { useJobDetail } from '@/features/jobs/hooks/useJobDetail';
import useJobDetailActions from '@/features/jobs/hooks/useJobDetailActions';
import { mapJobDetail } from '@/features/jobs/mapJobDetail';
import { useTodayBriefingCards } from '@/features/recommendations/hooks/useTodayBriefing';

interface JobDetailLocationState {
  deckId?: number;
  cardId?: number;
}

export default function JobDetailPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const { deckId, cardId } = (location.state as JobDetailLocationState | null) ?? {};
  const parsedJobId = Number(id);
  const jobId = Number.isInteger(parsedJobId) && parsedJobId > 0 ? parsedJobId : null;

  const jobDetailQuery = useJobDetail(jobId);
  const briefingCardsQuery = useTodayBriefingCards();
  const briefingCard = briefingCardsQuery.data?.find(
    (card) => card.jobId === jobId && card.status !== 'DISMISSED',
  );
  const dismissCardIds =
    deckId !== undefined && cardId !== undefined
      ? { deckId, cardId }
      : briefingCard
        ? { deckId: briefingCard.deckId, cardId: briefingCard.cardId }
        : undefined;
  const {
    isSkipModalOpen,
    setIsSkipModalOpen,
    isApplyModalOpen,
    setIsApplyModalOpen,
    intendedToApply,
    isSaved,
    isSavePending,
    handleConfirmApply,
    handleIntendToApply,
    handleSave,
    handleSkipSubmit,
  } = useJobDetailActions({
    jobId,
    deckId: dismissCardIds?.deckId,
    cardId: dismissCardIds?.cardId,
  });

  useEffect(() => {
    if (jobId === null || !jobDetailQuery.isSuccess) return;
    recordJobView(jobId).catch(console.error);
  }, [jobId, jobDetailQuery.isSuccess]);

  if (jobId === null) {
    return (
      <div className="flex min-h-0 w-full flex-1 items-center justify-center bg-gray-50 px-3 py-8">
        <NoticePanel resultIconVariant="warning" title="존재하지 않는 공고예요">
          <Button onClick={() => navigate('/explore')}>탐색으로</Button>
        </NoticePanel>
      </div>
    );
  }

  if (jobDetailQuery.isLoading) {
    return (
      <div className="flex min-h-0 w-full flex-1 items-center justify-center bg-gray-50 px-3 py-8">
        <Spinner />
      </div>
    );
  }

  if (jobDetailQuery.isError || !jobDetailQuery.data) {
    return (
      <div className="flex min-h-0 w-full flex-1 items-center justify-center bg-gray-50 px-3 py-8">
        <NoticePanel resultIconVariant="danger" title="공고 정보를 불러오지 못했어요">
          <Button onClick={() => jobDetailQuery.refetch()}>다시 시도</Button>
        </NoticePanel>
      </div>
    );
  }

  const job = mapJobDetail(jobDetailQuery.data);

  return (
    <div className="flex min-h-0 w-full flex-1 justify-center bg-gray-50 px-3 py-8">
      <div className="flex w-full max-w-300 flex-col gap-4">
        <div className="flex w-full flex-col items-start gap-6 lg:flex-row">
          <div className="min-w-0 w-full flex-1">
            <JobDetailMain job={job} backTo="/recommendations" backLabel="오늘의 추천으로" />
          </div>
          <div className="w-full lg:w-105.5 lg:shrink-0">
            <div className="lg:sticky lg:top-24">
              <JobDetailSidebar
                job={job}
                onApply={() => setIsApplyModalOpen(true)}
                onIntendToApply={handleIntendToApply}
                onSave={handleSave}
                isSaved={isSaved}
                isSavePending={isSavePending}
                onNotInterested={() => setIsSkipModalOpen(true)}
                isIntendedToApply={intendedToApply}
              />
            </div>
          </div>
        </div>
      </div>
      {isSkipModalOpen && (
        <JobDetailSkipFeedbackModal
          onClose={() => setIsSkipModalOpen(false)}
          onSubmit={handleSkipSubmit}
        />
      )}
      {isApplyModalOpen && (
        <JobDetailApplyInterstitialModal
          sourceName={job.source.siteName}
          onClose={() => setIsApplyModalOpen(false)}
          onConfirm={(intendToApply) => handleConfirmApply(intendToApply, job.source.originalUrl)}
        />
      )}
    </div>
  );
}
