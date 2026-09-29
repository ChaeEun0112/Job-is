import AlertTriangleIcon from '@/assets/icons/icon-alert-triangle.svg?react';
import Button from '@/components/common/Button';

interface LoginFailureContentProps {
  message: string;
  onRetry: () => void;
}

export default function LoginFailureContent({ message, onRetry }: LoginFailureContentProps) {
  return (
    <>
      <div className="flex items-center gap-2.5 p-2.5">
        <AlertTriangleIcon className="size-6 shrink-0 text-warning-500" />
        <p className="text-heading-medium font-semibold text-text-primary">
          로그인을 완료하지 못했어요.
        </p>
      </div>
      <p className="text-label-medium font-medium text-text-tertiary">{message}</p>
      <Button className="w-full max-w-104" onClick={onRetry}>
        다시 시도
      </Button>
    </>
  );
}
