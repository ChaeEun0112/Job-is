import { useMutation } from '@tanstack/react-query';
import axios from 'axios';
import { confirmProfileFile, issueProfileFilePresignedUrl } from '@/api/profile';
import type { ProfileFileCategory } from '@/api/types/profile.types';
import type { DocumentType, ProfileDocument } from '@/features/profile/types/profileDocument';
import { formatLocalDateKey } from '@/utils/formatLocalDateKey';

interface UploadProfileDocumentInput {
  type: DocumentType;
  file: File;
  signal: AbortSignal;
  onProgress: (progress: number) => void;
}

const FILE_CATEGORY_BY_TYPE = {
  resume: 'RESUME',
  coverLetter: 'COVER_LETTER',
} satisfies Record<DocumentType, ProfileFileCategory>;

const CONTENT_TYPE_BY_EXTENSION: Record<string, string> = {
  pdf: 'application/pdf',
  docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  hwp: 'application/x-hwp',
  hwpx: 'application/haansofthwpx',
};

function getContentType(file: File) {
  const extension = file.name.split('.').pop()?.toLowerCase();

  return extension ? CONTENT_TYPE_BY_EXTENSION[extension] : undefined;
}

export default function useProfileDocumentUpload() {
  const { mutateAsync: uploadProfileDocument } = useMutation({
    mutationFn: async ({ type, file, signal, onProgress }: UploadProfileDocumentInput) => {
      const category = FILE_CATEGORY_BY_TYPE[type];
      const { presignedUrl, objectKey } = await issueProfileFilePresignedUrl({
        category,
        fileName: file.name,
      });

      await axios.put(presignedUrl, file, {
        signal,
        withCredentials: false,
        headers: { 'Content-Type': getContentType(file) },
        onUploadProgress: (event) => {
          if (!event.total) return;

          onProgress(Math.round((event.loaded / event.total) * 100));
        },
      });

      const { fileId } = await confirmProfileFile({
        category,
        fileName: file.name,
        objectKey,
      });

      return {
        id: String(fileId),
        name: file.name,
        uploadedAt: formatLocalDateKey(new Date()),
      } satisfies ProfileDocument;
    },
  });

  return { uploadProfileDocument };
}
