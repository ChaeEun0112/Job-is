import type { JobSortOption } from '@/api/types/jobs.types';
import { Dropdown, Tag } from '@/components/common';
import type { ExploreActiveFilter } from '@/features/jobs/types/exploreJob';

const SORT_OPTIONS: { label: string; value: JobSortOption }[] = [
  { label: '추천순', value: 'FIT' },
  { label: '최신순', value: 'RECENT' },
  { label: '마감임박순', value: 'DEADLINE' },
];

interface ExploreResultsToolbarProps {
  resultCount: number;
  activeFilters: ExploreActiveFilter[];
  onReset?: () => void;
  isLoading?: boolean;
  sort: JobSortOption;
  onSortChange: (sort: JobSortOption) => void;
}

export default function ExploreResultsToolbar({
  resultCount,
  activeFilters,
  onReset,
  isLoading = false,
  sort,
  onSortChange,
}: ExploreResultsToolbarProps) {
  return (
    <div className="flex w-full items-center justify-between gap-2">
      <div className="flex flex-wrap items-center gap-2">
        {isLoading ? (
          <div
            className="h-5 w-28 animate-pulse rounded bg-gray-200"
            aria-busy="true"
            aria-label="검색 결과 확인 중"
          />
        ) : (
          <>
            <p className="text-label-large font-semibold text-text-primary">
              검색 결과 {resultCount}건
            </p>
            {activeFilters.map((filter) => (
              <Tag
                key={filter.key}
                variant="removable"
                label={filter.label}
                className="border-transparent bg-primary-400 text-text-primary"
                onClick={filter.onRemove}
              />
            ))}
            <button
              type="button"
              onClick={onReset}
              className="cursor-pointer text-label-large font-medium text-text-tertiary underline underline-offset-2"
            >
              초기화
            </button>
          </>
        )}
      </div>
      <Dropdown
        placeholder="정렬"
        size="sm"
        className="w-30 shrink-0"
        options={SORT_OPTIONS}
        value={sort}
        onChange={(value) => onSortChange(value as JobSortOption)}
      />
    </div>
  );
}
