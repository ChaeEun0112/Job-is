import { cva, type VariantProps } from 'class-variance-authority';
import { type ComponentPropsWithRef } from 'react';
import ExternalLinkIcon from '@/assets/icons/icon-external-link.svg?react';
import { cn } from '@/utils/cn';

const linkVariants = cva(
  'inline-flex items-center gap-1 align-middle text-label-large font-medium',
  {
    variants: {
      variant: {
        default: 'text-primary-400 underline decoration-solid underline-offset-2',
        subtle: 'text-gray-900 underline underline-offset-2',
        subtleNone: 'text-gray-900',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);

type LinkProps = ComponentPropsWithRef<'a'> &
  VariantProps<typeof linkVariants> & {
    iconClassName?: string;
  };

export default function Link({ className, variant, iconClassName, children, ...props }: LinkProps) {
  return (
    <a className={cn(linkVariants({ variant }), className)} {...props}>
      {children}
      <ExternalLinkIcon className={cn('size-4', iconClassName)} />
    </a>
  );
}
