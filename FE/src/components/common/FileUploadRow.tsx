import { type ReactNode } from 'react';
import CheckCircleIcon from '@/assets/icons/icon-check-circle.svg?react';
import ChevronRightIcon from '@/assets/icons/icon-chevron-right.svg?react';
import CloseCircleIcon from '@/assets/icons/icon-close-circle.svg?react';
import CloseIcon from '@/assets/icons/icon-close.svg?react';
import DownloadIcon from '@/assets/icons/icon-download.svg?react';
import RefreshIcon from '@/assets/icons/icon-refresh.svg?react';
import { cn } from '@/utils/cn';

export type FileUploadStatus = 'uploading' | 'success' | 'idle' | 'error' | 'uploaded';

export type FileUploadItem = {
  id: string;
  name: string;
  status: FileUploadStatus;
  errorMessage?: string;
};

export function FileActionButton({
  icon,
  children,
  onClick,
}: {
  icon: ReactNode;
  children: ReactNode;
  onClick?: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="inline-flex h-[30px] items-center justify-center gap-1 rounded-sm border border-primary-400 bg-white px-3 text-label-small font-normal text-gray-900"
    >
      {children}
      {icon}
    </button>
  );
}

type FileUploadRowProps = {
  file: FileUploadItem;
  onRemove?: (id: string) => void;
  onDownload?: (id: string) => void;
  onPreview?: (id: string) => void;
};

export default function FileUploadRow({
  file,
  onRemove,
  onDownload,
  onPreview,
}: FileUploadRowProps) {
  const isError = file.status === 'error';

  return (
    <div
      className={cn(
        'flex w-full flex-col rounded-sm p-4',
        isError
          ? 'gap-3 border-2 border-[#DE3412] bg-[#FDEFEC]'
          : 'border border-gray-200 bg-white',
      )}
    >
      <div className="flex items-center justify-between gap-4">
        <span className="truncate text-body-medium font-medium text-gray-900">{file.name}</span>

        {file.status === 'uploading' && (
          <RefreshIcon className="size-5 shrink-0 animate-spin text-gray-900" />
        )}

        {file.status === 'success' && (
          <CheckCircleIcon className="size-5 shrink-0 text-primary-400" />
        )}

        {(file.status === 'idle' || file.status === 'error') && onRemove && (
          <FileActionButton
            icon={<CloseIcon className="size-4" />}
            onClick={() => onRemove(file.id)}
          >
            삭제
          </FileActionButton>
        )}

        {file.status === 'uploaded' && (onDownload || onPreview) && (
          <div className="flex shrink-0 gap-2">
            {onDownload && (
              <FileActionButton
                icon={<DownloadIcon className="size-4" />}
                onClick={() => onDownload(file.id)}
              >
                다운로드
              </FileActionButton>
            )}
            {onPreview && (
              <FileActionButton
                icon={<ChevronRightIcon className="size-4" />}
                onClick={() => onPreview(file.id)}
              >
                바로보기
              </FileActionButton>
            )}
          </div>
        )}
      </div>

      {isError && file.errorMessage && (
        <p className="flex items-start gap-1 text-body-medium font-medium whitespace-pre-line text-danger-500">
          <CloseCircleIcon className="size-5 shrink-0" />
          {file.errorMessage}
        </p>
      )}
    </div>
  );
}
