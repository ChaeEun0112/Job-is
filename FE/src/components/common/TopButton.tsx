import { type ComponentPropsWithRef } from 'react';
import ChevronUpIcon from '@/assets/icons/icon-chevron-up.svg?react';
import { cn } from '@/utils/cn';

type TopButtonProps = ComponentPropsWithRef<'button'> & {
  label?: string;
};

export default function TopButton({ className, label, type = 'button', ...props }: TopButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        'inline-flex items-center gap-2 rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-semibold text-gray-900 shadow-sm transition hover:border-gray-300 hover:bg-gray-50',
        className,
      )}
      {...props}
    >
      <ChevronUpIcon className="size-4" />
      {label ?? '위로'}
    </button>
  );
}
