export interface RecommendationLetter {
  id: string;
  issueDate: string;
  volumeLabel: string;
  recommendReason: string;
  title: string;
  matchScoreLabel: string;
  companyInfo: string;
  tags: string[];
  dDayLabel: string;
  note: string;
}
