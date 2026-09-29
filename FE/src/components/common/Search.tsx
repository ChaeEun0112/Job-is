import { useId, type ComponentPropsWithRef, type FormEvent } from 'react';
import SearchIcon from '@/assets/icons/icon-search.svg?react';
import FormField from '@/components/common/FormField';
import { cn } from '@/utils/cn';

type SearchProps = Omit<ComponentPropsWithRef<'input'>, 'onSubmit'> & {
  label?: string;
  helperText?: string;
  onSearchSubmit?: (query: string) => void;
};

export default function Search({
  className,
  id,
  label,
  helperText,
  onSearchSubmit,
  placeholder = '선택해주세요.',
  name,
  disabled,
  ...props
}: SearchProps) {
  const generatedId = useId();
  const inputId = id ?? generatedId;
  const inputName = name ?? inputId;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!onSearchSubmit) return;
    const formData = new FormData(event.currentTarget);
    onSearchSubmit(String(formData.get(inputName) ?? ''));
  };

  return (
    <form
      onSubmit={handleSubmit}
      className={cn('flex shrink-0 flex-col items-start gap-2', className)}
    >
      <FormField id={inputId} label={label} helperText={helperText} className="w-full">
        <div className="flex h-14 w-full items-center gap-2 rounded-sm border border-gray-700 bg-white px-4">
          <input
            id={inputId}
            name={inputName}
            type="search"
            placeholder={placeholder}
            aria-describedby={helperText ? `${inputId}-helper-text` : undefined}
            disabled={disabled}
            className="h-full w-full flex-1 bg-transparent text-body-large font-medium text-gray-900 outline-none placeholder:text-gray-600 disabled:cursor-not-allowed disabled:text-gray-400"
            {...props}
          />
          <button
            type="submit"
            aria-label="검색"
            disabled={disabled}
            className="flex shrink-0 items-center justify-center text-gray-700 disabled:cursor-not-allowed disabled:text-gray-400"
          >
            <SearchIcon className="size-6" />
          </button>
        </div>
      </FormField>
    </form>
  );
}
