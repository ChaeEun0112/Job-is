import { useId, useRef, type ChangeEvent, type DragEvent } from 'react';
import CloseIcon from '@/assets/icons/icon-close.svg?react';
import { cn } from '@/utils/cn';
import FileUploadRow, { FileActionButton, type FileUploadItem } from './FileUploadRow';

export type { FileUploadItem };

type FileUploadProps = {
  title: string;
  description?: string;
  files: FileUploadItem[];
  maxFiles?: number;
  onSelectFiles?: (fileList: FileList) => void;
  onRemove?: (id: string) => void;
  onClearAll?: () => void;
  onDownload?: (id: string) => void;
  onPreview?: (id: string) => void;
  className?: string;
};

export default function FileUpload({
  title,
  description,
  files,
  maxFiles = 10,
  onSelectFiles,
  onRemove,
  onClearAll,
  onDownload,
  onPreview,
  className,
}: FileUploadProps) {
  const inputId = useId();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) onSelectFiles?.(event.target.files);
    event.target.value = '';
  };

  const handleDragOver = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault();
  };

  const handleDrop = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    if (event.dataTransfer.files.length > 0) onSelectFiles?.(event.dataTransfer.files);
  };

  return (
    <div
      className={cn(
        'flex w-[1060px] flex-col items-start gap-6 rounded-md border border-[#B1B8BE] bg-white p-10',
        className,
      )}
    >
      <div className="flex flex-col gap-5">
        <h3 className="text-heading-medium font-bold text-gray-900">{title}</h3>

        {description && (
          <p className="text-body-medium font-medium text-[#464C53]">{description}</p>
        )}
      </div>

      <div
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        className="flex w-full flex-col items-center gap-3 rounded-sm border border-dashed border-gray-200 px-10 py-10 text-center"
      >
        <span className="text-body-large font-medium text-gray-900">
          첨부할 파일을 여기에 끌어다 놓거나 파일 선택 버튼을 직접 선택해주세요.
        </span>

        <button
          type="button"
          disabled={!onSelectFiles}
          onClick={() => fileInputRef.current?.click()}
          className={cn(
            'inline-flex h-12 min-w-[78px] items-center justify-center gap-1 rounded-[6px] bg-primary-400 px-4 text-body-medium font-normal text-white',
            'disabled:cursor-not-allowed disabled:bg-gray-200 disabled:text-gray-400',
          )}
        >
          파일선택
        </button>

        <input
          id={inputId}
          ref={fileInputRef}
          type="file"
          multiple
          className="sr-only"
          onChange={handleChange}
        />
      </div>

      <div className="flex w-full items-center justify-between">
        <span className="text-body-large font-bold text-gray-900">
          <span className="text-primary-400">{files.length}개</span> / {maxFiles}개
        </span>

        {onClearAll && (
          <FileActionButton icon={<CloseIcon className="size-4" />} onClick={onClearAll}>
            전체 삭제
          </FileActionButton>
        )}
      </div>

      <div className="flex w-full flex-col gap-2">
        {files.map((file) => (
          <FileUploadRow
            key={file.id}
            file={file}
            onRemove={onRemove}
            onDownload={onDownload}
            onPreview={onPreview}
          />
        ))}
      </div>
    </div>
  );
}
