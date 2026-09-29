import { useEffect, useState } from 'react';

const RECOMMENDATION_INTRO_STORAGE_KEY = 'recommendation-intro-completed-at';
const RECOMMENDATION_INTRO_EXPIRATION = 1000 * 60 * 60 * 24 * 3;
const RECOMMENDATION_INTRO_DURATION = 3320;

export type RecommendationIntroAnimationMode = 'initial' | 'revisit' | 'none';

function getRecommendationIntroAnimationMode(): RecommendationIntroAnimationMode {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return 'none';

  try {
    const completedAt = Number(window.localStorage.getItem(RECOMMENDATION_INTRO_STORAGE_KEY));

    return Number.isFinite(completedAt) &&
      Date.now() - completedAt < RECOMMENDATION_INTRO_EXPIRATION
      ? 'revisit'
      : 'initial';
  } catch {
    return 'initial';
  }
}

export default function useRecommendationIntroAnimation(enabled: boolean) {
  const [animationMode] = useState<RecommendationIntroAnimationMode>(() =>
    enabled ? getRecommendationIntroAnimationMode() : 'none',
  );
  const shouldPlayIntroAnimation = animationMode === 'initial';

  useEffect(() => {
    if (!shouldPlayIntroAnimation) return;

    const timer = window.setTimeout(() => {
      try {
        window.localStorage.setItem(RECOMMENDATION_INTRO_STORAGE_KEY, String(Date.now()));
      } catch {
        // 스토리지 접근이 제한된 환경에서는 이번 방문에만 인트로를 재생한다.
      }
    }, RECOMMENDATION_INTRO_DURATION);

    return () => window.clearTimeout(timer);
  }, [shouldPlayIntroAnimation]);

  return animationMode;
}
