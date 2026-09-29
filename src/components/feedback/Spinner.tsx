import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

type SpinnerProps = ComponentPropsWithRef<'div'>;

export default function Spinner({ className, ref, ...props }: SpinnerProps) {
  return (
    <div
      ref={ref}
      role="status"
      aria-label="로딩 중"
      className={cn(
        'size-12 shrink-0 animate-spin rounded-full border-4 border-gray-200 border-t-primary-400',
        className,
      )}
      {...props}
    />
  );
}
