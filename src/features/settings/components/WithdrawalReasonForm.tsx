import { useState } from 'react';
import type { WithdrawalRequest } from '@/api/types/auth.types';
import Button from '@/components/common/Button';
import RadioButton from '@/components/common/RadioButton';
import WithdrawalConfirmModal from '@/features/settings/components/WithdrawalConfirmModal';

const WITHDRAWAL_REASONS = [
  { label: '추천이 잘 안맞아요', code: 'RECOMMENDATION_MISMATCH' },
  { label: '메일이 너무 자주 와요', code: 'EMAIL_TOO_FREQUENT' },
  { label: '지금은 구직 중이 아니에요', code: 'NOT_JOB_SEEKING' },
  { label: '서비스를 잘 안 쓰게 돼요', code: 'SERVICE_NOT_USED' },
  { label: '기타', code: 'OTHER' },
] satisfies Array<{ label: string; code: NonNullable<WithdrawalRequest['reasonCode']> }>;

export default function WithdrawalReasonForm({
  onCancel,
  onComplete,
  isSubmitting,
  errorMessage,
}: {
  onCancel: () => void;
  onComplete: (request: WithdrawalRequest) => Promise<void>;
  isSubmitting: boolean;
  errorMessage?: string;
}) {
  const [selectedReason, setSelectedReason] = useState(WITHDRAWAL_REASONS[1]);
  const [customReason, setCustomReason] = useState('');
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);

  return (
    <section className="flex min-h-74 w-full max-w-186 flex-col gap-5 overflow-hidden rounded-md border border-gray-200 bg-white p-6">
      <h1 className="text-heading-medium text-text-primary">정말 떠나시나요?</h1>

      <fieldset className="flex flex-col gap-5">
        <legend className="sr-only">회원 탈퇴 사유</legend>
        {WITHDRAWAL_REASONS.map((reason, index) => {
          const reasonId = `withdrawal-reason-${index}`;

          return (
            <RadioButton
              key={reason.code}
              id={reasonId}
              name="withdrawal-reason"
              value={reason.code}
              checked={selectedReason.code === reason.code}
              onChange={() => setSelectedReason(reason)}
              label={reason.label}
              className="min-h-17.5 w-full cursor-pointer gap-3.25 rounded-xs border border-gray-400 bg-white p-6 text-label-medium font-medium text-text-primary hover:bg-gray-50"
            />
          );
        })}
      </fieldset>

      <label htmlFor="withdrawal-custom-reason" className="sr-only">
        탈퇴 사유 자유 입력
      </label>
      <input
        id="withdrawal-custom-reason"
        type="text"
        value={customReason}
        onChange={(event) => setCustomReason(event.target.value)}
        placeholder="자유 입력 ···"
        className="h-10 w-full rounded-[6px] border border-text-secondary bg-white px-4 text-label-medium font-medium text-text-primary placeholder:text-gray-600 focus:border-primary-500 focus:outline-none"
      />

      <div className="rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6 text-label-medium font-medium">
        <p className="text-text-tertiary">탈퇴하면 이렇게 돼요</p>
        <ul className="ml-5 list-disc text-text-primary">
          <li>매일 아침 브리핑과 모든 메일 발송이 멈춰요(최대 48시간 내 완전 반영).</li>
          <li>
            이미 예약된 1건이 나갈 수 있어요. 프로필 · 저장 목록 · 업로드 파일에 더는 접근할 수
            없어요.
          </li>
          <li>계정은 30일 후 완전히 삭제돼요. 그 전에 다시 로그인하면 복구할 수 있어요.</li>
        </ul>
      </div>

      <div className="flex gap-7.5">
        <Button
          variant="outline"
          className="w-25 shrink-0"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          취소
        </Button>
        <Button
          className="min-w-0 flex-1"
          onClick={() => setIsConfirmModalOpen(true)}
          disabled={isSubmitting}
        >
          계속
        </Button>
      </div>

      {isConfirmModalOpen && (
        <WithdrawalConfirmModal
          onClose={() => setIsConfirmModalOpen(false)}
          onConfirm={async () => {
            try {
              await onComplete({
                reasonCode: selectedReason.code,
                reasonDetail: customReason.trim() || undefined,
              });
            } catch {
              // 오류 메시지는 mutation 상태를 통해 모달에 표시합니다.
            }
          }}
          isSubmitting={isSubmitting}
          errorMessage={errorMessage}
        />
      )}
    </section>
  );
}
