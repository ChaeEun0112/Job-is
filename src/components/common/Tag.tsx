import { type ComponentPropsWithRef } from 'react';
import CloseIcon from '@/assets/icons/icon-close.svg?react';
import HashIcon from '@/assets/icons/icon-hash.svg?react';
import PlusIcon from '@/assets/icons/icon-plus.svg?react';
import { cn } from '@/utils/cn';

type TagVariant = 'removable' | 'add' | 'hash' | 'plain' | 'select';

type TagProps = ComponentPropsWithRef<'button'> & {
  variant?: TagVariant;
  label: string;
  selected?: boolean;
};

const TAG_CLASS_NAME =
  'inline-flex h-10 items-center justify-center gap-0.5 rounded-full border border-gray-200 bg-white px-3 text-base leading-6 font-normal text-gray-900';

export default function Tag({
  className,
  variant = 'removable',
  label,
  selected = false,
  type = 'button',
  'aria-pressed': ariaPressed,
  ...props
}: TagProps) {
  const content = (
    <>
      {variant === 'add' && <PlusIcon className="size-4" />}
      {variant === 'hash' && <HashIcon className="size-4" />}
      {label}
      {variant === 'removable' && <CloseIcon className="size-4" />}
    </>
  );

  if (variant === 'plain') {
    return <span className={cn(TAG_CLASS_NAME, className)}>{content}</span>;
  }

  return (
    <button
      type={type}
      className={cn(
        TAG_CLASS_NAME,
        'cursor-pointer transition-colors duration-150 active:scale-95',
        variant === 'select' && selected && 'border-transparent bg-primary-600 text-text-primary',
        className,
      )}
      {...props}
      aria-pressed={ariaPressed ?? (variant === 'select' ? selected : undefined)}
    >
      {content}
    </button>
  );
}
