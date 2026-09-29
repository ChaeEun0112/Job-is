import { type ComponentPropsWithRef } from 'react';
import Button from '@/components/common/Button';
import { cn } from '@/utils/cn';

interface LandingHeroProps extends ComponentPropsWithRef<'section'> {
  headline: string;
  typedHeadline: string;
  isIntroComplete: boolean;
  shouldPlayRevisitAnimation: boolean;
  onStart?: () => void;
  onLogin?: () => void;
}

export default function LandingHero({
  className,
  ref,
  headline,
  typedHeadline,
  isIntroComplete,
  shouldPlayRevisitAnimation,
  onStart,
  onLogin,
  ...props
}: LandingHeroProps) {
  const revealClassName = isIntroComplete
    ? 'landing-reveal landing-reveal--visible'
    : 'landing-reveal';
  const headlineClassName = cn(
    'min-h-33.5 text-center text-display-medium leading-[1.2] font-bold whitespace-pre-line text-text-primary',
    shouldPlayRevisitAnimation && 'landing-reveal landing-reveal--visible',
  );

  return (
    <section ref={ref} className={cn('flex flex-col items-center', className)} {...props}>
      <div className="flex flex-col items-center gap-5">
        <h1 aria-label={headline.replace('\n', ' ')} className={headlineClassName}>
          <span aria-hidden="true">{typedHeadline}</span>
        </h1>
        <p
          className={cn(
            revealClassName,
            'landing-reveal--delay-1 text-center text-heading-medium font-medium text-text-secondary',
          )}
        >
          847건을 뒤지는 대신, 5건을 판단하세요.
        </p>
      </div>

      <div
        className={cn(
          revealClassName,
          'landing-reveal--delay-2 mt-5 flex flex-col items-center gap-4',
        )}
      >
        <Button className="w-37.5" onClick={onStart}>
          시작하기
        </Button>
        <button
          type="button"
          onClick={onLogin}
          className="cursor-pointer text-label-large font-medium text-text-tertiary underline decoration-solid decoration-from-font transition-colors [text-underline-position:from-font] hover:text-text-secondary"
        >
          이미 계정이 있어요 → 로그인
        </button>
      </div>
    </section>
  );
}
