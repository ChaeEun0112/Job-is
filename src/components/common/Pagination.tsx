import { type ComponentPropsWithoutRef } from 'react';
import ArrowLeftIcon from '@/assets/icons/icon-chevron-left-circle.svg?react';
import ArrowRightIcon from '@/assets/icons/icon-chevron-right-circle.svg?react';
import { cn } from '@/utils/cn';

type PaginationProps = ComponentPropsWithoutRef<'nav'> & {
  currentPage: number;
  totalPages: number;
  onPrevious: () => void;
  onNext: () => void;
  label?: string;
};

export default function Pagination({
  className,
  currentPage,
  totalPages,
  onPrevious,
  onNext,
  label,
  ...props
}: PaginationProps) {
  return (
    <nav
      aria-label="페이지 네비게이션"
      className={cn('flex items-center justify-center gap-2', className)}
      {...props}
    >
      <button
        type="button"
        onClick={onPrevious}
        disabled={currentPage <= 1}
        className="flex h-10 w-27.5 min-w-0 cursor-pointer items-center justify-center gap-1 rounded-sm py-0 pr-2 pl-1 text-body-medium font-normal text-text-primary transition-colors hover:bg-gray-100 active:bg-gray-200 disabled:cursor-not-allowed disabled:text-text-disabled disabled:opacity-100 disabled:hover:bg-transparent"
      >
        <ArrowLeftIcon className="size-5" /> 이전
      </button>

      <span className="flex w-27.5 min-w-0 items-center justify-center text-body-medium font-medium text-text-primary">
        {label ?? `오늘 도착 · ${currentPage}/${totalPages}`}
      </span>

      <button
        type="button"
        onClick={onNext}
        disabled={currentPage >= totalPages}
        className="flex h-10 w-27.5 min-w-0 cursor-pointer items-center justify-center gap-1 rounded-sm py-0 pr-2 pl-1 text-body-medium font-normal text-text-primary transition-colors hover:bg-gray-100 active:bg-gray-200 disabled:cursor-not-allowed disabled:text-text-disabled disabled:opacity-100 disabled:hover:bg-transparent"
      >
        다음 <ArrowRightIcon className="size-5" />
      </button>
    </nav>
  );
}
