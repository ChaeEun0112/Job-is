import { useSyncExternalStore } from 'react';

const QUERY = '(prefers-reduced-motion: reduce)';

function subscribe(onStoreChange: () => void) {
  const mediaQuery = window.matchMedia(QUERY);
  mediaQuery.addEventListener('change', onStoreChange);

  return () => mediaQuery.removeEventListener('change', onStoreChange);
}

function getSnapshot() {
  return window.matchMedia(QUERY).matches;
}

/**
 * 사용자가 OS 수준에서 모션 최소화를 선택했는지 여부.
 * 애니메이션 배경·전환을 정지 상태로 대체할 때 사용한다.
 */
export default function usePrefersReducedMotion() {
  return useSyncExternalStore(subscribe, getSnapshot, () => false);
}
