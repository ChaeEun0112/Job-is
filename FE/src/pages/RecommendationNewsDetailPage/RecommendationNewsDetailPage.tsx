import { useLocation, useNavigate, useParams } from 'react-router';
import { Button, NoticePanel } from '@/components/common';
import { Spinner } from '@/components/feedback';
import RecommendationNewsDetailMain from '@/features/recommendations/components/RecommendationNewsDetailMain';
import { useContentDetail } from '@/features/recommendations/hooks/useContents';
import { mapContentDetail } from '@/features/recommendations/mapContent';

export default function RecommendationNewsDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const parsedId = Number(id);
  const isValidId = Number.isInteger(parsedId) && parsedId > 0;
  const contentQuery = useContentDetail(isValidId ? parsedId : NaN);
  const returnTo =
    location.state &&
    typeof location.state === 'object' &&
    'returnTo' in location.state &&
    location.state.returnTo === '/recommendations/deck'
      ? '/recommendations/deck'
      : '/recommendations/news';
  const backLabel = returnTo === '/recommendations/deck' ? '오늘의 추천으로' : '오늘의 소식으로';

  return (
    <div className="flex min-h-0 w-full flex-1 items-start justify-center bg-gray-50 px-3 py-10">
      <div className="flex w-full max-w-160 flex-col">
        {!isValidId ? (
          <NoticePanel resultIconVariant="warning" title="존재하지 않는 소식이에요">
            <Button onClick={() => navigate(returnTo, { replace: true })}>목록으로</Button>
          </NoticePanel>
        ) : contentQuery.isLoading ? (
          <div className="flex w-full items-center justify-center py-20">
            <Spinner />
          </div>
        ) : contentQuery.isError || !contentQuery.data ? (
          <NoticePanel resultIconVariant="danger" title="정보를 불러오지 못했어요">
            <Button onClick={() => contentQuery.refetch()}>다시 시도</Button>
          </NoticePanel>
        ) : (
          <RecommendationNewsDetailMain
            newsDetail={mapContentDetail(contentQuery.data)}
            backTo={returnTo}
            backLabel={backLabel}
          />
        )}
      </div>
    </div>
  );
}
