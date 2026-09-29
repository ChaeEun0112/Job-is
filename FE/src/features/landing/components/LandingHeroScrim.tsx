import { type ReactNode } from 'react';
import { cn } from '@/utils/cn';

/**
 * `closest-side` 라야 타원이 박스 변에서 정확히 투명해진다.
 * 기본값(`farthest-corner`)은 변에 닿을 때 아직 불투명해서 박스 경계가 직선으로 드러난다.
 */
const SCRIM =
  'radial-gradient(ellipse closest-side at center, rgba(248,249,250,1) 0%, rgba(248,249,250,1) 72%, rgba(248,249,250,0) 100%)';

/**
 * 히어로 텍스트 뒤의 셰이더를 가려 가독성을 확보하는 스크림.
 *
 * 뷰포트가 아니라 감싼 요소를 기준으로 크기가 정해지므로, 카피가 길어지거나 화면이 좁아져
 * 헤드라인 줄 수가 늘어도 항상 텍스트 전체를 덮는다. 화면 폭별 좌표 보정이 필요 없다.
 *
 * 좌우 여백(`-inset-x-48`)은 타원이 텍스트 사각형을 내접하도록 잡은 값이다.
 * 타원은 같은 폭의 사각형을 덮지 못하므로 박스가 텍스트보다 넓어야 한다.
 */
export default function LandingHeroScrim({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <div className={cn('relative', className)}>
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -inset-x-48 -inset-y-16"
        style={{ background: SCRIM }}
      />
      <div className="relative">{children}</div>
    </div>
  );
}
