import { DetailListCard } from '@/components/common';
import type { RecommendationNewsDetailInfoRow } from '@/features/recommendations/types/recommendationNewsDetail';

interface RecommendationNewsDetailApplicationInfoProps {
  title: string;
  rows: RecommendationNewsDetailInfoRow[];
}

export default function RecommendationNewsDetailApplicationInfo({
  title,
  rows,
}: RecommendationNewsDetailApplicationInfoProps) {
  return (
    <div className="flex w-full flex-col gap-2">
      <p className="text-label-small font-medium text-text-tertiary">신청 정보</p>
      <DetailListCard title={title} rows={rows} />
    </div>
  );
}
