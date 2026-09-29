export interface ExploreJobSummary {
  id: number;
  thumbnailUrl: string;
  dDayLabel: string;
  matchScoreLabel?: string;
  title: string;
  companyName: string;
  employmentInfo: string;
  isRemote: boolean;
}

export interface ExploreActiveFilter {
  key: string;
  label: string;
  onRemove: () => void;
}
