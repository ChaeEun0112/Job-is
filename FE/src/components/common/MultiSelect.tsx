import ChevronDownIcon from '@/assets/icons/icon-chevron-down.svg?react';
import useDismissableOpen from '@/hooks/useDismissableOpen';
import { cn } from '@/utils/cn';

export interface MultiSelectOption {
  label: string;
  value: string;
}

interface MultiSelectProps {
  placeholder: string;
  options: MultiSelectOption[];
  selectedValues: string[];
  onToggle: (value: string) => void;
  className?: string;
}

export default function MultiSelect({
  placeholder,
  options,
  selectedValues,
  onToggle,
  className,
}: MultiSelectProps) {
  const { isOpen, setIsOpen, containerRef } = useDismissableOpen();

  return (
    <div ref={containerRef} className={cn('relative', className)}>
      <button
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        className="flex h-14 w-full cursor-pointer items-center justify-between gap-2 rounded-sm border border-gray-700 bg-white px-4 text-body-large font-medium text-gray-600"
      >
        {placeholder}
        <ChevronDownIcon className="size-6 shrink-0 text-black" />
      </button>
      {isOpen && (
        <div
          role="listbox"
          aria-multiselectable
          aria-label={placeholder}
          className="absolute top-full left-0 z-20 mt-1 flex max-h-70 w-max min-w-full flex-col overflow-y-auto rounded-sm border border-gray-200 bg-white shadow-md"
        >
          {options.map((option) => {
            const isSelected = selectedValues.includes(option.value);

            return (
              <button
                key={option.value}
                type="button"
                role="option"
                aria-selected={isSelected}
                onClick={() => onToggle(option.value)}
                className={cn(
                  'flex w-full cursor-pointer items-center px-4 py-3 text-left text-body-large font-medium whitespace-nowrap transition-colors',
                  isSelected ? 'bg-primary-600 text-white' : 'text-text-primary hover:bg-gray-50',
                )}
              >
                {option.label}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
