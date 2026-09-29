import { useMemo, useRef } from 'react';
import { useNavigate } from 'react-router';
import { dismissCard } from '@/api/recommendations';
import { showAlert } from '@/components/feedback';
import {
  getRecommendationLetterStatus,
  useRecommendationDeckStore,
} from '@/store/useRecommendationDeckStore';
import { useSaveRecommendedJob } from './useSaveRecommendedJob';
import { useTodayBriefingCards } from './useTodayBriefing';
import { mapBriefingCard, mapBriefingCardStatus } from '../mapBriefingCard';

export default function useRecommendationDeck() {
  const navigate = useNavigate();
  const setStatus = useRecommendationDeckStore((state) => state.setStatus);
  const markViewed = useRecommendationDeckStore((state) => state.markViewed);
  const statusByLetterId = useRecommendationDeckStore((state) => state.statusByLetterId);
  const viewedLetterIds = useRecommendationDeckStore((state) => state.viewedLetterIds);
  const pendingLetterIdsRef = useRef(new Set<string>());

  const cardsQuery = useTodayBriefingCards();
  const { mutateAsync: saveRecommendedJob } = useSaveRecommendedJob();

  const cards = useMemo(
    () => [...(cardsQuery.data ?? [])].sort((a, b) => a.position - b.position),
    [cardsQuery.data],
  );
  const initialStatusByLetterId = useMemo(
    () =>
      Object.fromEntries(
        cards.map((card) => [String(card.cardId), mapBriefingCardStatus(card.status)]),
      ),
    [cards],
  );
  const resolvedCards = useMemo(
    () =>
      cards.map((card) => ({
        card,
        status: getRecommendationLetterStatus(
          statusByLetterId,
          String(card.cardId),
          initialStatusByLetterId[String(card.cardId)],
        ),
      })),
    [cards, initialStatusByLetterId, statusByLetterId],
  );
  const letters = useMemo(
    () =>
      resolvedCards
        .filter(({ status }) => status !== 'dismissed')
        .map(({ card }) => mapBriefingCard(card)),
    [resolvedCards],
  );
  const isDeckCompleted =
    resolvedCards.length > 0 && resolvedCards.every(({ status }) => status !== 'unprocessed');
  const completionCounts = useMemo(
    () => ({
      saved: resolvedCards.filter(({ status }) => status === 'saved').length,
      dismissed: resolvedCards.filter(({ status }) => status === 'dismissed').length,
      viewed: resolvedCards.filter(({ card }) => viewedLetterIds[String(card.cardId)]).length,
    }),
    [resolvedCards, viewedLetterIds],
  );
  const jobIdByLetterId = useMemo(
    () => Object.fromEntries(cards.map((card) => [String(card.cardId), card.jobId])),
    [cards],
  );
  const deckIdByLetterId = useMemo(
    () => Object.fromEntries(cards.map((card) => [String(card.cardId), card.deckId])),
    [cards],
  );

  const resolveStatus = (letterId: string) =>
    getRecommendationLetterStatus(statusByLetterId, letterId, initialStatusByLetterId[letterId]);

  const handleSaveLetter = async (letterId: string) => {
    if (pendingLetterIdsRef.current.has(letterId)) return false;

    const jobId = jobIdByLetterId[letterId];
    if (jobId === undefined) return false;

    pendingLetterIdsRef.current.add(letterId);
    const previousStatus = resolveStatus(letterId);
    setStatus(letterId, 'saved');

    try {
      await saveRecommendedJob(jobId);
      showAlert('success', '공고를 저장했어요.');
      return true;
    } catch (error) {
      setStatus(letterId, previousStatus);
      console.error(error);
      showAlert('danger', '공고를 저장하지 못했어요. 다시 시도해주세요.');
      return false;
    } finally {
      pendingLetterIdsRef.current.delete(letterId);
    }
  };

  const handleExpandLetter = (letterId: string) => {
    const jobId = jobIdByLetterId[letterId];
    const deckId = deckIdByLetterId[letterId];
    navigate(`/jobs/${jobId ?? letterId}`, {
      state: deckId ? { deckId, cardId: Number(letterId) } : undefined,
    });
  };

  const handleDismissLetter = async (letterId: string) => {
    if (pendingLetterIdsRef.current.has(letterId)) return false;

    const deckId = deckIdByLetterId[letterId];
    if (deckId === undefined) return false;

    pendingLetterIdsRef.current.add(letterId);
    const previousStatus = resolveStatus(letterId);
    setStatus(letterId, 'dismissed');

    try {
      await dismissCard(deckId, Number(letterId));
      showAlert('success', '관심 없음으로 표시했어요.');
      return true;
    } catch (error) {
      setStatus(letterId, previousStatus);
      console.error(error);
      showAlert('danger', '관심 없음 처리하지 못했어요. 다시 시도해주세요.');
      return false;
    } finally {
      pendingLetterIdsRef.current.delete(letterId);
    }
  };

  return {
    cardsQuery,
    letters,
    isDeckCompleted,
    completionCounts,
    viewedLetterIds,
    markViewed,
    resolveStatus,
    handleSaveLetter,
    handleExpandLetter,
    handleDismissLetter,
  };
}
