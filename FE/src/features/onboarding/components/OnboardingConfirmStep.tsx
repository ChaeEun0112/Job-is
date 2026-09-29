import type { ProfileDraftResponse } from '@/api/types/profile.types';
import { Button, Tag } from '@/components/common';

interface OnboardingConfirmStepProps {
  draft: ProfileDraftResponse;
  resumeName?: string;
  onBack: () => void;
  onStart: () => void | Promise<void>;
  isStarting?: boolean;
  startError?: string;
}

export default function OnboardingConfirmStep({
  draft,
  resumeName,
  onBack,
  onStart,
  isStarting = false,
  startError,
}: OnboardingConfirmStepProps) {
  const careerLevelLabel = draft.careerLevel
    ? {
        ENTRY: '신입',
        JUNIOR: '1~3년',
        EXPERIENCED: '4년 이상',
      }[draft.careerLevel]
    : undefined;
  const preferenceSummary = [
    ...(draft.preferenceNotes ?? []),
    ...(draft.excludeKeywords ?? []),
    ...(draft.techStacks ?? []),
  ];
  const summaryItems = [
    {
      label: '직무 • 직군',
      value:
        (draft.jobCategories ?? []).map((category) => category.name).join(', ') || '선택 안 함',
    },
    {
      label: '지역 • 경력',
      value: `${draft.region?.name ?? '선택 안 함'} • ${careerLevelLabel ?? '선택 안 함'}`,
    },
    { label: '선호 • 제외 • 스택', value: preferenceSummary.join(' • ') || '선택 안 함' },
    { label: '이력서', value: resumeName ?? '미등록' },
  ];
  const personalityTags = draft.jobTestCompleted ? (draft.personalityTags ?? []) : [];

  return (
    <div className="flex min-h-0 w-full flex-1 items-center justify-center overflow-hidden bg-gray-50 px-3">
      <div className="flex w-full max-w-190 flex-col gap-5 rounded-md border border-gray-200 bg-white p-6">
        <h1 className="text-heading-medium text-text-primary">
          이대로 시작할게요. 맞는지 확인해 주세요.
        </h1>

        <div className="flex flex-col">
          {summaryItems.map((item) => (
            <div
              key={item.label}
              className="grid min-h-16 grid-cols-[140px_minmax(0,1fr)] items-center border-b border-dashed border-gray-400"
            >
              <p className="text-body-small font-bold text-text-secondary">{item.label}</p>
              <p className="min-w-0 text-body-small font-bold text-text-primary">{item.value}</p>
            </div>
          ))}

          {personalityTags.length > 0 ? (
            <div className="grid min-h-20 grid-cols-[140px_minmax(0,1fr)] items-center border-b border-dashed border-gray-400">
              <p className="text-body-small font-bold text-text-secondary">성향 결과</p>
              <div className="flex flex-wrap gap-1">
                {personalityTags.map((tag) => (
                  <Tag
                    key={tag}
                    variant="hash"
                    label={tag}
                    className="h-10 px-3 text-label-large font-medium"
                  />
                ))}
              </div>
            </div>
          ) : (
            <div className="grid min-h-16 grid-cols-[140px_minmax(0,1fr)] items-center border-b border-dashed border-gray-400">
              <p className="text-body-small font-bold text-text-secondary">성향 결과</p>
              <p className="text-body-small font-bold text-text-tertiary">미완료</p>
            </div>
          )}
        </div>

        <div className="flex min-h-17 items-center rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6">
          <p className="text-label-medium font-medium text-text-tertiary">
            저장한 조건은 <span className="text-text-secondary">다음 추천</span>부터 반영돼요.
          </p>
        </div>
        <div className="border-t border-gray-200" />

        {startError ? (
          <p role="alert" className="text-body-small font-medium text-error">
            {startError}
          </p>
        ) : null}

        <div className="flex items-center gap-5">
          <Button variant="outline" className="h-14 w-25" onClick={onBack} disabled={isStarting}>
            이전
          </Button>
          <Button className="h-14 flex-1" onClick={() => void onStart()} disabled={isStarting}>
            {isStarting ? '완료 처리 중...' : '이대로 시작'}
          </Button>
        </div>
      </div>
    </div>
  );
}
