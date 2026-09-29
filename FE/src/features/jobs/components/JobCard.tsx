import { type ComponentPropsWithRef, type MouseEvent } from 'react';
import MoreVerticalIcon from '@/assets/icons/icon-more-vertical.svg?react';
import { Badge } from '@/components/common';
import { cn } from '@/utils/cn';

interface JobCardProps extends ComponentPropsWithRef<'article'> {
  thumbnailUrl: string;
  thumbnailAlt?: string;
  dDayLabel: string;
  matchScoreLabel?: string;
  avatarUrl?: string;
  avatarAlt?: string;
  title: string;
  companyName: string;
  employmentInfo: string;
  onMoreClick?: (event: MouseEvent<HTMLButtonElement>) => void;
}

export default function JobCard({
  className,
  ref,
  thumbnailUrl,
  thumbnailAlt = '',
  dDayLabel,
  matchScoreLabel,
  avatarUrl,
  avatarAlt = '',
  title,
  companyName,
  employmentInfo,
  onMoreClick,
  ...props
}: JobCardProps) {
  return (
    <article
      ref={ref}
      className={cn('flex w-87.5 flex-col items-start gap-3', className)}
      {...props}
    >
      <div className="relative h-49.25 w-full overflow-hidden rounded-md border border-white">
        <img src={thumbnailUrl} alt={thumbnailAlt} className="size-full object-cover" />
        <Badge type="outline" color="primary" className="absolute top-2 left-2.5">
          {dDayLabel}
        </Badge>
        {matchScoreLabel && (
          <Badge type="solid" color="primary" className="absolute right-2.5 bottom-1.75">
            {matchScoreLabel}
          </Badge>
        )}
      </div>
      <div className="flex w-full items-start gap-3 px-2.5">
        {avatarUrl && (
          <img
            src={avatarUrl}
            alt={avatarAlt}
            className="size-9 shrink-0 rounded-full object-cover"
          />
        )}
        <div className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
          <p className="truncate text-[16px] font-semibold text-text-primary">{title}</p>
          <p className="truncate text-sm font-medium text-text-secondary">{companyName}</p>
          <p className="truncate text-sm font-medium text-text-secondary">{employmentInfo}</p>
        </div>
        {onMoreClick && (
          <button
            type="button"
            onClick={onMoreClick}
            aria-label="더보기"
            className="flex size-6 shrink-0 items-center justify-center"
          >
            <MoreVerticalIcon className="size-6" />
          </button>
        )}
      </div>
    </article>
  );
}
