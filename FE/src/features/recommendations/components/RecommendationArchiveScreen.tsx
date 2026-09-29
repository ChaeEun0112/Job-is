import { useState } from 'react';
import type { RecommendationLetterStatus } from '@/store/useRecommendationDeckStore';
import RecommendationLetterCard from './RecommendationLetterCard';
import RecommendationLetterCarousel from './RecommendationLetterCarousel';
import RecommendationQueryState from './RecommendationQueryState';
import RecommendationScreenLayout from './RecommendationScreenLayout';
import RecommendationStatusTabs from './RecommendationStatusTabs';
import useRecommendationDeck from '../hooks/useRecommendationDeck';

const STATUS_TABS: { label: string; status: RecommendationLetterStatus }[] = [
  { label: '저장됨', status: 'saved' },
  { label: '관심 없음', status: 'dismissed' },
  { label: '미처리', status: 'unprocessed' },
];

const EMPTY_MESSAGE: Record<RecommendationLetterStatus, string> = {
  saved: '저장한 공고가 아직 없어요.',
  dismissed: '관심 없음으로 표시한 공고가 아직 없어요.',
  unprocessed: '아직 확인하지 않은 공고가 없어요.',
};

export default function RecommendationArchiveScreen() {
  const {
    cardsQuery,
    letters,
    resolveStatus,
    handleSaveLetter,
    handleExpandLetter,
    handleDismissLetter,
  } = useRecommendationDeck();

  const [statusIndex, setStatusIndex] = useState(0);
  const [cardIndex, setCardIndex] = useState(0);
  const activeStatus = STATUS_TABS[statusIndex].status;
  const filteredLetters = letters.filter((letter) => resolveStatus(letter.id) === activeStatus);

  const displayIndex = Math.min(cardIndex, Math.max(filteredLetters.length - 1, 0));
  const letter = filteredLetters[displayIndex];
  const contentKey = letter?.id ?? `empty-${activeStatus}`;

  return (
    <RecommendationQueryState
      isLoading={cardsQuery.isLoading}
      isError={cardsQuery.isError}
      errorTitle="목록을 불러오지 못했어요"
      onRetry={() => cardsQuery.refetch()}
    >
      <RecommendationScreenLayout>
        <RecommendationLetterCarousel
          key={activeStatus}
          className="gap-5"
          current={filteredLetters.length === 0 ? 0 : displayIndex + 1}
          total={filteredLetters.length}
          contentKey={contentKey}
          enableStackTransition
          onPrev={() => setCardIndex(Math.max(displayIndex - 1, 0))}
          onNext={() => setCardIndex(Math.min(displayIndex + 1, filteredLetters.length - 1))}
          prevDisabled={displayIndex === 0}
          nextDisabled={displayIndex >= filteredLetters.length - 1}
          filterSlot={
            <RecommendationStatusTabs
              tabs={STATUS_TABS.map((tab) => tab.label)}
              activeIndex={statusIndex}
              onChange={(index) => {
                setStatusIndex(index);
                setCardIndex(0);
              }}
            />
          }
        >
          <div key={activeStatus} className="page-content-enter">
            {letter ? (
              <RecommendationLetterCard
                {...letter}
                onExpand={() => handleExpandLetter(letter.id)}
                onSave={() => handleSaveLetter(letter.id)}
                onDismiss={() => handleDismissLetter(letter.id)}
              />
            ) : (
              <div className="flex min-h-142 w-190 items-center justify-center rounded-md border border-gray-200 bg-white p-6 text-center">
                <p className="text-heading-small font-medium text-text-secondary">
                  {EMPTY_MESSAGE[activeStatus]}
                </p>
              </div>
            )}
          </div>
        </RecommendationLetterCarousel>
      </RecommendationScreenLayout>
    </RecommendationQueryState>
  );
}
