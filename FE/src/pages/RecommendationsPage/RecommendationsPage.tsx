import { useSearchParams } from 'react-router';
import RecommendationFlowContent, {
  type RecommendationScreen,
} from '@/features/recommendations/components/RecommendationFlowContent';

interface RecommendationsPageProps {
  screen?: RecommendationScreen;
}

export default function RecommendationsPage({ screen = 'pending' }: RecommendationsPageProps) {
  const [searchParams] = useSearchParams();
  // NOTE: ?preview= 쿼리스트링, 디자인 QA용 강제 진입. 실제 진입은 오늘의 브리핑 카드 존재 여부로 결정된다.
  const previewScreen = searchParams.get('preview') === 'intro' ? 'intro' : undefined;

  return <RecommendationFlowContent screen={previewScreen ?? screen} />;
}
