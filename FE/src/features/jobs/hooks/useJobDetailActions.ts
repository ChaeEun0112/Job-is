import { useRef, useState } from 'react';
import { recordJobApply, toggleApplyIntent } from '@/api/jobs';
import { dismissCard, submitDismissReason } from '@/api/recommendations';
import { showAlert } from '@/components/feedback';
import { useSaveJob } from '@/features/jobs/hooks/useSaveJob';

const DISMISS_REASON_MAX_LENGTH = 30;

function buildDismissReason(reasons: string[]): string | undefined {
  let combined = '';
  for (const reason of reasons) {
    const next = combined ? `${combined},${reason}` : reason;
    if (next.length > DISMISS_REASON_MAX_LENGTH) break;
    combined = next;
  }
  return combined || undefined;
}

interface UseJobDetailActionsOptions {
  jobId: number | null;
  deckId?: number;
  cardId?: number;
}

export default function useJobDetailActions({ jobId, deckId, cardId }: UseJobDetailActionsOptions) {
  const [isSkipModalOpen, setIsSkipModalOpen] = useState(false);
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false);
  const [intendedToApply, setIntendedToApply] = useState(false);
  const [isSaved, setIsSaved] = useState(false);
  const [isSavePending, setIsSavePending] = useState(false);
  const dismissedCardKeyRef = useRef<string | undefined>(undefined);
  const isSkipPendingRef = useRef(false);
  const { save, unsave } = useSaveJob();

  const canSubmitDismissReason = deckId !== undefined && cardId !== undefined;

  function handleConfirmApply(intendToApply: boolean, sourceUrl: string) {
    if (jobId === null) return;
    setIsApplyModalOpen(false);

    let isValidSourceUrl = false;
    try {
      isValidSourceUrl = new URL(sourceUrl).protocol === 'https:';
    } catch {
      isValidSourceUrl = false;
    }

    if (!isValidSourceUrl) {
      console.error(`유효하지 않은 원문 링크입니다: ${sourceUrl}`);
      return;
    }

    setIntendedToApply(intendToApply);
    window.open(sourceUrl, '_blank', 'noopener,noreferrer');
    recordJobApply(jobId, intendToApply).catch(console.error);
  }

  function handleIntendToApply() {
    if (jobId === null) return;
    const previous = intendedToApply;
    setIntendedToApply((prev) => !prev);
    toggleApplyIntent(jobId)
      .then((result) => setIntendedToApply(result.applyIntent))
      .catch((error) => {
        setIntendedToApply(previous);
        console.error(error);
      });
  }

  async function handleSave() {
    if (jobId === null || isSavePending) return;

    setIsSavePending(true);
    try {
      if (isSaved) {
        await unsave(jobId);
        setIsSaved(false);
        showAlert('success', '공고 저장을 해제했어요.');
        return;
      }

      await save(jobId);
      setIsSaved(true);
      showAlert('success', '공고를 저장했어요.');
    } catch {
      showAlert(
        'danger',
        isSaved
          ? '공고 저장을 해제하지 못했어요. 다시 시도해주세요.'
          : '공고를 저장하지 못했어요. 다시 시도해주세요.',
      );
    } finally {
      setIsSavePending(false);
    }
  }

  async function handleSkipSubmit(reasons: string[], note: string) {
    if (!canSubmitDismissReason) {
      showAlert('danger', '오늘의 추천에 포함된 공고만 관심 없음으로 표시할 수 있어요.');
      return;
    }

    if (isSkipPendingRef.current) return;

    const dismissCardKey = `${deckId}-${cardId}`;
    isSkipPendingRef.current = true;

    try {
      if (dismissedCardKeyRef.current !== dismissCardKey) {
        await dismissCard(deckId, cardId);
        dismissedCardKeyRef.current = dismissCardKey;
      }

      await submitDismissReason(deckId, cardId, {
        reason: buildDismissReason(reasons),
        comment: note || undefined,
      });
      setIsSkipModalOpen(false);
      showAlert('success', '관심 없음으로 표시했어요.');
    } catch {
      showAlert(
        dismissedCardKeyRef.current === dismissCardKey ? 'warning' : 'danger',
        dismissedCardKeyRef.current === dismissCardKey
          ? '관심 없음으로 표시했어요. 사유 저장에 실패했어요. 다시 시도해주세요.'
          : '관심 없음 처리하지 못했어요. 다시 시도해주세요.',
      );
    } finally {
      isSkipPendingRef.current = false;
    }
  }

  return {
    isSkipModalOpen,
    setIsSkipModalOpen,
    isApplyModalOpen,
    setIsApplyModalOpen,
    intendedToApply,
    isSaved,
    isSavePending,
    handleConfirmApply,
    handleIntendToApply,
    handleSave,
    handleSkipSubmit,
  };
}
