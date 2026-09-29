import { useMemo, useRef, useState } from 'react';
import useProfileDocumentActions from '@/features/profile/hooks/useProfileDocumentActions';
import useProfileDocumentUpload from '@/features/profile/hooks/useProfileDocumentUpload';
import useProfileFiles from '@/features/profile/hooks/useProfileFiles';
import { validateProfileDocument } from '@/features/profile/profileDocument';
import {
  type DocumentType,
  type DocumentUpload,
  type DocumentUploadError,
  type ProfileDocument,
} from '@/features/profile/types/profileDocument';
import { formatLocalDateKey } from '@/utils/formatLocalDateKey';

export default function useProfileDocumentsManager() {
  const [documents, setDocuments] = useState<Partial<Record<DocumentType, ProfileDocument | null>>>(
    {},
  );
  const [uploads, setUploads] = useState<Partial<Record<DocumentType, DocumentUpload>>>({});
  const [errors, setErrors] = useState<Partial<Record<DocumentType, DocumentUploadError>>>({});
  const [deleteTarget, setDeleteTarget] = useState<DocumentType | null>(null);
  const [deleteError, setDeleteError] = useState<string>();
  const [downloadError, setDownloadError] = useState<string>();
  const [downloadingType, setDownloadingType] = useState<DocumentType | null>(null);
  const uploadControllersRef = useRef<Partial<Record<DocumentType, AbortController>>>({});
  const { uploadProfileDocument } = useProfileDocumentUpload();
  const { profileFiles, refetchProfileFiles } = useProfileFiles();
  const { deleteProfileFile, isDeletingProfileFile, downloadProfileFile } =
    useProfileDocumentActions();

  const loadedDocuments = useMemo<Record<DocumentType, ProfileDocument | null>>(() => {
    const nextDocuments: Record<DocumentType, ProfileDocument | null> = {
      resume: null,
      coverLetter: null,
    };

    profileFiles.forEach((file) => {
      const type = file.category === 'RESUME' ? 'resume' : 'coverLetter';
      nextDocuments[type] = {
        id: String(file.fileId),
        name: file.fileName,
        uploadedAt: formatLocalDateKey(new Date(file.uploadedAt)),
      };
    });

    return nextDocuments;
  }, [profileFiles]);

  const displayedDocuments = useMemo(
    () => ({ ...loadedDocuments, ...documents }),
    [documents, loadedDocuments],
  );

  const clearLocalDocument = (type: DocumentType) => {
    setDocuments((previous) => {
      const { [type]: _document, ...remainingDocuments } = previous;
      return remainingDocuments;
    });
  };

  const syncDocuments = (type: DocumentType) => {
    void refetchProfileFiles().then((result) => {
      if (!result.isError) clearLocalDocument(type);
    });
  };

  const cancelUpload = (type: DocumentType) => {
    uploadControllersRef.current[type]?.abort();
    delete uploadControllersRef.current[type];
    setUploads((previous) => ({ ...previous, [type]: undefined }));
  };

  const selectDocument = (type: DocumentType, file: File) => {
    const uploadError = validateProfileDocument(file);

    if (uploadError) {
      setErrors((previous) => ({ ...previous, [type]: uploadError }));
      return;
    }

    setErrors((previous) => ({ ...previous, [type]: undefined }));
    cancelUpload(type);

    setUploads((previous) => ({ ...previous, [type]: { fileName: file.name, progress: 0 } }));

    const controller = new AbortController();
    uploadControllersRef.current[type] = controller;

    void uploadProfileDocument({
      type,
      file,
      signal: controller.signal,
      onProgress: (progress) => {
        setUploads((previous) => ({
          ...previous,
          [type]: { fileName: file.name, progress },
        }));
      },
    })
      .then((document) => {
        setDocuments((previous) => ({ ...previous, [type]: document }));
        syncDocuments(type);
      })
      .catch(() => {
        if (controller.signal.aborted) return;

        setErrors((previous) => ({
          ...previous,
          [type]: {
            fileName: file.name,
            fileSize: `${(file.size / 1024 / 1024).toFixed(1)}MB`,
            messages: ['파일 업로드에 실패했어요.', '잠시 후 다시 시도해주세요.'],
          },
        }));
      })
      .finally(() => {
        if (uploadControllersRef.current[type] !== controller) return;

        delete uploadControllersRef.current[type];
        setUploads((previous) => ({ ...previous, [type]: undefined }));
      });
  };

  const openDeleteDialog = (type: DocumentType) => {
    setDeleteError(undefined);
    setDeleteTarget(type);
  };

  const closeDeleteDialog = () => {
    if (isDeletingProfileFile) return;

    setDeleteError(undefined);
    setDeleteTarget(null);
  };

  const confirmDelete = () => {
    if (!deleteTarget || isDeletingProfileFile) return;

    const document = displayedDocuments[deleteTarget];
    if (!document) return;

    void deleteProfileFile(Number(document.id))
      .then(() => {
        setDocuments((previous) => ({ ...previous, [deleteTarget]: null }));
        setErrors((previous) => ({ ...previous, [deleteTarget]: undefined }));
        setDeleteTarget(null);
        syncDocuments(deleteTarget);
      })
      .catch(() => {
        setDeleteError('파일을 삭제하지 못했어요. 잠시 후 다시 시도해주세요.');
      });
  };

  const clearError = (type: DocumentType) => {
    setErrors((previous) => ({ ...previous, [type]: undefined }));
  };

  const downloadDocument = (type: DocumentType, document: ProfileDocument) => {
    setDownloadError(undefined);
    setDownloadingType(type);
    void downloadProfileFile(Number(document.id))
      .catch(() => {
        setDownloadError('파일을 다운로드하지 못했어요. 잠시 후 다시 시도해주세요.');
      })
      .finally(() => {
        setDownloadingType((previous) => (previous === type ? null : previous));
      });
  };

  return {
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
  };
}
