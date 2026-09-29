import { type ComponentPropsWithRef } from 'react';
import LandingFeatureCard, {
  type LandingFeatureCardData,
} from '@/features/landing/components/LandingFeatureCard';
import { cn } from '@/utils/cn';

const FEATURE_CARDS: LandingFeatureCardData[] = [
  {
    title: '하루 한통의 편지',
    lines: ['무한 스크롤 대신, 매일 아침 정해진 시각에 딱 한 번 도착해요.'],
  },
  {
    title: '추천 이유 설명',
    lines: ['왜 이 공고인지, 당신의 기준으로 ', '근거를 함께 보여드려요.'],
  },
  {
    title: '무한 피드 아님',
    lines: ['많이 보여주지 않아요.', '하루 5건, 판단할 만큼만.'],
  },
  { lines: ['프로필 입력', '직무 • 지역 • 경력'], dashed: true },
  { lines: ['매일 아침 큐레이션', '07:30 발송'], dashed: true },
  { lines: ['판단 • 행동', '저장 • 지원 • 넘기기'], dashed: true },
];

const CARD_DELAY_CLASSES = [
  'landing-reveal--delay-3',
  'landing-reveal--delay-4',
  'landing-reveal--delay-5',
  'landing-reveal--delay-6',
  'landing-reveal--delay-7',
  'landing-reveal--delay-8',
] as const;

interface LandingFeatureGridProps extends ComponentPropsWithRef<'section'> {
  isIntroComplete: boolean;
}

export default function LandingFeatureGrid({
  className,
  ref,
  isIntroComplete,
  ...props
}: LandingFeatureGridProps) {
  const revealClassName = isIntroComplete
    ? 'landing-reveal landing-reveal--visible'
    : 'landing-reveal';

  return (
    <section
      ref={ref}
      aria-label="서비스 특징"
      className={cn('mt-12 w-full max-w-[910px]', className)}
      {...props}
    >
      <h2 className="sr-only">Job.is 주요 기능</h2>
      <div className="grid grid-cols-3 gap-x-[65px] gap-y-5">
        {FEATURE_CARDS.map((card, index) => (
          <LandingFeatureCard
            key={card.lines[0]}
            {...card}
            className={cn(revealClassName, CARD_DELAY_CLASSES[index])}
          />
        ))}
      </div>
    </section>
  );
}
