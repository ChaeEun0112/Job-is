import { type ComponentPropsWithRef } from 'react';
import CheckIcon from '@/assets/icons/icon-check.svg?react';
import { cn } from '@/utils/cn';

type CheckboxProps = Omit<ComponentPropsWithRef<'input'>, 'type'> & {
  label?: string;
};

export default function Checkbox({ className, id, label, disabled, ...props }: CheckboxProps) {
  return (
    <label
      htmlFor={id}
      className={cn(
        'inline-flex items-center gap-2 text-label-medium font-medium text-gray-900',
        disabled && 'text-gray-400',
        className,
      )}
    >
      <span className="relative inline-flex size-6 shrink-0">
        <input id={id} type="checkbox" disabled={disabled} className="peer sr-only" {...props} />
        <span
          className={cn(
            'absolute inset-0 rounded-xs border border-gray-300 bg-white transition-[background-color,border-color] duration-150 ease-out motion-reduce:transition-none',
            'peer-checked:border-primary-400 peer-checked:bg-primary-400',
            'peer-disabled:border-gray-300 peer-disabled:bg-gray-200',
            'peer-disabled:peer-checked:border-gray-300 peer-disabled:peer-checked:bg-gray-200',
            'peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-primary-500',
          )}
        />
        <CheckIcon
          className={cn(
            'pointer-events-none absolute inset-0 m-auto size-4 scale-75 text-text-primary opacity-0 transition-[opacity,transform] duration-150 ease-out motion-reduce:transition-none',
            'peer-checked:scale-100 peer-checked:opacity-100 peer-disabled:text-gray-400',
          )}
        />
      </span>
      {label}
    </label>
  );
}
