import { cn } from '@/utils/cn';

interface RecommendationStatusTabsProps {
  tabs: string[];
  activeIndex: number;
  onChange?: (index: number) => void;
  className?: string;
}

export default function RecommendationStatusTabs({
  tabs,
  activeIndex,
  onChange,
  className,
}: RecommendationStatusTabsProps) {
  return (
    <div
      className={cn(
        'flex w-190 items-center justify-center gap-2.5 overflow-hidden rounded-xs p-2.5',
        className,
      )}
    >
      {tabs.map((label, index) => {
        const active = index === activeIndex;

        return (
          <button
            key={label}
            type="button"
            onClick={() => onChange?.(index)}
            className={cn(
              'flex h-10 items-center justify-center rounded-full border border-transparent px-3 text-label-large font-medium whitespace-nowrap transition-[background-color,border-color] duration-150 motion-reduce:transition-none',
              active
                ? 'bg-primary-600 text-gray-900'
                : 'border-gray-200 bg-white text-text-primary hover:bg-gray-50',
            )}
          >
            {label}
          </button>
        );
      })}
    </div>
  );
}
