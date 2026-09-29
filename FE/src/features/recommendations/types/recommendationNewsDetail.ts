export interface RecommendationNewsDetailInfoRow {
  label: string;
  value: string;
}

export interface RecommendationNewsDetailSource {
  siteName: string;
  registeredDate: string;
  originalUrl: string;
}

export interface RecommendationNewsDetail {
  id: string;
  badgeLabel: string;
  title: string;
  applicationPeriod: string;
  summary: string;
  applicationInfoTitle: string;
  applicationInfo: RecommendationNewsDetailInfoRow[];
  source: RecommendationNewsDetailSource;
}
