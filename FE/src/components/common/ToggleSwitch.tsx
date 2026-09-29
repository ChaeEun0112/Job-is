import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

type ToggleSwitchProps = Omit<ComponentPropsWithRef<'input'>, 'type'> & {
  label?: string;
};

export default function ToggleSwitch({
  className,
  id,
  disabled,
  label,
  ...props
}: ToggleSwitchProps) {
  return (
    <label
      htmlFor={id}
      className={cn(
        'inline-flex items-center gap-2 text-label-medium font-medium text-gray-900',
        disabled && 'text-gray-400',
        className,
      )}
    >
      <span
        className={cn(
          'relative inline-flex h-6 w-10 shrink-0 items-center rounded-full bg-gray-200 p-0.5 transition-colors',
          'has-checked:bg-primary-400',
          'has-disabled:bg-gray-200',
          'has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-primary-500',
        )}
      >
        <input id={id} type="checkbox" disabled={disabled} className="peer sr-only" {...props} />
        <span
          className={cn(
            'pointer-events-none size-5 rounded-full bg-white transition-transform',
            'peer-checked:translate-x-4',
            'peer-disabled:bg-gray-400',
          )}
        />
      </span>
      {label}
    </label>
  );
}
