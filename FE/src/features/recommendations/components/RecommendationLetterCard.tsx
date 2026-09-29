import { type ComponentPropsWithRef } from 'react';
import Badge from '@/components/common/Badge';
import Button from '@/components/common/Button';
import Tag from '@/components/common/Tag';
import { cn } from '@/utils/cn';

interface RecommendationLetterCardProps extends ComponentPropsWithRef<'article'> {
  issueDate: string;
  volumeLabel: string;
  recommendReason: string;
  title: string;
  matchScoreLabel: string;
  companyInfo: string;
  tags: string[];
  dDayLabel: string;
  noteTitle?: string;
  note: string;
  onExpand?: () => void;
  onSave?: () => void;
  onDismiss?: () => void;
}

export default function RecommendationLetterCard({
  className,
  ref,
  issueDate,
  volumeLabel,
  recommendReason,
  title,
  matchScoreLabel,
  companyInfo,
  tags,
  dDayLabel,
  noteTitle = "한눈에 요약 · Editor's Note",
  note,
  onExpand,
  onSave,
  onDismiss,
  ...props
}: RecommendationLetterCardProps) {
  return (
    <div className={cn('relative w-190', className)}>
      <div
        aria-hidden="true"
        className="absolute inset-0 translate-x-10.5 rounded-md border border-gray-200 bg-white"
      />
      <div
        aria-hidden="true"
        className="absolute inset-0 translate-x-5.25 rounded-md border border-gray-200 bg-white"
      />
      <article
        ref={ref}
        className="relative z-10 flex min-h-74 w-full flex-col items-start gap-5 rounded-md border border-gray-200 bg-white p-6"
        {...props}
      >
        <div className="flex w-full items-center justify-between gap-4">
          <p className="text-heading-medium text-center font-bold text-text-primary">
            Job.is Daily
          </p>
          <div className="flex flex-col items-end gap-0.5">
            <p className="text-heading-xxsmall text-right font-medium text-text-secondary">
              {issueDate}
            </p>
            <p className="text-heading-xxsmall font-medium text-text-tertiary">{volumeLabel}</p>
          </div>
        </div>

        <div className="h-0 w-full border-t border-gray-500" />

        <div className="flex w-full flex-col gap-2">
          <p className="text-body-small font-bold text-text-secondary">
            추천 이유 : {recommendReason}
          </p>
          <div className="flex min-w-0 items-start gap-2">
            <h2 className="min-w-0 flex-1 text-heading-xlarge text-left font-bold text-text-primary">
              {title}
            </h2>
            <Badge className="shrink-0">{matchScoreLabel}</Badge>
          </div>
          <p className="text-body-medium font-bold text-text-secondary">{companyInfo}</p>
          <div className="flex flex-wrap items-center gap-1.5">
            {tags.map((tag) => (
              <Tag key={tag} variant="plain" label={tag} />
            ))}
            <Badge className="rounded-full">{dDayLabel}</Badge>
          </div>
        </div>

        <div className="flex w-full flex-col gap-1 rounded-sm bg-gray-200 p-4">
          <p className="text-label-small font-bold text-text-tertiary">{noteTitle}</p>
          <p className="min-h-10.5 text-body-small leading-normal font-medium text-text-secondary">
            {note}
          </p>
        </div>

        <div className="h-0 w-full border-t border-gray-500" />

        <div className="flex w-full items-center gap-2">
          <Button className="w-118 shrink-0" onClick={onExpand}>
            펼쳐보기
          </Button>
          <Button variant="outline" className="w-25 shrink-0" onClick={onSave}>
            저장
          </Button>
          <Button
            className="w-25 shrink-0 bg-gray-400 text-gray-500 hover:bg-gray-400 active:bg-gray-400"
            onClick={onDismiss}
          >
            관심 없음
          </Button>
        </div>
      </article>
    </div>
  );
}
