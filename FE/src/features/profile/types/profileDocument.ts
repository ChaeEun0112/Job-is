export type DocumentType = 'resume' | 'coverLetter';

export interface ProfileDocument {
  id: string;
  name: string;
  uploadedAt: string;
}

export interface DocumentUpload {
  fileName: string;
  progress: number;
}

export interface DocumentUploadError {
  fileName: string;
  fileSize: string;
  messages: [string, string];
}
