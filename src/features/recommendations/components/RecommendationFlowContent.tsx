import { useNavigate } from 'react-router';
import type { BriefingState } from '@/api/types/recommendations.types';
import { Spinner } from '@/components/feedback';
import RecommendationArchiveScreen from './RecommendationArchiveScreen';
import RecommendationCompleteScreen from './RecommendationCompleteScreen';
import RecommendationDeckScreen from './RecommendationDeckScreen';
import RecommendationEmptyScreen from './RecommendationEmptyScreen';
import RecommendationIntroScreen from './RecommendationIntroScreen';
import RecommendationNewsScreen from './RecommendationNewsScreen';
import RecommendationPendingContent from './RecommendationPendingContent';
import RecommendationScreenLayout from './RecommendationScreenLayout';
import useRecommendationDeck from '../hooks/useRecommendationDeck';
import { useTodayBriefing } from '../hooks/useTodayBriefing';

export type RecommendationScreen =
  'pending' | 'intro' | 'deck' | 'news' | 'complete' | 'archive' | 'empty-candidates';

const EMPTY_SCREEN_BY_BRIEFING_STATE: Partial<Record<BriefingState, RecommendationScreen>> = {
  no_candidates: 'empty-candidates',
};

export default function RecommendationFlowContent({ screen }: { screen: RecommendationScreen }) {
  const navigate = useNavigate();
  const { cardsQuery, letters, isDeckCompleted } = useRecommendationDeck();
  const briefingQuery = useTodayBriefing();

  const isEntryScreen = screen === 'pending';
  const hasTodayCards = letters.length > 0;
  const briefingState = briefingQuery.data?.state;
  const resolvedScreen: RecommendationScreen = isEntryScreen
    ? isDeckCompleted
      ? 'complete'
      : hasTodayCards
        ? 'intro'
        : (briefingState && EMPTY_SCREEN_BY_BRIEFING_STATE[briefingState]) || screen
    : screen;

  if (isEntryScreen && (cardsQuery.isLoading || briefingQuery.isLoading)) {
    return (
      <RecommendationScreenLayout>
        <Spinner />
      </RecommendationScreenLayout>
    );
  }

  if (resolvedScreen === 'pending') {
    return (
      <RecommendationPendingContent
        onExploreClick={() => navigate('/explore', { state: { transition: 'main-tab' } })}
        onCompleteProfileClick={() => navigate('/profile')}
      />
    );
  }

  if (resolvedScreen === 'intro') return <RecommendationIntroScreen />;
  if (resolvedScreen === 'deck') return <RecommendationDeckScreen />;
  if (resolvedScreen === 'news') return <RecommendationNewsScreen />;
  if (resolvedScreen === 'complete') return <RecommendationCompleteScreen />;
  if (resolvedScreen === 'archive') return <RecommendationArchiveScreen />;

  return <RecommendationEmptyScreen variant={resolvedScreen} />;
}
