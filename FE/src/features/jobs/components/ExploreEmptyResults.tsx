import { Button, NoticePanel, Tag } from '@/components/common';
import type { ExploreActiveFilter } from '@/features/jobs/types/exploreJob';

interface ExploreEmptyResultsProps {
  activeFilters: ExploreActiveFilter[];
  onResetFilters: () => void;
}

export default function ExploreEmptyResults({
  activeFilters,
  onResetFilters,
}: ExploreEmptyResultsProps) {
  return (
    <NoticePanel
      resultIconVariant="warning"
      title="조건에 맞는 공고가 없어요"
      className="w-full max-w-full border-none bg-transparent px-0 pt-16 pb-0 shadow-none"
    >
      {activeFilters.length > 0 && (
        <div className="flex w-full flex-wrap items-center justify-center gap-2">
          {activeFilters.map((filter) => (
            <Tag
              key={filter.key}
              variant="removable"
              label={filter.label}
              className="h-8 gap-1 text-body-small"
              onClick={filter.onRemove}
            />
          ))}
        </div>
      )}
      <p className="text-body-medium font-medium text-text-secondary">
        필터를 조금 더 넓혀 보세요.
      </p>
      <div className="h-px w-full bg-gray-200" />
      <Button variant="outline" className="w-full max-w-100" onClick={onResetFilters}>
        필터 초기화
      </Button>
    </NoticePanel>
  );
}
