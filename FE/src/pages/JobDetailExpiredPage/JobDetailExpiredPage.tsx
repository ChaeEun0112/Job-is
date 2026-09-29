import { useNavigate } from 'react-router';
import ChevronLeftIcon from '@/assets/icons/icon-chevron-left.svg?react';
import { Button, NoticePanel } from '@/components/common';

export default function JobDetailExpiredPage() {
  const navigate = useNavigate();

  return (
    <div className="flex min-h-0 w-full flex-1 items-center justify-center gap-4 bg-gray-50 px-3 py-10">
      <NoticePanel
        headerLeft={
          <Button
            variant="outline"
            onClick={() => navigate('/recommendations')}
            className="h-[35px] w-fit gap-1 rounded-sm border-primary-400 bg-white px-5 text-body-small font-medium text-text-secondary hover:bg-primary-50"
          >
            <ChevronLeftIcon className="size-4" />
            오늘의 추천으로
          </Button>
        }
        resultIconVariant="warning"
        title="이 공고의 원문이 만료되었거나 변경됐어요"
        description="원티드에서 삭제·수정되어 지금은 열 수 없어요."
      >
        <div className="flex w-full flex-wrap items-center justify-center gap-3">
          <Button variant="outline" onClick={() => navigate('/saved')}>
            저장 목록으로
          </Button>
          <Button variant="outline" onClick={() => navigate('/explore')}>
            비슷한 추천 보기
          </Button>
          <Button className="border-none bg-gray-400 text-text-tertiary hover:bg-gray-400 active:bg-gray-400">
            출처(원티드) 홈
          </Button>
        </div>
        <div className="h-px w-full bg-gray-200" />
        <div className="flex w-full flex-wrap items-center justify-center gap-3">
          <Button variant="outline" className="w-[250px]" disabled>
            저장 해제
          </Button>
          <Button className="w-[250px] border-none bg-gray-400 text-text-tertiary hover:bg-gray-400 active:bg-gray-400">
            관심 없음
          </Button>
        </div>
      </NoticePanel>
    </div>
  );
}
