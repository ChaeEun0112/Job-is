import { useId, type ComponentPropsWithRef } from 'react';
import FormField from '@/components/common/FormField';
import { cn } from '@/utils/cn';

type TextInputProps = ComponentPropsWithRef<'input'> & {
  label?: string;
  helperText?: string;
};

export default function TextInput({ className, id, label, helperText, ...props }: TextInputProps) {
  const generatedId = useId();
  const inputId = id ?? generatedId;

  return (
    <FormField id={inputId} label={label} helperText={helperText}>
      <input
        id={inputId}
        aria-describedby={helperText ? `${inputId}-helper-text` : undefined}
        className={cn(
          'h-14 w-full rounded-sm border border-gray-700 bg-white px-4 text-body-large font-medium text-gray-900 placeholder:text-gray-600 focus:border-primary-500 focus:outline-none disabled:bg-gray-200 disabled:text-gray-600',
          className,
        )}
        {...props}
      />
    </FormField>
  );
}
