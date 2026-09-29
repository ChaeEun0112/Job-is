import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import type { HistoryFilter, SavedJobsSort } from '@/api/types/savedJobs.types';
import { Button, NoticePanel } from '@/components/common';
import Tab from '@/components/common/Tab';
import { showAlert, Spinner } from '@/components/feedback';
import { useSaveJob } from '@/features/jobs/hooks/useSaveJob';
import SavedJobsHistoryContent from '@/features/savedJobs/components/SavedJobsHistoryContent';
import SavedJobsSavedContent from '@/features/savedJobs/components/SavedJobsSavedContent';
import { useJobHistory } from '@/features/savedJobs/hooks/useJobHistory';
import { useSavedJobs } from '@/features/savedJobs/hooks/useSavedJobs';
import { mapHistoryItem } from '@/features/savedJobs/mapHistoryItem';
import { mapSavedJob } from '@/features/savedJobs/mapSavedJob';
import type { SavedJobHistoryStatus } from '@/features/savedJobs/types/savedJob';

const LIST_TABS = [
  { label: '저장', value: 'saved' },
  { label: '열람 • 피드백 내역', value: 'history' },
] as const;

type SavedJobsTab = (typeof LIST_TABS)[number]['value'];

const HISTORY_FILTER_TO_API: Record<SavedJobHistoryStatus | 'all', HistoryFilter> = {
  all: 'ALL',
  viewed: 'VIEWED',
  skipped: 'SKIPPED',
  intended: 'APPLY_INTENT',
};

interface SavedJobsListProps {
  isEmptyPreview?: boolean;
}

export default function SavedJobsList({ isEmptyPreview = false }: SavedJobsListProps) {
  const [activeTab, setActiveTab] = useState<SavedJobsTab>('saved');
  const [activeFilter, setActiveFilter] = useState<SavedJobHistoryStatus | 'all'>('all');
  const [unsavingJobId, setUnsavingJobId] = useState<string>();
  const [savedJobsSort, setSavedJobsSort] = useState<SavedJobsSort>('SAVED_DESC');
  const navigate = useNavigate();

  const savedJobsQuery = useSavedJobs({ page: 1, sort: savedJobsSort });
  const historyQuery = useJobHistory(HISTORY_FILTER_TO_API[activeFilter]);
  const { unsave } = useSaveJob();

  const savedJobs = isEmptyPreview
    ? []
    : (savedJobsQuery.data?.saves.content ?? []).map(mapSavedJob);
  const totalSaved = isEmptyPreview ? 0 : (savedJobsQuery.data?.totalSaved ?? 0);
  const totalApplyIntent = isEmptyPreview ? 0 : (savedJobsQuery.data?.totalApplyIntent ?? 0);
  const isSavedJobsLoading =
    !isEmptyPreview &&
    (savedJobsQuery.isLoading || (savedJobsQuery.isFetching && savedJobs.length === 0));

  const historyItems = useMemo(() => {
    const items = historyQuery.data?.pages.flatMap((page) => page.content) ?? [];
    return items
      .map(mapHistoryItem)
      .filter((item): item is NonNullable<typeof item> => item !== null);
  }, [historyQuery.data]);

  const handleBrowseRecommendations = () => navigate('/recommendations');
  const handleExplore = () => navigate('/explore', { state: { transition: 'main-tab' } });
  const handleView = (jobId: string | number) => navigate(`/jobs/${jobId}`);

  async function handleUnsave(jobId: string) {
    setUnsavingJobId(jobId);

    try {
      await unsave(Number(jobId));
      showAlert('success', '공고 저장을 해제했어요.');
    } catch {
      showAlert('danger', '공고 저장을 해제하지 못했어요. 다시 시도해주세요.');
    } finally {
      setUnsavingJobId(undefined);
    }
  }

  return (
    <div className="flex min-h-225 w-full flex-1 justify-center bg-gray-50 px-3 py-12.5">
      <div className="flex w-full max-w-300 flex-1 flex-col items-start gap-5">
        <p className="text-heading-medium font-bold text-text-primary">저장 목록</p>
        <div className="min-h-5" aria-live="polite">
          {isSavedJobsLoading ? (
            <div
              className="h-5 w-40 animate-pulse rounded bg-gray-200"
              aria-busy="true"
              aria-label="저장 목록 집계 불러오는 중"
            />
          ) : savedJobs.length > 0 ? (
            <p className="text-label-medium font-medium text-text-secondary">
              저장 {totalSaved}건 · 지원 의향 {totalApplyIntent}건
            </p>
          ) : null}
        </div>

        <div className="flex items-center gap-2.5">
          {LIST_TABS.map((tab) => (
            <Tab
              key={tab.value}
              variant="underline"
              label={tab.label}
              active={activeTab === tab.value}
              onClick={() => setActiveTab(tab.value)}
            />
          ))}
        </div>

        <div
          key={activeTab}
          className="saved-jobs-tab-content-enter flex w-full flex-1 flex-col gap-5"
        >
          {activeTab === 'saved' ? (
            !isEmptyPreview && isSavedJobsLoading ? (
              <div className="flex w-full flex-1 items-center justify-center">
                <Spinner />
              </div>
            ) : !isEmptyPreview && savedJobsQuery.isError ? (
              <div className="flex min-h-75 w-full flex-1">
                <NoticePanel
                  resultIconVariant="danger"
                  title="저장 목록을 불러오지 못했어요"
                  className="h-full max-w-none justify-center"
                >
                  <Button onClick={() => savedJobsQuery.refetch()}>다시 시도</Button>
                </NoticePanel>
              </div>
            ) : (
              <SavedJobsSavedContent
                jobs={savedJobs}
                onBrowseRecommendations={handleBrowseRecommendations}
                onExplore={handleExplore}
                onView={handleView}
                onUnsave={handleUnsave}
                unsavingJobId={unsavingJobId}
                sort={savedJobsSort}
                onSortChange={setSavedJobsSort}
              />
            )
          ) : (
            <SavedJobsHistoryContent
              history={historyItems}
              activeFilter={activeFilter}
              onFilterChange={setActiveFilter}
              onBrowseRecommendations={handleBrowseRecommendations}
              onExplore={handleExplore}
              onView={handleView}
              onLoadMore={historyQuery.hasNextPage ? () => historyQuery.fetchNextPage() : undefined}
              isLoading={historyQuery.isLoading}
              isError={historyQuery.isError}
              onRetry={() => historyQuery.refetch()}
            />
          )}
        </div>
      </div>
    </div>
  );
}
