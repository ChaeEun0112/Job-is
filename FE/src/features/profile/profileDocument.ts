import { type DocumentUploadError } from '@/features/profile/types/profileDocument';

const MAX_FILE_SIZE = 10 * 1024 * 1024;
const ACCEPTED_FILE_EXTENSIONS = ['pdf', 'docx', 'hwp', 'hwpx'];

export const ACCEPTED_PROFILE_DOCUMENT_TYPES = '.pdf,.docx,.hwp,.hwpx';

export function validateProfileDocument(file: File): DocumentUploadError | null {
  const extension = file.name.split('.').pop()?.toLowerCase();
  const fileSize = `${(file.size / 1024 / 1024).toFixed(1)}MB`;

  if (!extension || !ACCEPTED_FILE_EXTENSIONS.includes(extension)) {
    return {
      fileName: file.name,
      fileSize,
      messages: [
        '등록 가능한 파일 형식이 아닙니다.',
        'PDF, DOCX, HWP, HWPX 파일만 등록할 수 있습니다.',
      ],
    };
  }

  if (file.size > MAX_FILE_SIZE) {
    return {
      fileName: file.name,
      fileSize,
      messages: [
        '등록 가능한 파일 용량을 초과하였습니다.',
        '10MB 미만의 파일만 등록할 수 있습니다.',
      ],
    };
  }

  return null;
}
