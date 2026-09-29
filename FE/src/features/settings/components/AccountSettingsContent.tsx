import { type ReactNode } from 'react';
import { Link } from 'react-router';
import Badge from '@/components/common/Badge';
import { Spinner } from '@/components/feedback';
import useAccount from '@/features/settings/hooks/useAccount';
import useAccountLogout from '@/features/settings/hooks/useAccountLogout';

const SMALL_OUTLINE_BUTTON_CLASS_NAME =
  'inline-flex h-7.5 cursor-pointer items-center justify-center rounded-sm border border-primary-400 bg-white px-3 text-label-small font-normal text-text-primary transition-colors hover:bg-primary-50 disabled:cursor-not-allowed disabled:border-gray-200 disabled:bg-gray-100 disabled:text-gray-600 disabled:hover:bg-gray-100';

function AccountSettingsCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="flex flex-col gap-5 rounded-md border border-gray-200 bg-white p-6">
      <h2 className="text-heading-medium text-text-primary">{title}</h2>
      {children}
    </section>
  );
}

function formatJoinedAt(value: string) {
  const joinedAt = new Date(value);
  if (Number.isNaN(joinedAt.getTime())) return '가입일 확인 필요';

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(joinedAt);
}

export default function AccountSettingsContent() {
  const { data: account, isPending: isAccountPending, isError: isAccountError } = useAccount();
  const { logout, isLoggingOut, isLogoutError } = useAccountLogout();

  if (isAccountPending) {
    return (
      <div
        className="flex min-h-145 w-full min-w-0 max-w-185 flex-1 items-center justify-center rounded-md border border-gray-200 bg-white p-6"
        role="status"
      >
        <Spinner />
      </div>
    );
  }

  return (
    <div className="flex min-w-0 max-w-185 flex-1 flex-col gap-5">
      <AccountSettingsCard title="연결된 계정">
        {isAccountError || !account ? (
          <p className="text-label-medium font-medium text-danger-500">
            계정 정보를 불러오지 못했어요.
          </p>
        ) : (
          <p className="text-label-medium font-medium text-text-primary">
            {account.socialType === 'KAKAO' ? '카카오' : '구글'}로 연결됨 ·{' '}
            {formatJoinedAt(account.joinedAt)} 가입
          </p>
        )}
      </AccountSettingsCard>

      <AccountSettingsCard title="수신 이메일">
        <div className="flex items-center justify-between gap-5">
          <div className="flex min-w-0 items-center gap-2.5">
            {isAccountError || !account ? (
              <p className="text-body-medium font-medium text-danger-500">
                이메일을 불러오지 못했어요.
              </p>
            ) : (
              <>
                <p className="truncate text-body-medium font-medium text-gray-1000">
                  {account.receivingEmail}
                </p>
                {account.emailVerified && (
                  <Badge className="h-6.25 border-primary-400" color="primary">
                    확인됨
                  </Badge>
                )}
              </>
            )}
          </div>
          <button
            type="button"
            className={SMALL_OUTLINE_BUTTON_CLASS_NAME}
            disabled
            title="이메일 변경 기능 준비 중"
            aria-label="이메일 변경 기능 준비 중"
          >
            변경
          </button>
        </div>
      </AccountSettingsCard>

      <AccountSettingsCard title="세션">
        <div>
          <button
            type="button"
            className={SMALL_OUTLINE_BUTTON_CLASS_NAME}
            disabled={isLoggingOut}
            onClick={() => logout()}
          >
            {isLoggingOut ? '로그아웃 중…' : '로그아웃'}
          </button>
          {isLogoutError && (
            <p className="mt-2 text-label-small font-medium text-danger-500" role="alert">
              로그아웃에 실패했어요. 다시 시도해주세요.
            </p>
          )}
        </div>
      </AccountSettingsCard>

      <AccountSettingsCard title="계정 삭제">
        <Link
          to="/settings/account/withdraw"
          className="w-fit cursor-pointer text-label-medium font-medium text-text-primary underline decoration-from-font [text-underline-position:from-font]"
        >
          회원 탈퇴하기 →
        </Link>
      </AccountSettingsCard>
    </div>
  );
}
