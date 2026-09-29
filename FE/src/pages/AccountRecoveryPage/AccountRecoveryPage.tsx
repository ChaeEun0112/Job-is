import { useLocation, useNavigate } from 'react-router';
import Button from '@/components/common/Button';
import ResultIcon from '@/components/feedback/ResultIcon';
import useAccountRecovery from '@/features/login/hooks/useAccountRecovery';

function formatRecoveryDeadline(value: string | null) {
  if (!value) return '확인 필요';

  const recoveryDeadline = new Date(value);
  if (Number.isNaN(recoveryDeadline.getTime())) return '확인 필요';

  return new Intl.DateTimeFormat('ko-KR', {
    month: 'long',
    day: 'numeric',
  }).format(recoveryDeadline);
}

function getRemainingDays(value: string | null) {
  if (!value) return null;

  const recoveryDeadline = new Date(value);
  if (Number.isNaN(recoveryDeadline.getTime())) return null;

  return Math.max(0, Math.ceil((recoveryDeadline.getTime() - Date.now()) / 86_400_000));
}

interface AccountRecoveryLocationState {
  restoreCode?: string;
  restorableUntil?: string | null;
}

export default function AccountRecoveryPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const recoveryState = location.state as AccountRecoveryLocationState | null;
  const restoreCode = recoveryState?.restoreCode ?? null;
  const restorableUntil = recoveryState?.restorableUntil ?? null;
  const recoveryDeadline = formatRecoveryDeadline(restorableUntil);
  const remainingDays = getRemainingDays(restorableUntil);
  const { restoreAccount, isRestoring, isRestoreError } = useAccountRecovery();

  const handleRestore = () => {
    if (!restoreCode) return;

    restoreAccount({ restoreCode });
  };

  return (
    <div className="flex flex-1 items-center justify-center bg-gray-50 px-5 py-12">
      <section className="flex min-h-74 w-full max-w-190 flex-col items-center gap-5 overflow-hidden rounded-md border border-gray-200 bg-white p-6">
        <ResultIcon variant="loading" />

        <h1 className="text-heading-medium font-semibold text-text-primary">다시 오셨네요.</h1>

        <p className="text-center text-label-medium font-medium whitespace-pre-line text-text-secondary">
          {
            '탈퇴를 신청한 계정이에요.\n지금 복구하면 프로필과 저장 목록을 그대로 이어서 쓸 수 있어요.'
          }
        </p>

        <div className="flex min-h-17.5 w-full max-w-103.75 items-center justify-center rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6 text-label-medium font-medium text-text-tertiary">
          {remainingDays === null
            ? '복구 가능 기간을 확인해 주세요.'
            : `남은 복구 기간 · ${remainingDays}일(${recoveryDeadline}까지)`}
        </div>

        {isRestoreError ? (
          <p className="text-label-small font-medium text-danger-500" role="alert">
            계정을 복구하지 못했어요. 다시 시도해주세요.
          </p>
        ) : null}

        <Button
          className="w-full max-w-103.75"
          onClick={handleRestore}
          disabled={!restoreCode || isRestoring}
        >
          {isRestoring ? '복구 중…' : '계정 복구하기'}
        </Button>
        <Button variant="outline" className="w-full max-w-103.75" onClick={() => navigate('/')}>
          복구하지 않고 나가기
        </Button>
      </section>
    </div>
  );
}
