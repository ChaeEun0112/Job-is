import { type ComponentPropsWithRef } from 'react';
import largeLeftArrow from '@/assets/icons/icon-carousel-arrow-large-left.svg';
import largeRightArrow from '@/assets/icons/icon-carousel-arrow-large-right.svg';
import mediumLeftArrow from '@/assets/icons/icon-carousel-arrow-medium-left.svg';
import mediumRightArrow from '@/assets/icons/icon-carousel-arrow-medium-right.svg';
import smallLeftArrow from '@/assets/icons/icon-carousel-arrow-small-left.svg';
import smallRightArrow from '@/assets/icons/icon-carousel-arrow-small-right.svg';
import { cn } from '@/utils/cn';

interface CarouselArrowProps extends ComponentPropsWithRef<'button'> {
  direction: 'left' | 'right';
  size?: 'large' | 'medium' | 'small';
}

const sizeClasses = {
  large: { button: 'size-16', icon: 'size-[50.4px]' },
  medium: { button: 'size-12', icon: 'size-[37.8px]' },
  small: { button: 'size-10', icon: 'size-[19.5px]' },
} as const;

const arrowIcons = {
  large: { left: largeLeftArrow, right: largeRightArrow },
  medium: { left: mediumLeftArrow, right: mediumRightArrow },
  small: { left: smallLeftArrow, right: smallRightArrow },
} as const;

export default function CarouselArrow({
  className,
  ref,
  direction,
  size = 'large',
  type = 'button',
  'aria-label': ariaLabel,
  ...props
}: CarouselArrowProps) {
  const { button, icon } = sizeClasses[size];

  return (
    <button
      ref={ref}
      type={type}
      aria-label={ariaLabel ?? (direction === 'left' ? '이전' : '다음')}
      className={cn(
        'flex items-center justify-center rounded-full transition-transform duration-150 motion-reduce:transition-none enabled:hover:scale-[1.02] enabled:focus-visible:ring-2 enabled:focus-visible:ring-primary-300 enabled:focus-visible:ring-offset-2 enabled:focus-visible:outline-none enabled:active:scale-[0.97] disabled:cursor-not-allowed',
        button,
        className,
      )}
      {...props}
    >
      <img alt="" aria-hidden="true" src={arrowIcons[size][direction]} className={icon} />
    </button>
  );
}
