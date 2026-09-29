import ArrowRightIcon from '@/assets/icons/icon-arrow-right.svg?react';
import { Button } from '@/components/common';
import { cn } from '@/utils/cn';

interface JobDetailApplyPanelProps {
  onApply?: () => void;
  onIntendToApply?: () => void;
  onSave?: () => void;
  isSaved?: boolean;
  isSavePending?: boolean;
  onNotInterested?: () => void;
  isIntendedToApply?: boolean;
}

export default function JobDetailApplyPanel({
  onApply,
  onIntendToApply,
  onSave,
  isSaved,
  isSavePending,
  onNotInterested,
  isIntendedToApply,
}: JobDetailApplyPanelProps) {
  return (
    <div className="flex w-full flex-col gap-5 rounded-md border border-gray-400 bg-white p-6">
      <Button className="w-full rounded-sm" onClick={onApply} disabled={!onApply}>
        지원하기
        <ArrowRightIcon className="size-6" />
      </Button>
      <div className="flex w-full gap-5.5">
        <Button
          variant="outline"
          className={cn(
            'w-[110px] shrink-0 rounded-sm',
            isIntendedToApply && 'border-transparent bg-primary-400 hover:bg-primary-400',
          )}
          onClick={onIntendToApply}
        >
          지원 의향
        </Button>
        <Button
          variant="outline"
          className={cn(
            'w-[110px] shrink-0 rounded-sm',
            isSaved && 'border-transparent bg-primary-400 hover:bg-primary-400',
          )}
          onClick={onSave}
          disabled={!onSave || isSavePending}
        >
          {isSaved ? '저장됨' : '저장'}
        </Button>
        <Button
          className="w-[110px] shrink-0 rounded-sm bg-gray-400 text-text-tertiary hover:bg-gray-400 active:bg-gray-400"
          onClick={onNotInterested}
          disabled={!onNotInterested}
        >
          관심없음
        </Button>
      </div>
    </div>
  );
}
