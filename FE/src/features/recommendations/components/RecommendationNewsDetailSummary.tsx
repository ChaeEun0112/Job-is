import { Badge } from '@/components/common';
import type { RecommendationNewsDetail } from '@/features/recommendations/types/recommendationNewsDetail';

interface RecommendationNewsDetailSummaryProps {
  newsDetail: Pick<
    RecommendationNewsDetail,
    'badgeLabel' | 'title' | 'applicationPeriod' | 'source'
  >;
}

export default function RecommendationNewsDetailSummary({
  newsDetail,
}: RecommendationNewsDetailSummaryProps) {
  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-col gap-2">
        <Badge type="outline" color="primary" className="w-fit">
          {newsDetail.badgeLabel}
        </Badge>
        <h1 className="text-heading-medium font-bold text-text-primary">{newsDetail.title}</h1>
        <div className="flex flex-col gap-0.5 text-label-medium font-medium text-text-primary">
          <p>출처 {newsDetail.source.siteName}</p>
          <p>등록 {newsDetail.source.registeredDate}</p>
          <p>신청기간 {newsDetail.applicationPeriod}</p>
        </div>
      </div>
      <div className="h-px w-full bg-gray-200" />
    </div>
  );
}
