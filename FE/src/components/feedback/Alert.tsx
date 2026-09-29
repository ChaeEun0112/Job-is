import { cva, type VariantProps } from 'class-variance-authority';
import { type ReactNode } from 'react';
import AlertTriangleIcon from '@/assets/icons/icon-alert-triangle.svg?react';
import CheckCircleIcon from '@/assets/icons/icon-check-circle.svg?react';
import CloseCircleIcon from '@/assets/icons/icon-close-circle.svg?react';
import InfoCircleIcon from '@/assets/icons/icon-info-circle.svg?react';
import { cn } from '@/utils/cn';

const alertVariants = cva('flex w-full rounded-md border border-solid p-4', {
  variants: {
    variant: {
      success: 'bg-primary-50 border-primary-100 text-primary-500',
      warning: 'bg-warning-50 border-warning-100 text-warning-500',
      info: 'bg-info-50 border-info-100 text-info-500',
      danger: 'bg-danger-50 border-danger-100 text-danger-500',
    },
  },
  defaultVariants: { variant: 'success' },
});

const ALERT_ICONS = {
  success: CheckCircleIcon,
  warning: AlertTriangleIcon,
  info: InfoCircleIcon,
  danger: CloseCircleIcon,
} as const;

type AlertBaseProps = VariantProps<typeof alertVariants> & { className?: string };

type AlertProps =
  | (AlertBaseProps & { size?: 'title'; title: string; children: ReactNode })
  | (AlertBaseProps & { size: 'slim'; children: ReactNode });

export default function Alert(props: AlertProps) {
  const { className } = props;
  const resolvedVariant = props.variant ?? 'success';
  const Icon = ALERT_ICONS[resolvedVariant];

  if (props.size === 'slim') {
    return (
      <div
        className={cn(
          alertVariants({ variant: resolvedVariant }),
          'flex-row items-center gap-2',
          className,
        )}
      >
        <Icon className="size-6 shrink-0" />
        <p className="flex-1 truncate text-[16px] leading-normal font-medium text-text-primary">
          {props.children}
        </p>
      </div>
    );
  }

  return (
    <div
      className={cn(
        alertVariants({ variant: resolvedVariant }),
        'flex-col items-start justify-center gap-2',
        className,
      )}
    >
      <div className="flex w-full items-center gap-2">
        <Icon className="size-6 shrink-0" />
        <p className="flex-1 truncate text-[18px] leading-normal font-bold">{props.title}</p>
      </div>
      <div className="w-full pl-8">
        <p className="line-clamp-2 text-[16px] leading-normal font-medium text-text-primary">
          {props.children}
        </p>
      </div>
    </div>
  );
}
