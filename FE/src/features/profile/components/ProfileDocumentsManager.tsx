import ChevronLeftIcon from '@/assets/icons/icon-chevron-left.svg?react';
import DeleteDocumentModal from '@/features/profile/components/DeleteDocumentModal';
import UploadDropzone from '@/features/profile/components/UploadDropzone';
import useProfileDocumentsManager from '@/features/profile/hooks/useProfileDocumentsManager';
import { type DocumentType } from '@/features/profile/types/profileDocument';

interface ProfileDocumentsManagerProps {
  onBack: () => void;
  backLabel?: string;
}

const DOCUMENT_OPTIONS: { type: DocumentType; title: string }[] = [
  { type: 'resume', title: '이력서' },
  { type: 'coverLetter', title: '자소서' },
];

export default function ProfileDocumentsManager({
  onBack,
  backLabel = '내 프로필로',
}: ProfileDocumentsManagerProps) {
  const {
    displayedDocuments,
    uploads,
    errors,
    deleteTarget,
    deleteError,
    downloadError,
    isDeletingProfileFile,
    downloadingType,
    selectDocument,
    cancelUpload,
    openDeleteDialog,
    closeDeleteDialog,
    confirmDelete,
    clearError,
    downloadDocument,
  } = useProfileDocumentsManager();

  return (
    <>
      <div className="flex w-full max-w-190 flex-col gap-5 rounded-md border border-gray-200 bg-white p-6">
        <button
          type="button"
          onClick={onBack}
          className="inline-flex h-8.75 w-fit cursor-pointer items-center gap-1 rounded-sm border border-primary-400 px-5 text-label-large font-medium text-text-primary transition-colors hover:bg-primary-50"
        >
          <ChevronLeftIcon className="size-6" aria-hidden="true" />
          {backLabel}
        </button>

        <h1 className="text-heading-medium text-text-primary">이력서 • 자소서</h1>

        <p className="flex min-h-18 items-center rounded-xs border border-dashed border-gray-400 bg-gray-200 px-6 text-label-medium font-medium text-text-tertiary">
          추천 고도화에 활용할 예정이에요 · 지금은 본인 보관용
        </p>
        {downloadError && (
          <p className="text-label-small font-medium text-danger-500" role="alert">
            {downloadError}
          </p>
        )}

        <div className="flex flex-col gap-5 border-t border-gray-400 pt-5">
          {DOCUMENT_OPTIONS.map(({ type, title }) => {
            const document = displayedDocuments[type];

            return (
              <UploadDropzone
                key={type}
                title={title}
                document={document}
                upload={uploads[type]}
                uploadError={errors[type]}
                onSelect={(file) => selectDocument(type, file)}
                onDownload={() => {
                  if (document) downloadDocument(type, document);
                }}
                isDownloading={downloadingType === type}
                onDelete={() => openDeleteDialog(type)}
                onCancelUpload={() => cancelUpload(type)}
                onClearError={() => clearError(type)}
              />
            );
          })}
        </div>
      </div>

      {deleteTarget && (
        <DeleteDocumentModal
          documentLabel={deleteTarget === 'resume' ? '이력서' : '자소서'}
          onCancel={closeDeleteDialog}
          onConfirm={confirmDelete}
          isSubmitting={isDeletingProfileFile}
          errorMessage={deleteError}
        />
      )}
    </>
  );
}
