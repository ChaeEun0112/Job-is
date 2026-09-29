import { type ComponentPropsWithRef } from 'react';
import SunIcon from '@/assets/icons/icon-sun.svg?react';
import Button from '@/components/common/Button';
import type { RecommendationIntroAnimationMode } from '@/features/recommendations/hooks/useRecommendationIntroAnimation';
import { cn } from '@/utils/cn';

interface RecommendationGreetingProps extends ComponentPropsWithRef<'section'> {
  reviewedCount: number;
  matchedCount: number;
  focusDescription: string;
  animationMode?: RecommendationIntroAnimationMode;
  onStart?: () => void;
}

export default function RecommendationGreeting({
  className,
  ref,
  reviewedCount,
  matchedCount,
  focusDescription,
  animationMode = 'none',
  onStart,
  ...props
}: RecommendationGreetingProps) {
  const revealClassName = (delay: number) => {
    if (animationMode === 'initial') {
      return `recommendation-intro-reveal recommendation-intro-reveal--delay-${delay}`;
    }

    if (animationMode === 'revisit') {
      return `recommendation-intro-revisit recommendation-intro-revisit--delay-${delay}`;
    }
  };

  return (
    <section
      ref={ref}
      className={cn('flex flex-col items-center gap-6 text-center', className)}
      {...props}
    >
      <div className="flex flex-col items-center gap-3">
        <p
          className={cn(
            revealClassName(1),
            'flex items-center gap-1 text-heading-medium font-semibold text-text-secondary',
          )}
        >
          <SunIcon aria-hidden="true" className="size-5" />
          좋은 아침 입니다
        </p>
        <p
          className={cn(
            revealClassName(2),
            'text-heading-medium font-semibold text-text-secondary',
          )}
        >
          오늘 들어온 공고 {reviewedCount}건을 검토했습니다.
        </p>
        <h1
          className={cn(
            revealClassName(3),
            'text-display-medium font-bold leading-[1.2] text-text-primary',
          )}
        >
          당신에게 맞는 단{' '}
          <span className="underline [text-underline-position:from-font]">{matchedCount}건</span>만
          추렸습니다.
        </h1>
        <p
          className={cn(
            revealClassName(4),
            'text-heading-medium font-semibold text-text-secondary',
          )}
        >
          {focusDescription}
        </p>
      </div>
      <Button className={cn(revealClassName(5), 'w-45')} onClick={onStart}>
        바로 보기
      </Button>
    </section>
  );
}
