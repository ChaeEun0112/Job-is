import { Fragment, type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

interface DetailListCardRow {
  label: string;
  value: string;
}

interface DetailListCardProps extends ComponentPropsWithRef<'div'> {
  title: string;
  rows: DetailListCardRow[];
}

export default function DetailListCard({
  className,
  ref,
  title,
  rows,
  ...props
}: DetailListCardProps) {
  return (
    <div
      ref={ref}
      className={cn(
        'flex w-full flex-col items-start justify-center gap-2 rounded-sm border border-gray-300 bg-white p-6',
        className,
      )}
      {...props}
    >
      <div className="flex w-full flex-col items-start justify-center gap-1">
        <div className="flex w-full items-center gap-4">
          <p className="flex-1 text-[18px] leading-normal font-bold text-text-primary">{title}</p>
        </div>
        {rows.map((row, index) => (
          <Fragment key={`${row.label}-${index}`}>
            {index > 0 && <div className="h-0 w-full border-t border-dashed border-gray-500" />}
            <div className="flex w-full items-center justify-between">
              <span className="text-[14px] leading-normal font-medium text-text-tertiary">
                {row.label}
              </span>
              <span
                className={cn(
                  'text-[14px] leading-normal font-medium',
                  row.value === '정보 없음' ? 'text-text-tertiary' : 'text-text-primary',
                )}
              >
                {row.value}
              </span>
            </div>
          </Fragment>
        ))}
      </div>
    </div>
  );
}
