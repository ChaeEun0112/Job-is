import ChevronRightIcon from '@/assets/icons/icon-chevron-right.svg?react';

function ProfileLinkRow({
  title,
  status,
  onClick,
}: {
  title: string;
  status: string;
  onClick?: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex min-h-18 w-full cursor-pointer items-center justify-between rounded-xs border border-gray-400 bg-white px-6 py-4 text-left transition-colors hover:bg-gray-50"
    >
      <span className="flex flex-wrap items-center gap-1.5">
        <strong className="text-body-small text-text-primary">{title}</strong>
        <span className="text-body-xsmall font-medium text-text-tertiary">{status}</span>
      </span>
      <ChevronRightIcon className="size-6 text-gray-800" aria-hidden="true" />
    </button>
  );
}

export default function ProfileLinksSection({
  onDocumentsClick,
  onAptitudeTestClick,
  aptitudeTestCompleted = false,
  documentsStatus = '미등록',
}: {
  onDocumentsClick?: () => void;
  onAptitudeTestClick?: () => void;
  aptitudeTestCompleted?: boolean;
  documentsStatus?: string;
}) {
  return (
    <section className="flex flex-col gap-5">
      <ProfileLinkRow
        title="이력서 • 자기소개서 관리"
        status={documentsStatus}
        onClick={onDocumentsClick}
      />
      <ProfileLinkRow
        title="직무 성향 테스트"
        status={aptitudeTestCompleted ? '완료' : '미완료'}
        onClick={onAptitudeTestClick}
      />
    </section>
  );
}
