import { cva, type VariantProps } from 'class-variance-authority';
import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

const buttonVariants = cva(
  'inline-flex h-14 cursor-pointer items-center justify-center gap-1 rounded-sm px-5 text-base font-medium transition-colors disabled:cursor-not-allowed',
  {
    variants: {
      variant: {
        solid:
          'bg-primary-400 text-gray-900 hover:bg-primary-500 active:bg-primary-600 disabled:bg-gray-200 disabled:text-gray-600',
        outline:
          'border border-primary-400 bg-white text-gray-900 hover:bg-gray-50 active:bg-gray-100 disabled:border-gray-200 disabled:text-gray-600',
      },
    },
    defaultVariants: {
      variant: 'solid',
    },
  },
);

type ButtonProps = ComponentPropsWithRef<'button'> & VariantProps<typeof buttonVariants>;

export default function Button({ className, type = 'button', variant, ...props }: ButtonProps) {
  return <button type={type} className={cn(buttonVariants({ variant }), className)} {...props} />;
}
