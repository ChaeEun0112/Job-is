import { Spinner } from '@/components/feedback';
import RecommendationNews from './RecommendationNews';
import RecommendationScreenLayout from './RecommendationScreenLayout';
import useRecommendationNewsItems from '../hooks/useRecommendationNewsItems';

export default function RecommendationNewsScreen() {
  const { newsItems, isLoading } = useRecommendationNewsItems();

  return (
    <RecommendationScreenLayout>
      {isLoading ? <Spinner /> : <RecommendationNews items={newsItems} />}
    </RecommendationScreenLayout>
  );
}
