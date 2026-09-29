import { cva, type VariantProps } from 'class-variance-authority';
import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

const badgeVariants = cva(
  'inline-flex h-5 items-center justify-center rounded-xs px-1 py-px text-[12px] leading-[1.3] font-semibold tracking-[-0.24px] text-text-primary whitespace-nowrap',
  {
    variants: {
      color: {
        primary: '',
        ok: '',
        warn: '',
        est: '',
        disabled: '',
      },
      type: {
        outline: 'border border-solid bg-white',
        solid: '',
      },
    },
    compoundVariants: [
      { type: 'outline', color: 'primary', className: 'border-primary-400' },
      { type: 'outline', color: 'ok', className: 'border-primary-400' },
      { type: 'outline', color: 'warn', className: 'border-warning-400' },
      { type: 'outline', color: 'est', className: 'border-info-400' },
      { type: 'outline', color: 'disabled', className: 'border-gray-200' },
      { type: 'solid', color: 'primary', className: 'bg-primary-400' },
      { type: 'solid', color: 'ok', className: 'bg-primary-400' },
      { type: 'solid', color: 'warn', className: 'bg-warning-400' },
      { type: 'solid', color: 'est', className: 'bg-info-400' },
      { type: 'solid', color: 'disabled', className: 'bg-gray-200' },
    ],
    defaultVariants: {
      color: 'primary',
      type: 'outline',
    },
  },
);

type BadgeProps = ComponentPropsWithRef<'span'> & VariantProps<typeof badgeVariants>;

export default function Badge({ className, color, type, ref, ...props }: BadgeProps) {
  return <span ref={ref} className={cn(badgeVariants({ color, type }), className)} {...props} />;
}
