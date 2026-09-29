import ArrowRightIcon from '@/assets/icons/icon-arrow-right.svg?react';
import QuizIcon from '@/assets/icons/icon-quiz.svg?react';
import { Button } from '@/components/common';

interface QuizStartProps {
  onStart: () => void;
}

export default function QuizStart({ onStart }: QuizStartProps) {
  return (
    <section className="flex min-h-74 w-full max-w-190 flex-col items-center gap-5 overflow-hidden rounded-md border border-gray-200 bg-white p-6">
      <div
        className="flex size-12 shrink-0 items-center justify-center rounded-full border border-[#5917b8] bg-white"
        aria-hidden="true"
      >
        <QuizIcon className="size-8 text-gray-800" />
      </div>

      <h1 className="text-heading-medium font-semibold text-text-primary">직무 성향 테스트</h1>

      <p className="text-label-medium font-medium text-text-secondary">양자 택일 10문항 · 약 3분</p>

      <p className="flex w-103.75 items-center justify-center rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6 text-label-medium font-medium text-text-tertiary">
        결과는 추천 선호 시드로 프로필에 반영돼요.
      </p>

      <Button className="w-103.5" onClick={onStart}>
        시작하기
        <ArrowRightIcon className="size-6" aria-hidden="true" />
      </Button>
    </section>
  );
}
