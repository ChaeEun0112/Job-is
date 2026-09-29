import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { Spinner } from '@/components/feedback';
import RecommendationLetterCard from './RecommendationLetterCard';
import RecommendationLetterCarousel from './RecommendationLetterCarousel';
import RecommendationNews from './RecommendationNews';
import RecommendationQueryState from './RecommendationQueryState';
import RecommendationScreenLayout from './RecommendationScreenLayout';
import useRecommendationDeck from '../hooks/useRecommendationDeck';
import useRecommendationNewsItems from '../hooks/useRecommendationNewsItems';

export default function RecommendationDeckScreen() {
  const navigate = useNavigate();
  const {
    cardsQuery,
    letters,
    markViewed,
    handleSaveLetter,
    handleExpandLetter,
    handleDismissLetter,
  } = useRecommendationDeck();
  const { newsItems, isLoading: isNewsLoading } = useRecommendationNewsItems();

  const DECK = useMemo(
    () => [
      ...letters.map((letter) => ({ type: 'card', letter }) as const),
      { type: 'news' } as const,
    ],
    [letters],
  );

  const [deckIndex, setDeckIndex] = useState(0);
  const currentDeckIndex = Math.min(deckIndex, DECK.length - 1);
  const deckStep = DECK[currentDeckIndex];

  useEffect(() => {
    if (deckStep.type === 'card') markViewed(deckStep.letter.id);
  }, [deckStep, markViewed]);

  const isLastStep = currentDeckIndex === DECK.length - 1;
  const goNext = () => {
    if (isLastStep) {
      navigate('/recommendations/complete', { state: { transition: 'recommendation-flow' } });
    } else setDeckIndex(currentDeckIndex + 1);
  };

  return (
    <RecommendationQueryState
      isLoading={cardsQuery.isLoading}
      isError={cardsQuery.isError}
      errorTitle="오늘의 추천을 불러오지 못했어요"
      onRetry={() => cardsQuery.refetch()}
    >
      <RecommendationScreenLayout>
        <RecommendationLetterCarousel
          current={currentDeckIndex + 1}
          total={DECK.length}
          contentKey={deckStep.type === 'card' ? deckStep.letter.id : 'news'}
          enableStackTransition
          onPrev={() => setDeckIndex(Math.max(currentDeckIndex - 1, 0))}
          onNext={goNext}
          prevDisabled={currentDeckIndex === 0}
          footNote={deckStep.type === 'news' ? '' : undefined}
        >
          {deckStep.type === 'news' ? (
            isNewsLoading ? (
              <Spinner />
            ) : (
              <RecommendationNews items={newsItems} returnTo="/recommendations/deck" />
            )
          ) : (
            <RecommendationLetterCard
              {...deckStep.letter}
              onExpand={() => handleExpandLetter(deckStep.letter.id)}
              onSave={async () => {
                const isSaved = await handleSaveLetter(deckStep.letter.id);
                if (isSaved) goNext();
              }}
              onDismiss={() => {
                handleDismissLetter(deckStep.letter.id);
              }}
            />
          )}
        </RecommendationLetterCarousel>
      </RecommendationScreenLayout>
    </RecommendationQueryState>
  );
}
