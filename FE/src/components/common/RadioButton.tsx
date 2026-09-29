import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

type RadioButtonProps = Omit<ComponentPropsWithRef<'input'>, 'type'> & {
  label?: string;
};

export default function RadioButton({
  className,
  id,
  label,
  disabled,
  ...props
}: RadioButtonProps) {
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
        <input id={id} type="radio" disabled={disabled} className="peer sr-only" {...props} />
        <span
          className={cn(
            'absolute inset-0 rounded-full border border-gray-400 bg-white',
            'peer-checked:border-primary-400',
            'peer-disabled:border-gray-400 peer-disabled:bg-gray-200',
            'peer-focus-visible:outline peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-primary-500',
          )}
        />
        <span
          className={cn(
            'pointer-events-none absolute inset-0 m-auto size-2.5 rounded-full bg-primary-400 opacity-0',
            'peer-checked:opacity-100 peer-disabled:bg-gray-400',
          )}
        />
      </span>
      {label}
    </label>
  );
}
