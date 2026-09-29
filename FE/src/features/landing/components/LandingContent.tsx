import type { LandingOutletContext } from '@/components/layout/LandingLayout';
import LandingFeatureGrid from '@/features/landing/components/LandingFeatureGrid';
import LandingHero from '@/features/landing/components/LandingHero';
import LandingHeroScrim from '@/features/landing/components/LandingHeroScrim';

interface LandingContentProps extends LandingOutletContext {
  onStart: () => void;
  onLogin: () => void;
}

export default function LandingContent({
  headline,
  typedHeadline,
  isIntroComplete,
  shouldPlayRevisitAnimation,
  onStart,
  onLogin,
}: LandingContentProps) {
  return (
    <>
      <LandingHeroScrim>
        <LandingHero
          headline={headline}
          typedHeadline={typedHeadline}
          isIntroComplete={isIntroComplete}
          shouldPlayRevisitAnimation={shouldPlayRevisitAnimation}
          onStart={onStart}
          onLogin={onLogin}
        />
      </LandingHeroScrim>
      <LandingFeatureGrid isIntroComplete={isIntroComplete} />
    </>
  );
}
