import { useNavigate } from 'react-router';
import { Button, NoticePanel } from '@/components/common';
import RecommendationScreenLayout from './RecommendationScreenLayout';

export type RecommendationEmptyVariant = 'empty-candidates';

const NOTICE_BY_VARIANT: Record<
  RecommendationEmptyVariant,
  {
    icon: 'warning' | 'loading';
    title: string;
    description: string;
    footNote: string;
    primary: string;
  }
> = {
  'empty-candidates': {
    icon: 'warning',
    title: '오늘은 딱 맞는 공고가 적어요',
    description: '오늘은 조건에 맞는 공고를 찾지 못했어요.\n내일 아침 다시 골라 보내 드릴게요.',
    footNote: '메일함에서 지난 레터를 확인할 수 있어요.',
    primary: '조건 넓히기',
  },
};

export default function RecommendationEmptyScreen({
  variant,
}: {
  variant: RecommendationEmptyVariant;
}) {
  const navigate = useNavigate();
  const notice = NOTICE_BY_VARIANT[variant];

  const handlePrimaryAction = () => navigate('/profile');

  return (
    <RecommendationScreenLayout>
      <NoticePanel
        resultIconVariant={notice.icon}
        title={notice.title}
        description={notice.description}
        footNote={notice.footNote}
      >
        <Button className="w-103.5" onClick={handlePrimaryAction}>
          {notice.primary}
        </Button>
        <Button className="w-103.5" variant="outline" onClick={() => navigate('/explore')}>
          탐색 둘러보기
        </Button>
      </NoticePanel>
    </RecommendationScreenLayout>
  );
}
