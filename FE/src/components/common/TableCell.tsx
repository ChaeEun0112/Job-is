import { cva, type VariantProps } from 'class-variance-authority';
import { type ReactNode, type Ref } from 'react';
import { cn } from '@/utils/cn';

const tableCellVariants = cva('border-b border-solid px-4 leading-normal', {
  variants: {
    variant: {
      header: 'bg-primary-400 border-gray-300 py-2 text-base font-semibold text-text-primary',
      body: 'bg-white border-gray-200 py-3 text-sm font-medium text-text-primary',
    },
  },
  defaultVariants: {
    variant: 'body',
  },
});

interface TableCellProps extends VariantProps<typeof tableCellVariants> {
  className?: string;
  children: ReactNode;
  ref?: Ref<HTMLTableCellElement>;
}

export default function TableCell({ className, ref, variant, children }: TableCellProps) {
  const Tag = variant === 'header' ? 'th' : 'td';

  return (
    <Tag ref={ref} className={cn(tableCellVariants({ variant }), className)}>
      {children}
    </Tag>
  );
}
