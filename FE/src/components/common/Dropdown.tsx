import ChevronDownIcon from '@/assets/icons/icon-chevron-down.svg?react';
import useDismissableOpen from '@/hooks/useDismissableOpen';
import { cn } from '@/utils/cn';

export interface DropdownOption {
  label: string;
  value: string;
}

interface DropdownProps {
  placeholder: string;
  options: DropdownOption[];
  value: string;
  onChange: (value: string) => void;
  className?: string;
  size?: 'md' | 'sm';
}

export default function Dropdown({
  placeholder,
  options,
  value,
  onChange,
  className,
  size = 'md',
}: DropdownProps) {
  const { isOpen, setIsOpen, containerRef } = useDismissableOpen();

  const selectedLabel = options.find((option) => option.value === value)?.label;

  return (
    <div ref={containerRef} className={cn('relative', className)}>
      <button
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        className={cn(
          'flex w-full cursor-pointer items-center justify-between gap-2 rounded-sm border border-gray-700 bg-white font-medium whitespace-nowrap transition-[background-color,border-color] duration-150 ease-out hover:border-gray-900 hover:bg-gray-50 focus-visible:border-primary-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-400',
          size === 'sm' ? 'h-10 px-4 text-body-small' : 'h-14 px-4 text-body-large',
          selectedLabel ? 'text-gray-900' : 'text-gray-600',
        )}
      >
        {selectedLabel ?? placeholder}
        <ChevronDownIcon
          className={cn(
            'shrink-0 text-black transition-transform duration-150 ease-out',
            isOpen && 'rotate-180',
            size === 'sm' ? 'size-4' : 'size-6',
          )}
        />
      </button>
      {isOpen && (
        <div
          role="listbox"
          aria-label={placeholder}
          className="absolute top-full left-0 z-20 mt-1 flex max-h-70 w-max min-w-full origin-top overflow-y-auto rounded-sm border border-gray-200 bg-white shadow-md motion-safe:animate-[dropdown-menu-enter_180ms_cubic-bezier(0.16,1,0.3,1)_both]"
        >
          {options.map((option) => {
            const isSelected = option.value === value;

            return (
              <button
                key={option.value}
                type="button"
                role="option"
                aria-selected={isSelected}
                onClick={() => {
                  onChange(option.value);
                  setIsOpen(false);
                }}
                className={cn(
                  'flex w-full cursor-pointer items-center px-4 py-3 text-left font-medium whitespace-nowrap transition-colors duration-150 ease-out',
                  size === 'sm' ? 'text-body-small' : 'text-body-large',
                  isSelected
                    ? 'bg-primary-600 text-white hover:bg-primary-700'
                    : 'text-text-primary hover:bg-gray-100',
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
