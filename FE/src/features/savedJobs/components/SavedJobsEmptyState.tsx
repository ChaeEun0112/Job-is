import Button from '@/components/common/Button';
import { ResultIcon } from '@/components/feedback';

interface SavedJobsEmptyStateProps {
  title: string;
  description: string;
  onBrowseRecommendations: () => void;
  onExplore: () => void;
}

export default function SavedJobsEmptyState({
  title,
  description,
  onBrowseRecommendations,
  onExplore,
}: SavedJobsEmptyStateProps) {
  return (
    <div className="flex min-h-150 w-full max-w-285 self-center flex-col items-center justify-center gap-5">
      <ResultIcon variant="warning" />
      <p className="text-center text-heading-medium font-semibold text-text-primary">{title}</p>
      <p className="text-center text-label-medium font-medium text-text-secondary">{description}</p>
      <div className="w-174 border-t border-gray-200" />
      <Button className="h-14 w-62.5" onClick={onBrowseRecommendations}>
        오늘의 추천 보기
      </Button>
      <button
        type="button"
        className="cursor-pointer text-label-large font-medium text-text-tertiary underline decoration-from-font [text-underline-position:from-font]"
        onClick={onExplore}
      >
        탐색 둘러보기
      </button>
    </div>
  );
}
