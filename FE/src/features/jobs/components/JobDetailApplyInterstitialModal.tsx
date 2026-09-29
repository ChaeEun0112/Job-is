import { useState } from 'react';
import { createPortal } from 'react-dom';
import ArrowRightIcon from '@/assets/icons/icon-arrow-right.svg?react';
import { Button, ModalCheckbox } from '@/components/common';

interface JobDetailApplyInterstitialModalProps {
  sourceName: string;
  onClose: () => void;
  onConfirm: (intendToApply: boolean) => void;
}

export default function JobDetailApplyInterstitialModal({
  sourceName,
  onClose,
  onConfirm,
}: JobDetailApplyInterstitialModalProps) {
  const [intendToApply, setIntendToApply] = useState(false);

  return createPortal(
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black-alpha-40 px-4"
      onClick={onClose}
    >
      <div onClick={(event) => event.stopPropagation()}>
        <ModalCheckbox
          title={`원문(${sourceName})에서 지원해요`}
          description={'Job.is 웹은 원문 페이지로 이동해요.\n지원 진행 결과는 원문에서 확인하세요.'}
          checkboxLabel="지원 의향"
          checked={intendToApply}
          onChange={(event) => setIntendToApply(event.target.checked)}
          onClose={onClose}
          className="w-[760px]"
          footer={
            <>
              <Button variant="outline" onClick={onClose}>
                취소
              </Button>
              <Button onClick={() => onConfirm(intendToApply)}>
                {sourceName}에서 보기
                <ArrowRightIcon className="size-5" />
              </Button>
            </>
          }
        />
      </div>
    </div>,
    document.body,
  );
}
