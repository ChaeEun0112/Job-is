import { GemSmoke } from '@paper-design/shaders-react';
import usePrefersReducedMotion from '@/hooks/usePrefersReducedMotion';
import { cn } from '@/utils/cn';

/** 브랜드 토큰: primary-500 / point-500 / secondary-500 */
const SMOKE_COLORS = ['#22c98e', '#2be6a8', '#36aef7'];
/** gray-50 — 랜딩 배경색과 같아야 캔버스 경계가 보이지 않는다 */
const BACKGROUND = '#f8f9fa';
/** 모션 최소화 상태에서 정지 화면으로 쓸 프레임 */
const STILL_FRAME = 8200;
/**
 * 헤더(불투명 흰색, 높이 88px) 아래에서 셰이더로 넘어가는 경계를 없애는 흰색 띠.
 *
 * 이 레이어는 콘텐츠보다 **뒤**에 있어야 한다. 앞에 두면 스크롤할 때 헤더 밑을 지나는
 * 글자가 흰색에 씻겨 흐려진다. 헤더 높이가 조금 달라져도 상단의 부드러운 흰 띠로만
 * 보이므로 어긋나도 티가 나지 않는다.
 */
const HEADER_BLEND =
  'linear-gradient(180deg, #ffffff 0px, #ffffff 88px, rgba(255,255,255,0) 152px)';

/**
 * 랜딩 배경 셰이더.
 *
 * 텍스트 가독성은 이 레이어가 아니라 `LandingHeroScrim` 이 책임진다.
 * 여기서 마스크로 텍스트 자리를 비우면 좌표가 뷰포트 기준이 되어 화면 폭에 따라 어긋난다.
 *
 * - `minPixelRatio={1}`: 기본값 2는 DPR 1 화면에서도 4배 픽셀을 그린다.
 * - 모션 최소화 시 `speed={0}` 으로 정지 → rAF 루프가 아예 돌지 않는다.
 * - 화면 밖·비활성 탭에서는 라이브러리가 자동으로 렌더를 멈춘다.
 */
export default function LandingShaderBackdrop({ className }: { className?: string }) {
  const prefersReducedMotion = usePrefersReducedMotion();

  return (
    <div aria-hidden="true" className={cn('pointer-events-none fixed inset-0 z-0', className)}>
      <GemSmoke
        style={{ width: '100%', height: '100%' }}
        shape="metaballs"
        colors={SMOKE_COLORS}
        colorBack={BACKGROUND}
        colorInner="#ffffff"
        innerGlow={0.55}
        outerGlow={0.35}
        innerDistortion={0.65}
        outerDistortion={0.5}
        size={0.9}
        scale={0.62}
        offsetY={-0.22}
        speed={prefersReducedMotion ? 0 : 0.25}
        frame={prefersReducedMotion ? STILL_FRAME : 0}
        minPixelRatio={1}
      />
      <div
        className="absolute inset-0"
        style={{
          background:
            'linear-gradient(180deg, rgba(248,249,250,0.05) 0%, rgba(248,249,250,0.30) 48%, rgba(248,249,250,0.70) 100%)',
        }}
      />
      <div className="absolute inset-0" style={{ background: HEADER_BLEND }} />
    </div>
  );
}
