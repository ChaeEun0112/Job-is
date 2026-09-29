import { api } from '@/api/base/request';
import type {
  BriefingCard,
  BriefingSummary,
  ContentDetail,
  ContentSummary,
  DismissReasonRequest,
} from './types/recommendations.types';

export async function getTodayBriefing() {
  return api.get<BriefingSummary>('/api/briefings/today');
}

export async function getTodayBriefingStatus() {
  return api.get<BriefingCard[]>('/api/briefings/status');
}

export async function generateTodayDeck() {
  return api.post<BriefingCard[]>('/api/decks/generate');
}

export async function dismissCard(deckId: number, cardId: number) {
  return api.post<BriefingCard>(`/api/decks/${deckId}/cards/${cardId}/dismiss`);
}

export async function cancelDismissCard(deckId: number, cardId: number) {
  return api.delete<BriefingCard>(`/api/decks/${deckId}/cards/${cardId}/dismiss`);
}

export async function submitDismissReason(
  deckId: number,
  cardId: number,
  request: DismissReasonRequest,
) {
  await api.post<void>(`/api/decks/${deckId}/cards/${cardId}/dismiss-reason`, request);
}

export async function getContents() {
  return api.get<ContentSummary[]>('/api/contents');
}

export async function getContent(contentId: number) {
  return api.get<ContentDetail>(`/api/contents/${contentId}`);
}
