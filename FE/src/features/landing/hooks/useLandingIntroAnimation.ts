import { useEffect, useState } from 'react';

const LANDING_INTRO_STORAGE_KEY = 'landing-intro-completed-at';
const LANDING_INTRO_EXPIRATION = 1000 * 60 * 60 * 24 * 3;

export default function useLandingIntroAnimation(headline: string, interval: number) {
  const [prefersReducedMotion] = useState(
    () => window.matchMedia('(prefers-reduced-motion: reduce)').matches,
  );
  const [shouldSkipIntro] = useState(() => {
    if (prefersReducedMotion) return true;

    try {
      const completedAt = Number(window.localStorage.getItem(LANDING_INTRO_STORAGE_KEY));

      return Number.isFinite(completedAt) && Date.now() - completedAt < LANDING_INTRO_EXPIRATION;
    } catch {
      return false;
    }
  });
  const [typedHeadline, setTypedHeadline] = useState(() => (shouldSkipIntro ? headline : ''));
  const [isIntroComplete, setIsIntroComplete] = useState(shouldSkipIntro);

  useEffect(() => {
    if (shouldSkipIntro) return;

    let characterIndex = 0;
    const timer = window.setInterval(() => {
      characterIndex += 1;
      setTypedHeadline(headline.slice(0, characterIndex));

      if (characterIndex === headline.length) {
        window.clearInterval(timer);
        setIsIntroComplete(true);

        try {
          window.localStorage.setItem(LANDING_INTRO_STORAGE_KEY, String(Date.now()));
        } catch {
          // 스토리지 접근이 제한된 환경에서는 이번 방문에만 인트로를 재생한다.
        }
      }
    }, interval);

    return () => window.clearInterval(timer);
  }, [headline, interval, shouldSkipIntro]);

  return {
    typedHeadline,
    isIntroComplete,
    shouldPlayRevisitAnimation: shouldSkipIntro && !prefersReducedMotion,
  };
}
