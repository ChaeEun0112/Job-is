import { useState } from 'react';
import { useNavigate } from 'react-router';
import WithdrawalCompleteContent from '@/features/settings/components/WithdrawalCompleteContent';
import WithdrawalReasonForm from '@/features/settings/components/WithdrawalReasonForm';
import useAccountWithdrawal from '@/features/settings/hooks/useAccountWithdrawal';

type WithdrawalStep = 'reason' | 'complete';

export default function AccountWithdrawalPage() {
  const navigate = useNavigate();
  const [withdrawalStep, setWithdrawalStep] = useState<WithdrawalStep>('reason');
  const { withdrawAccount, isWithdrawing, isWithdrawalError } = useAccountWithdrawal(() => {
    setWithdrawalStep('complete');
  });

  return (
    <div
      className={
        withdrawalStep === 'complete'
          ? 'flex flex-1 items-center justify-center bg-gray-50 px-5 py-12'
          : 'flex flex-1 justify-center bg-gray-50 px-5 py-30'
      }
    >
      {withdrawalStep === 'reason' ? (
        <WithdrawalReasonForm
          onCancel={() => navigate('/settings/account')}
          onComplete={withdrawAccount}
          isSubmitting={isWithdrawing}
          errorMessage={
            isWithdrawalError ? '회원 탈퇴에 실패했어요. 다시 시도해주세요.' : undefined
          }
        />
      ) : (
        <WithdrawalCompleteContent onHome={() => navigate('/')} />
      )}
    </div>
  );
}
