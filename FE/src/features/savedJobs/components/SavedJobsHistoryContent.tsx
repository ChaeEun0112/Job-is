import { useMemo } from 'react';
import { Button, NoticePanel } from '@/components/common';
import Tag from '@/components/common/Tag';
import { Spinner } from '@/components/feedback';
import SavedJobsEmptyState from '@/features/savedJobs/components/SavedJobsEmptyState';
import {
  type SavedJobHistoryItem,
  type SavedJobHistoryStatus,
} from '@/features/savedJobs/types/savedJob';
import { cn } from '@/utils/cn';

const HISTORY_FILTERS: { label: string; value: SavedJobHistoryStatus | 'all' }[] = [
  { label: '전체', value: 'all' },
  { label: '열람', value: 'viewed' },
  { label: '스킵', value: 'skipped' },
  { label: '지원 의향', value: 'intended' },
];

const HISTORY_STATUS_LABEL: Record<SavedJobHistoryStatus, string> = {
  viewed: '열람',
  skipped: '스킵',
  intended: '지원 의향',
};

interface SavedJobHistoryGroup {
  dateLabel: string;
  items: SavedJobHistoryItem[];
}

interface SavedJobsHistoryContentProps {
  history: SavedJobHistoryItem[];
  activeFilter: SavedJobHistoryStatus | 'all';
  onFilterChange: (filter: SavedJobHistoryStatus | 'all') => void;
  onBrowseRecommendations: () => void;
  onExplore: () => void;
  onView: (jobId: number) => void;
  onLoadMore?: () => void;
  isLoading?: boolean;
  isError?: boolean;
  onRetry?: () => void;
}

interface SavedJobHistoryRowProps {
  item: SavedJobHistoryItem;
  onView: (jobId: number) => void;
}

function SavedJobHistoryRow({ item, onView }: SavedJobHistoryRowProps) {
  return (
    <div className="grid w-full grid-cols-[7rem_minmax(0,1fr)_auto] items-center gap-2.5 rounded-xs border border-gray-400 bg-white p-6">
      <p className="text-body-small font-bold text-text-primary">
        {HISTORY_STATUS_LABEL[item.status]}
      </p>
      <p className="min-w-0 truncate text-body-small font-bold text-text-primary">{item.title}</p>
      <div className="flex items-center gap-2.5">
        <p className="text-body-xsmall leading-[1.3] font-semibold text-text-tertiary">
          {item.time}
        </p>
        <Button className="h-10" onClick={() => onView(item.jobId)}>
          다시 보기
        </Button>
        {item.canRestore && (
          <Button variant="outline" className="h-10">
            저장으로 되돌리기
          </Button>
        )}
      </div>
    </div>
  );
}

export default function SavedJobsHistoryContent({
  history,
  activeFilter,
  onFilterChange,
  onBrowseRecommendations,
  onExplore,
  onView,
  onLoadMore,
  isLoading = false,
  isError = false,
  onRetry,
}: SavedJobsHistoryContentProps) {
  const historyByDate = useMemo(() => {
    const filteredHistory = history.filter(
      (item) => activeFilter === 'all' || item.status === activeFilter,
    );

    return Object.values(
      filteredHistory.reduce<Record<string, SavedJobHistoryGroup>>((groups, item) => {
        const group = groups[item.date] ?? { dateLabel: item.dateLabel, items: [] };
        group.items.push(item);
        groups[item.date] = group;
        return groups;
      }, {}),
    );
  }, [activeFilter, history]);

  return (
    <>
      <div className="flex items-center gap-2.5">
        {HISTORY_FILTERS.map((filter) => (
          <Tag
            key={filter.value}
            variant="select"
            label={filter.label}
            selected={activeFilter === filter.value}
            className={cn('font-medium', activeFilter === filter.value && 'bg-primary-400')}
            onClick={() => onFilterChange(filter.value)}
          />
        ))}
      </div>

      {isLoading ? (
        <div className="flex w-full flex-1 items-center justify-center">
          <Spinner />
        </div>
      ) : isError ? (
        <div className="flex min-h-75 w-full flex-1">
          <NoticePanel
            resultIconVariant="danger"
            title="내역을 불러오지 못했어요"
            className="h-full max-w-none justify-center"
          >
            <Button onClick={onRetry}>다시 시도</Button>
          </NoticePanel>
        </div>
      ) : historyByDate.length > 0 ? (
        historyByDate.map((group) => (
          <section key={group.dateLabel} className="flex w-full flex-col gap-5">
            <p className="text-label-medium font-medium text-text-primary">{group.dateLabel}</p>
            <div className="h-0 w-full border-t border-gray-400" />
            {group.items.map((item) => (
              <SavedJobHistoryRow key={item.id} item={item} onView={onView} />
            ))}
          </section>
        ))
      ) : (
        <div className="flex w-full flex-1 items-center justify-center">
          <SavedJobsEmptyState
            title="아직 열람·피드백 내역이 없어요"
            description="오늘의 추천을 살펴보면 활동 내역이 쌓여요."
            onBrowseRecommendations={onBrowseRecommendations}
            onExplore={onExplore}
          />
        </div>
      )}

      {!isLoading && !isError && onLoadMore && (
        <Button className="h-10 w-93.75 self-center" onClick={onLoadMore}>
          더보기
        </Button>
      )}
    </>
  );
}
