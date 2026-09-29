import Badge from '@/components/common/Badge';
import ListCard from '@/components/common/ListCard';
import type { RecommendationNewsItem } from '@/features/recommendations/types/recommendationNewsItem';
import { cn } from '@/utils/cn';

interface RecommendationNewsProps {
  items: RecommendationNewsItem[];
  className?: string;
  returnTo?: string;
}

export default function RecommendationNews({
  items,
  className,
  returnTo = '/recommendations/news',
}: RecommendationNewsProps) {
  return (
    <section
      className={cn(
        'mx-auto flex w-full max-w-180 flex-col gap-4 rounded-sm border border-gray-200 bg-white p-6',
        className,
      )}
    >
      <p className="text-body-medium font-bold text-text-secondary">오늘의 소식</p>
      <div className="grid grid-cols-2 gap-4">
        {items.map((item) => (
          <ListCard
            key={item.href ?? item.title}
            badge={<Badge color="disabled">{item.badgeLabel}</Badge>}
            heading={item.title}
            caption={item.description}
            linkLabel="자세히 보기"
            linkHref={item.href}
            linkState={{ returnTo }}
          />
        ))}
      </div>
    </section>
  );
}
