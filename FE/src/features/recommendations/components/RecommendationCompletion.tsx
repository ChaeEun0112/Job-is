import { type ComponentPropsWithRef } from 'react';
import Button from '@/components/common/Button';
import { cn } from '@/utils/cn';

interface RecommendationCompletionProps extends ComponentPropsWithRef<'section'> {
  savedCount: number;
  dismissedCount: number;
  viewedCount: number;
  nextLetterNotice: string;
  onViewSaved?: () => void;
  onExplore?: () => void;
}

export default function RecommendationCompletion({
  className,
  ref,
  savedCount,
  dismissedCount,
  viewedCount,
  nextLetterNotice,
  onViewSaved,
  onExplore,
  ...props
}: RecommendationCompletionProps) {
  return (
    <section
      ref={ref}
      className={cn('flex flex-col items-center gap-5 text-center', className)}
      {...props}
    >
      <div className="flex flex-col items-center gap-5">
        <h1 className="text-display-medium leading-[1.2] font-bold text-text-primary">
          오늘의 레터를 다 보셨어요.
        </h1>
        <p className="text-heading-medium font-medium text-text-secondary">
          저장 {savedCount}건 · 관심 없음 {dismissedCount}건 · 열람 {viewedCount}건
        </p>
      </div>
      <div className="flex items-center gap-15">
        <Button className="w-62.5" onClick={onViewSaved}>
          저장 목록 보기
        </Button>
        <Button variant="outline" className="w-62.5" onClick={onExplore}>
          탐색 둘러보기
        </Button>
      </div>
      <p className="text-heading-small font-medium text-text-tertiary">{nextLetterNotice}</p>
    </section>
  );
}
