import { type ComponentPropsWithRef } from 'react';
import { cn } from '@/utils/cn';

interface ProgressStep {
  label: string;
  progress: number;
}

interface ProgressStepperProps extends ComponentPropsWithRef<'div'> {
  steps: ProgressStep[];
}

export default function ProgressStepper({ className, ref, steps, ...props }: ProgressStepperProps) {
  return (
    <div ref={ref} className={cn('flex w-full flex-col', className)} {...props}>
      {steps.map((step, index) => {
        const isLast = index === steps.length - 1;
        const clampedProgress = Math.min(100, Math.max(0, step.progress));

        return (
          <div key={`${step.label}-${index}`} className="flex gap-4">
            <div className="flex w-6 flex-col items-center">
              <span className="size-6 shrink-0 rounded-full bg-primary-400" />
              {!isLast && <span className="w-px flex-1 bg-primary-400" />}
            </div>
            <div className={cn('flex flex-1 flex-col gap-2.5', !isLast && 'pb-4')}>
              <p className="text-[10px] leading-normal font-medium text-text-primary">
                {step.label}
              </p>
              <div
                className="h-1 w-full overflow-hidden rounded-full bg-gray-300"
                role="progressbar"
                aria-label={step.label}
                aria-valuenow={clampedProgress}
                aria-valuemin={0}
                aria-valuemax={100}
              >
                <div
                  className="h-full rounded-full bg-primary-400"
                  style={{ width: `${clampedProgress}%` }}
                />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
