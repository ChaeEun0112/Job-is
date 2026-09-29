import { useId, useRef, useState, type ChangeEvent, type DragEvent } from 'react';
import CloseCircleIcon from '@/assets/icons/icon-close-circle.svg?react';
import CloseIcon from '@/assets/icons/icon-close.svg?react';
import DownloadIcon from '@/assets/icons/icon-download.svg?react';
import Button from '@/components/common/Button';
import { ACCEPTED_PROFILE_DOCUMENT_TYPES } from '@/features/profile/profileDocument';
import {
  type DocumentUpload,
  type DocumentUploadError,
  type ProfileDocument,
} from '@/features/profile/types/profileDocument';
import { cn } from '@/utils/cn';

interface UploadDropzoneProps {
  title: string;
  document: ProfileDocument | null;
  upload?: DocumentUpload;
  uploadError?: DocumentUploadError;
  onSelect: (file: File) => void;
  onDownload: () => void;
  isDownloading: boolean;
  onDelete: () => void;
  onCancelUpload: () => void;
  onClearError: () => void;
}

export default function UploadDropzone({
  title,
  document,
  upload,
  uploadError,
  onSelect,
  onDownload,
  isDownloading,
  onDelete,
  onCancelUpload,
  onClearError,
}: UploadDropzoneProps) {
  const inputId = useId();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);

  const selectFirstFile = (fileList: FileList | null) => {
    const file = fileList?.item(0);
    if (file) onSelect(file);
  };

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    selectFirstFile(event.target.files);
    event.target.value = '';
  };

  const handleDrop = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setIsDragging(false);
    selectFirstFile(event.dataTransfer.files);
  };

  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-body-medium font-medium text-gray-700">{title}</h2>

      {uploadError ? (
        <div
          className="flex flex-col gap-3 rounded-sm border-2 border-[#DE3412] bg-[#FDEFEC] p-4"
          role="alert"
        >
          <div className="flex items-center gap-4">
            <p className="min-w-0 flex-1 truncate text-body-medium font-medium text-text-primary">
              {uploadError.fileName} [{uploadError.fileSize}]
            </p>
            <Button
              variant="outline"
              className="h-7.5 shrink-0 rounded-sm px-3 text-label-small font-normal opacity-50"
              onClick={onClearError}
            >
              삭제
              <CloseIcon className="size-4" aria-hidden="true" />
            </Button>
          </div>
          <div className="border-t border-danger-200" />
          <div className="flex items-start gap-1 text-body-medium font-medium text-danger-500">
            <CloseCircleIcon className="mt-1 size-5 shrink-0" aria-hidden="true" />
            <p>
              {uploadError.messages[0]}
              <br />
              {uploadError.messages[1]}
            </p>
          </div>
        </div>
      ) : upload ? (
        <div className="flex min-h-26 items-center gap-4 rounded-sm border border-gray-200 bg-white p-4">
          <div className="flex min-w-0 max-w-100 flex-1 flex-col gap-2.5 p-2.5">
            <p className="truncate text-label-xsmall font-medium text-text-primary">
              {upload.fileName}
            </p>
            <div
              className="h-1 overflow-hidden rounded-full bg-gray-300"
              role="progressbar"
              aria-label={`${title} 업로드 진행률`}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-valuenow={upload.progress}
            >
              <div
                className="h-full rounded-full bg-primary-400 transition-[width] duration-100 ease-linear"
                style={{ width: `${upload.progress}%` }}
              />
            </div>
            <p className="text-label-xsmall font-medium text-text-tertiary">
              업로드 중 · {upload.progress}%
            </p>
          </div>
          <Button
            variant="outline"
            className="ml-auto h-7.5 shrink-0 rounded-sm px-3 text-label-small font-normal"
            onClick={onCancelUpload}
          >
            삭제
            <CloseIcon className="size-4" aria-hidden="true" />
          </Button>
        </div>
      ) : document ? (
        <div className="flex min-h-18 items-center gap-4 rounded-sm border border-gray-200 bg-white p-4">
          <div className="min-w-0 flex-1">
            <p className="truncate text-body-medium font-medium text-text-primary">
              {document.name}
            </p>
            <p className="text-body-medium font-medium text-text-tertiary">
              {document.uploadedAt} 업로드
            </p>
          </div>
          <div className="flex shrink-0 gap-4">
            <Button
              variant="outline"
              className="h-7.5 rounded-sm px-3 text-label-small font-normal"
              onClick={onDownload}
              disabled={isDownloading}
            >
              {isDownloading ? '다운로드 중…' : '다운로드'}
              <DownloadIcon className="size-4" aria-hidden="true" />
            </Button>
            <Button
              variant="outline"
              className="h-7.5 rounded-sm px-3 text-label-small font-normal"
              onClick={onDelete}
              disabled={isDownloading}
            >
              삭제
              <CloseIcon className="size-4" aria-hidden="true" />
            </Button>
          </div>
        </div>
      ) : (
        <div
          className={cn(
            'flex min-h-56 flex-col items-center justify-center gap-6 rounded-md border border-dashed border-gray-200 bg-white p-10 text-center transition-colors',
            isDragging && 'border-gray-800 bg-gray-50',
          )}
          onDragEnter={(event) => {
            event.preventDefault();
            setIsDragging(true);
          }}
          onDragOver={(event) => event.preventDefault()}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleDrop}
        >
          <div>
            <p className="text-body-large font-medium text-text-primary">
              첨부할 파일을 여기에 끌어다 놓거나 파일 선택 버튼을 직접 선택해주세요.
            </p>
            <p className="text-label-medium font-medium text-text-tertiary">
              PDF · DOCX · HWP · HWPX · 10MB 이하
            </p>
          </div>
          <Button
            className="h-12 min-w-20 text-white font-normal"
            onClick={() => fileInputRef.current?.click()}
          >
            파일선택
          </Button>
        </div>
      )}

      <input
        id={inputId}
        ref={fileInputRef}
        type="file"
        accept={ACCEPTED_PROFILE_DOCUMENT_TYPES}
        className="sr-only"
        onChange={handleChange}
        aria-label={`${title} 파일 선택`}
      />
    </section>
  );
}
