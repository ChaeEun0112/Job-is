import { Link } from '@/components/common';
import type { RecommendationNewsDetailSource } from '@/features/recommendations/types/recommendationNewsDetail';

interface RecommendationNewsDetailSourceCardProps {
  source: RecommendationNewsDetailSource;
}

export default function RecommendationNewsDetailSourceCard({
  source,
}: RecommendationNewsDetailSourceCardProps) {
  return (
    <div className="flex w-full flex-col gap-2">
      <p className="text-label-small font-medium text-text-tertiary">출처</p>
      <div className="flex w-full flex-col gap-3 rounded-sm border border-gray-200 bg-white p-4">
        <div className="flex flex-col gap-1">
          <p className="text-body-small font-medium text-text-tertiary">{source.siteName}</p>
          <p className="text-body-small font-medium text-text-tertiary">
            등록 {source.registeredDate}
          </p>
        </div>
        <Link
          href={source.originalUrl}
          target="_blank"
          rel="noreferrer"
          className="text-body-small"
        >
          원문 보기
        </Link>
      </div>
    </div>
  );
}
