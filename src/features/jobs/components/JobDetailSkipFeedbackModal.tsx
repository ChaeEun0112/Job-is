import { useState } from 'react';
import { createPortal } from 'react-dom';
import { Button, Modal, TextInput } from '@/components/common';
import Chip from '@/components/common/Chip';

const SKIP_REASONS = ['직무 불일치', '지역', '이미 지원함'];

interface JobDetailSkipFeedbackModalProps {
  onClose: () => void;
  onSubmit: (reasons: string[], note: string) => void;
}

export default function JobDetailSkipFeedbackModal({
  onClose,
  onSubmit,
}: JobDetailSkipFeedbackModalProps) {
  const [selectedReasons, setSelectedReasons] = useState<string[]>([]);
  const [note, setNote] = useState('');

  function toggleReason(reason: string) {
    setSelectedReasons((prev) =>
      prev.includes(reason) ? prev.filter((item) => item !== reason) : [...prev, reason],
    );
  }

  return createPortal(
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black-alpha-40 px-4"
      onClick={onClose}
    >
      <Modal
        title="왜 관심이 없으신가요?"
        onClose={onClose}
        className="w-[760px]"
        onClick={(event) => event.stopPropagation()}
        footer={
          <>
            <Button variant="outline" onClick={onClose}>
              닫기
            </Button>
            <Button onClick={() => onSubmit(selectedReasons, note)}>사유보내기</Button>
          </>
        }
      >
        <p className="text-body-medium leading-[1.5] font-medium text-text-secondary">
          맞는 추천을 위해 참고할게요 · 사유는 선택이에요
        </p>
        <div className="flex w-full flex-wrap gap-2">
          {SKIP_REASONS.map((reason) => (
            <Chip
              key={reason}
              label={reason}
              selected={selectedReasons.includes(reason)}
              onClick={() => toggleReason(reason)}
            />
          ))}
        </div>
        <TextInput
          label="코멘트 (선택)"
          placeholder="내용을 입력하세요"
          value={note}
          onChange={(event) => setNote(event.target.value)}
          className="h-12 w-[360px] rounded-[6px]"
        />
      </Modal>
    </div>,
    document.body,
  );
}
