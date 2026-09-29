import { ListCard } from '@/components/common';
import type { JobDetailGlanceItem } from '@/features/jobs/types/jobDetail';

interface JobDetailAtAGlanceProps {
  items: JobDetailGlanceItem[];
}

export default function JobDetailAtAGlance({ items }: JobDetailAtAGlanceProps) {
  return (
    <div className="flex w-full flex-col gap-2">
      <p className="text-label-small font-semibold tracking-wide text-text-tertiary uppercase">
        At a Glance
      </p>
      <div className="grid grid-cols-3 gap-2">
        {items.map((item) => (
          <ListCard
            key={item.label}
            heading={item.value}
            headingClassName={item.isMuted ? 'text-text-tertiary' : undefined}
            caption={item.label}
            captionPosition="top"
          />
        ))}
      </div>
    </div>
  );
}
