export type SavedJobBadgeColor = 'primary' | 'warn' | 'disabled';

export interface SavedJobBadge {
  label: string;
  color: SavedJobBadgeColor;
}

export interface SavedJobListing {
  id: string;
  title: string;
  meta: string;
  badges: SavedJobBadge[];
  closed?: boolean;
}

export type SavedJobHistoryStatus = 'viewed' | 'skipped' | 'intended';

export interface SavedJobHistoryItem {
  id: string;
  jobId: number;
  date: string;
  dateLabel: string;
  status: SavedJobHistoryStatus;
  title: string;
  time: string;
  canRestore?: boolean;
}
