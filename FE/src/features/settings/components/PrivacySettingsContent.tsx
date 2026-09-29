import { type ReactNode } from 'react';
import { Link } from 'react-router';
import TableCell from '@/components/common/TableCell';
import { PRIVACY_POLICY_URL, TERMS_OF_SERVICE_URL } from '@/constants/legalLinks';

const DATA_RETENTION_ROWS = [
  ['소셜 식별자 · 이메일', '로그인 · 브리핑 발송', '필수', '탈퇴 후 30일'],
  ['관심 직무 · 지역 · 경력', '추천 매칭', '필수', '탈퇴 후 30일'],
  ['이력서 · 성향 결과', '본인 보관 · 선호 시드', '선택', '삭제 시 즉시 파기'],
  ['행동 로그 · UTM', '서비스 개선', '자동', '비식별 후 통계 보존'],
];

const PRIVACY_LINKS = [
  { label: '개인정보처리방침 전문', href: PRIVACY_POLICY_URL },
  { label: '이용약관', href: TERMS_OF_SERVICE_URL },
  { label: '문의하기', href: null },
];

function PrivacyCard({ title, description }: { title: string; description: ReactNode }) {
  return (
    <section className="flex flex-col gap-5 rounded-md border border-gray-200 bg-white p-6">
      <h2 className="text-heading-medium text-text-primary">{title}</h2>
      <div className="text-label-medium font-medium text-text-tertiary">{description}</div>
    </section>
  );
}

export default function PrivacySettingsContent() {
  return (
    <div className="flex min-w-0 max-w-185 flex-1 flex-col gap-5">
      <PrivacyCard
        title="개인정보 · 데이터 안내"
        description="Job.is가 어떤 정보를, 왜, 얼마나 보관하는지 알려드려요."
      />

      <section className="flex flex-col gap-5 rounded-md border border-gray-200 bg-white p-6">
        <h2 className="text-heading-medium text-text-primary">개인정보 · 데이터 안내</h2>
        <div className="w-full overflow-x-auto rounded-xs">
          <table className="w-full min-w-169 border-collapse text-left">
            <colgroup>
              <col className="w-45" />
              <col className="w-45" />
              <col className="w-33.75" />
              <col className="w-45" />
            </colgroup>
            <thead>
              <tr>
                {['항목', '목적', '필수', '보존기간'].map((heading) => (
                  <TableCell key={heading} variant="header">
                    {heading}
                  </TableCell>
                ))}
              </tr>
            </thead>
            <tbody>
              {DATA_RETENTION_ROWS.map((row) => (
                <tr key={row[0]}>
                  {row.map((cell) => (
                    <TableCell key={cell}>{cell}</TableCell>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <PrivacyCard
        title="행동 데이터를 이렇게 써요"
        description="당신의 저장 · 스킵 · 클릭이 다음 추천을 더 맞게 만들어요. 탈퇴 후에도 비식별 통계는 보존될 수 있어요."
      />

      <PrivacyCard
        title="개인정보 · 데이터 안내"
        description={
          <p>
            이력서 · 자소서는 본인 보관용 · 자동 활용 없음. 조건 수정은{' '}
            <Link
              to="/profile"
              className="font-medium text-text-primary underline decoration-from-font [text-underline-position:from-font]"
            >
              프로필
            </Link>
            에서 직접. 열람 · 삭제 요청은 문의로 접수해요.
          </p>
        }
      />

      <nav className="flex flex-wrap gap-2.5" aria-label="개인정보 관련 링크">
        {PRIVACY_LINKS.map((link) => {
          const linkClassName =
            'flex h-10 cursor-pointer items-center justify-center rounded-full border border-gray-200 bg-white px-3 text-label-large font-normal text-text-primary transition-colors hover:bg-gray-50';

          if (!link.href) {
            return (
              <button
                key={link.label}
                type="button"
                onClick={() => alert('문의하기는 준비 중이에요.')}
                className={linkClassName}
              >
                {link.label}
              </button>
            );
          }

          return link.href.startsWith('http') ? (
            <a
              key={link.href}
              href={link.href}
              target="_blank"
              rel="noopener noreferrer"
              className={linkClassName}
            >
              {link.label}
            </a>
          ) : (
            <Link key={link.href} to={link.href} className={linkClassName}>
              {link.label}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
