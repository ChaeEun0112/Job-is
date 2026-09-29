import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

type ChipProps = ComponentPropsWithRef<'button'> & {
  label: string;
  selected?: boolean;
};

export default function Chip({
  className,
  label,
  selected = false,
  disabled,
  type = 'button',
  ...props
}: ChipProps) {
  return (
    <button
      type={type}
      disabled={disabled}
      aria-pressed={selected}
      className={cn(
        'inline-flex items-center rounded-2xl border px-4 py-2 text-sm font-semibold transition',
        disabled
          ? 'cursor-not-allowed border-gray-200 bg-gray-100 text-gray-400'
          : selected
            ? 'border-transparent bg-primary-600 text-text-primary shadow-sm'
            : 'border-gray-200 bg-white text-gray-900 hover:border-gray-300 hover:bg-gray-50',
        className,
      )}
      {...props}
    >
      {label}
    </button>
  );
}
