import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

type CarouselIndicatorProps = ComponentPropsWithRef<'div'> &
  (
    | { variant: 'number'; current: number; total: number }
    | { variant: 'dot'; total: number; activeIndex: number }
  );

export default function CarouselIndicator({ className, ref, ...rest }: CarouselIndicatorProps) {
  if (rest.variant === 'number') {
    const { variant, current, total, ...domProps } = rest;
    return (
      <div
        ref={ref}
        className={cn(
          'inline-flex h-10 items-center gap-1 rounded-full border border-gray-200 bg-white px-4 py-3.5 text-[18px] leading-normal font-bold whitespace-nowrap',
          className,
        )}
        {...domProps}
      >
        <span className="text-text-secondary">{current}</span>
        <span className="text-text-primary">/</span>
        <span className="text-text-primary">{total}</span>
      </div>
    );
  }

  const { variant, total, activeIndex, ...domProps } = rest;
  return (
    <div
      ref={ref}
      className={cn('inline-flex h-10 items-center gap-1 rounded-full bg-white p-4', className)}
      {...domProps}
    >
      {Array.from({ length: total }, (_, index) => (
        <span
          key={index}
          className={cn(
            'h-2 rounded-full transition-[width,background-color] duration-200 ease-out',
            index === activeIndex ? 'w-5 bg-primary-400' : 'w-2 bg-gray-300',
          )}
        />
      ))}
    </div>
  );
}
