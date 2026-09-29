import { useNavigate } from 'react-router';
import RecommendationGreeting from './RecommendationGreeting';
import RecommendationQueryState from './RecommendationQueryState';
import RecommendationScreenLayout from './RecommendationScreenLayout';
import useRecommendationIntroAnimation from '../hooks/useRecommendationIntroAnimation';
import { useTodayBriefing } from '../hooks/useTodayBriefing';

export default function RecommendationIntroScreen() {
  const navigate = useNavigate();
  const briefingQuery = useTodayBriefing();
  const introAnimationMode = useRecommendationIntroAnimation(true);

  return (
    <RecommendationQueryState
      isLoading={briefingQuery.isLoading}
      isError={briefingQuery.isError}
      errorTitle="브리핑을 불러오지 못했어요"
      onRetry={() => briefingQuery.refetch()}
    >
      <RecommendationScreenLayout>
        <RecommendationGreeting
          reviewedCount={briefingQuery.data?.applicableCount ?? 0}
          matchedCount={briefingQuery.data?.curatedCount ?? 0}
          focusDescription={briefingQuery.data?.theme ?? ''}
          animationMode={introAnimationMode}
          onStart={() =>
            navigate('/recommendations/deck', { state: { transition: 'recommendation-flow' } })
          }
        />
      </RecommendationScreenLayout>
    </RecommendationQueryState>
  );
}
