import { cva, type VariantProps } from 'class-variance-authority';
import CheckIcon from '@/assets/icons/icon-check.svg?react';
import CloseIcon from '@/assets/icons/icon-close.svg?react';
import ExclamationIcon from '@/assets/icons/icon-exclamation.svg?react';
import QuizIcon from '@/assets/icons/icon-quiz.svg?react';
import RefreshIcon from '@/assets/icons/icon-refresh.svg?react';
import { cn } from '@/utils/cn';

const resultIconVariants = cva(
  'inline-flex size-12 shrink-0 items-center justify-center rounded-full border border-solid bg-white',
  {
    variants: {
      variant: {
        success: 'border-primary-400 text-gray-900',
        warning: 'border-warning-400 text-gray-900',
        danger: 'border-danger-400 text-gray-900',
        loading: 'border-info-400 text-gray-900',
        quiz: 'border-[#5917b8] text-gray-900',
      },
    },
    defaultVariants: { variant: 'success' },
  },
);

const RESULT_ICONS = {
  success: CheckIcon,
  warning: ExclamationIcon,
  danger: CloseIcon,
  loading: RefreshIcon,
  quiz: QuizIcon,
} as const;

interface ResultIconProps extends VariantProps<typeof resultIconVariants> {
  className?: string;
}

export default function ResultIcon({ className, variant = 'success' }: ResultIconProps) {
  const Icon = RESULT_ICONS[variant ?? 'success'];

  return (
    <span className={cn(resultIconVariants({ variant }), className)}>
      <Icon className="size-8" aria-hidden="true" />
    </span>
  );
}
