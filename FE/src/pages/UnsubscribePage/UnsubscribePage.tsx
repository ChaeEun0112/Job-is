import { useState } from 'react';
import { Link } from 'react-router';
import CheckCircleIcon from '@/assets/icons/icon-check-circle.svg?react';
import Button from '@/components/common/Button';
import RadioButton from '@/components/common/RadioButton';

const UNSUBSCRIBE_REASONS = [
  '너무 자주 와요',
  '추천이 안맞아요',
  '지금은 구직 중이 아니에요',
  '기타',
];

export default function UnsubscribePage() {
  const [selectedReason, setSelectedReason] = useState<string>();

  return (
    <div className="flex flex-1 justify-center bg-gray-50 px-3 py-30">
      <div className="flex w-full max-w-190 flex-col items-center gap-5 rounded-md border border-gray-200 bg-white p-6">
        <div className="flex items-center gap-2.5">
          <CheckCircleIcon className="size-6 text-primary-400" />
          <p className="text-heading-medium font-semibold text-text-primary">수신을 해지했어요</p>
        </div>

        <p className="text-center text-label-medium font-medium text-text-secondary">
          이제 데일리 브리핑 메일을 보내지 않아요.
          <br />
          회원 자격과 웹 추천은 그대로 유지돼요.
        </p>

        <Button className="w-full max-w-169">다시 받을게요 (재구독)</Button>

        <hr className="w-full border-t border-gray-200" />

        <div className="flex w-full max-w-169 flex-col items-start gap-2.5">
          <p className="text-label-medium font-semibold text-text-primary">
            잠깐 쉬고 싶었던 거라면?
          </p>
          <ul className="ml-5 list-disc space-y-1 text-label-medium font-medium text-text-secondary">
            <li>
              일시정지(스누즈)로 바꾸기 →{' '}
              <Link
                to="/settings/notifications"
                className="underline decoration-solid decoration-from-font [text-underline-position:from-font]"
              >
                수신 설정
              </Link>
            </li>
            <li>
              받는 시간·요일 바꾸기 →{' '}
              <Link
                to="/settings/notifications"
                className="underline decoration-solid decoration-from-font [text-underline-position:from-font]"
              >
                수신 설정
              </Link>
            </li>
          </ul>
        </div>

        <hr className="w-full border-t border-gray-200" />

        <p className="text-center text-heading-small font-semibold text-text-primary">
          괜찮으시면 이유를 알려주세요 (선택)
        </p>

        <div className="flex w-full max-w-169 flex-col gap-2.5">
          {UNSUBSCRIBE_REASONS.map((reason, index) => (
            <div
              key={reason}
              className="flex w-full items-center rounded-xs border border-gray-400 p-6"
            >
              <RadioButton
                id={`unsubscribe-reason-${index}`}
                name="unsubscribe-reason"
                label={reason}
                checked={selectedReason === reason}
                onChange={() => setSelectedReason(reason)}
              />
            </div>
          ))}
        </div>

        <Button variant="outline" className="w-full max-w-169">
          의견 보내기
        </Button>

        <button
          type="button"
          className="text-label-medium font-medium text-text-tertiary underline decoration-solid decoration-from-font [text-underline-position:from-font]"
        >
          건너뛰기
        </button>
      </div>
    </div>
  );
}
