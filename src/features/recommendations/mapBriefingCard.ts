import type { BriefingCard, BriefingCardStatus } from '@/api/types/recommendations.types';
import type { RecommendationLetter } from '@/features/recommendations/types/recommendationLetter';
import type { RecommendationLetterStatus } from '@/store/useRecommendationDeckStore';
import { formatDDayLabel } from '@/utils/formatDDayLabel';

function formatIssueDate(postedAt: string): string {
  const date = new Date(postedAt);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}.${month}.${day}`;
}

function formatMatchScoreLabel(fitScore: number | null): string {
  if (fitScore === null) return '적합도 산정 중';
  return `적합도 ${Math.round(fitScore)}%`;
}

export function mapBriefingCard(card: BriefingCard): RecommendationLetter {
  return {
    id: String(card.cardId),
    issueDate: formatIssueDate(card.postedAt),
    volumeLabel: `VOL.1 · No.${card.cardId}`,
    recommendReason: card.reason,
    title: card.jobTitle,
    matchScoreLabel: formatMatchScoreLabel(card.fitScore),
    companyInfo: card.companyLocation
      ? `${card.jobScope} · ${card.companyLocation}`
      : card.jobScope,
    tags: card.tags,
    dDayLabel: formatDDayLabel(card.deadlineAt),
    note: card.summary ?? '요약 정보를 준비 중이에요.',
  };
}

const STATUS_MAP: Record<BriefingCardStatus, RecommendationLetterStatus> = {
  PENDING: 'unprocessed',
  SAVED: 'saved',
  DISMISSED: 'dismissed',
};

export function mapBriefingCardStatus(status: BriefingCardStatus): RecommendationLetterStatus {
  return STATUS_MAP[status];
}
