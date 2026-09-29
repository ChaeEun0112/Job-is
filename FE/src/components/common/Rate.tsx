import StarIcon from '@/assets/icons/icon-star.svg?react';
import { cn } from '@/utils/cn';

type RateProps = {
  value: number;
  className?: string;
};

export default function Rate({ value, className }: RateProps) {
  return (
    <span className={cn('inline-flex items-center gap-1', className)}>
      <StarIcon className="size-4 shrink-0" />
      <span className="flex h-5 w-[53px] flex-col items-start justify-center text-label-xsmall font-medium leading-[15px] text-gray-900">
        {value.toFixed(1)}
      </span>
    </span>
  );
}
