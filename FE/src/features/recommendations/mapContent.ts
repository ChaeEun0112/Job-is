import type { ContentDetail, ContentSummary, ContentType } from '@/api/types/recommendations.types';
import type { RecommendationNewsDetail } from '@/features/recommendations/types/recommendationNewsDetail';
import type { RecommendationNewsItem } from '@/features/recommendations/types/recommendationNewsItem';
import { formatDotDate } from '@/utils/formatDotDate';

const CONTENT_TYPE_LABEL: Record<ContentType, string> = {
  NEWS: '뉴스',
  BENEFIT: '혜택',
};

export function mapContentSummary(content: ContentSummary): RecommendationNewsItem {
  return {
    badgeLabel: CONTENT_TYPE_LABEL[content.contentType],
    title: content.title,
    description: content.summary,
    href: `/recommendations/news/${content.contentId}`,
  };
}

export function mapContentDetail(content: ContentDetail): RecommendationNewsDetail {
  return {
    id: String(content.contentId),
    badgeLabel: CONTENT_TYPE_LABEL[content.contentType],
    title: content.title,
    applicationPeriod: `${formatDotDate(content.applicationStartDate)} ~ ${formatDotDate(content.applicationEndDate)}`,
    summary: content.body,
    applicationInfoTitle: '신청 정보',
    applicationInfo: [
      { label: '대상', value: content.target },
      {
        label: '기간',
        value: `${formatDotDate(content.applicationStartDate)} ~ ${formatDotDate(content.applicationEndDate)}`,
      },
      { label: '방법', value: content.applicationMethod },
    ],
    source: {
      siteName: content.sourceName,
      registeredDate: formatDotDate(content.publishedAt),
      originalUrl: content.originalUrl,
    },
  };
}
