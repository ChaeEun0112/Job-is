export type BriefingState = 'pre_slot' | 'no_candidates' | 'onboarding_incomplete';

export interface BriefingSummary {
  deckId: number;
  deckDate: string;
  deckSeq: number;
  state: BriefingState | null;
  firstOpenedAt: string | null;
  greeting: string;
  applicableCount: number;
  curatedCount: number;
  theme: string;
}

export type BriefingCardStatus = 'PENDING' | 'SAVED' | 'DISMISSED';

export interface BriefingCard {
  cardId: number;
  deckId: number;
  jobId: number;
  postedAt: string;
  jobTitle: string;
  reason: string;
  fitScore: number | null;
  techStack: string[];
  jobScope: string;
  companyLocation: string | null;
  tags: string[];
  deadlineAt: string | null;
  summary: string | null;
  position: number;
  status: BriefingCardStatus;
}

export interface DismissReasonRequest {
  reason?: string;
  comment?: string;
}

export type ContentType = 'NEWS' | 'BENEFIT';

export interface ContentSummary {
  contentId: number;
  contentType: ContentType;
  tag: string;
  title: string;
  summary: string;
  sourceName: string;
  publishedAt: string;
}

export interface ContentDetail {
  contentId: number;
  contentType: ContentType;
  tag: string;
  title: string;
  summary: string;
  body: string;
  sourceName: string;
  publishedAt: string;
  target: string;
  applicationStartDate: string;
  applicationEndDate: string;
  applicationMethod: string;
  originalUrl: string;
}
