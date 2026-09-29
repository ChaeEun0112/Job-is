import { type ReactNode } from 'react';
import { cn } from '@/utils/cn';

type FormFieldProps = {
  id: string;
  label?: string;
  helperText?: string;
  children: ReactNode;
  className?: string;
};

export default function FormField({ id, label, helperText, children, className }: FormFieldProps) {
  return (
    <div className={cn('flex flex-col gap-2', className)}>
      {label && (
        <label htmlFor={id} className="text-label-medium font-medium text-gray-900">
          {label}
        </label>
      )}
      {helperText && (
        <span id={`${id}-helper-text`} className="text-label-small text-gray-500">
          {helperText}
        </span>
      )}
      {children}
    </div>
  );
}
