import { type ComponentPropsWithRef } from 'react';
import MailIcon from '@/assets/icons/icon-mail.svg?react';
import { Button } from '@/components/common';
import { cn } from '@/utils/cn';

interface RecommendationPendingContentProps extends ComponentPropsWithRef<'section'> {
  onExploreClick?: () => void;
  onCompleteProfileClick?: () => void;
}

export default function RecommendationPendingContent({
  className,
  ref,
  onExploreClick,
  onCompleteProfileClick,
  ...props
}: RecommendationPendingContentProps) {
  return (
    <section
      ref={ref}
      className={cn(
        'flex min-h-0 w-full flex-1 items-center justify-center bg-gray-50 px-3 py-6',
        className,
      )}
      {...props}
    >
      <div className="recommendation-pending-enter flex w-full max-w-190 flex-col items-center gap-5 rounded-md border border-gray-200 bg-white p-6">
        <div className="flex w-full max-w-103.75 items-center justify-center gap-2.5 rounded-xs">
          <MailIcon aria-hidden="true" className="size-6" />
          <p className="text-center text-heading-medium font-semibold text-text-primary">
            편지를 준비하고 있어요
          </p>
        </div>

        <p className="text-center text-label-medium font-medium text-text-tertiary">
          내일 아침 <span className="text-text-secondary">07:30</span>, 메일과 함께 웹으로
          보내드려요.
        </p>

        <div className="flex w-full max-w-103.75 items-center justify-center rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6">
          <p className="text-label-medium font-medium text-text-tertiary">
            메일함에서 확인 링크를 눌러주세요. <span className="text-text-secondary">72시간</span>{' '}
            이내 유효해요.
          </p>
        </div>

        <div className="flex items-center justify-center gap-2.5">
          <Button variant="outline" className="w-37.5" onClick={onExploreClick}>
            공고 둘러보기
          </Button>
          <Button variant="outline" className="w-37.5" onClick={onCompleteProfileClick}>
            프로필 보완
          </Button>
        </div>

        <p className="text-center text-label-large font-medium text-text-tertiary">
          프로필을 보완하고 더 나에게 맞는 추천을 받아보세요.
        </p>
      </div>
    </section>
  );
}
