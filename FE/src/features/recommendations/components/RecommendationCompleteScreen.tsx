import { useNavigate } from 'react-router';
import RecommendationCompletion from './RecommendationCompletion';
import RecommendationScreenLayout from './RecommendationScreenLayout';
import useRecommendationDeck from '../hooks/useRecommendationDeck';

export default function RecommendationCompleteScreen() {
  const navigate = useNavigate();
  const { completionCounts } = useRecommendationDeck();

  return (
    <RecommendationScreenLayout>
      <RecommendationCompletion
        savedCount={completionCounts.saved}
        dismissedCount={completionCounts.dismissed}
        viewedCount={completionCounts.viewed}
        nextLetterNotice="내일 오전 7시 30분경, 다음 레터가 도착해요."
        onViewSaved={() => navigate('/recommendations/archive')}
        onExplore={() => navigate('/explore', { state: { transition: 'main-tab' } })}
      />
    </RecommendationScreenLayout>
  );
}
