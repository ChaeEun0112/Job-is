export type ProfileFileCategory = 'RESUME' | 'COVER_LETTER';

export interface PresignedUrlRequest {
  category: ProfileFileCategory;
  fileName: string;
}

export interface PresignedUrlResponse {
  presignedUrl: string;
  objectKey: string;
  expiresIn: number;
}

export interface ConfirmProfileFileRequest {
  category: ProfileFileCategory;
  fileName: string;
  objectKey: string;
}

export interface ConfirmProfileFileResponse {
  fileId: number;
  created: boolean;
}

export interface ProfileFileDownloadUrlResponse {
  downloadUrl: string;
  fileName: string;
  expiresIn: number;
}

export interface ProfileFile {
  fileId: number;
  category: ProfileFileCategory;
  fileName: string;
  fileFormat: string;
  uploadedAt: string;
}

export const CAREER_LEVELS = ['ENTRY', 'JUNIOR', 'EXPERIENCED'] as const;

export type CareerLevel = (typeof CAREER_LEVELS)[number];
export type OnboardingStep = 'PROFILE' | 'QUIZ' | 'REVIEW';

export interface ProfileResponse {
  jobCategories: Array<{ id: number; name: string; primary: boolean }> | null;
  region: { id: number; name: string } | null;
  careerLevel: CareerLevel | null;
  preferenceNotes: string[] | null;
  excludeKeywords: string[] | null;
  techStacks: string[] | null;
  personalityTags: string[];
  jobTestCompleted: boolean;
}

export interface ProfileDraftResponse {
  onboardingStep: OnboardingStep;
  jobCategories: Array<{ id: number; name: string; primary: boolean }> | null;
  region: { id: number; name: string } | null;
  careerLevel: CareerLevel | null;
  preferenceNotes: string[] | null;
  excludeKeywords: string[] | null;
  techStacks: string[] | null;
  personalityTags: string[] | null;
  jobTestCompleted: boolean;
}

export interface ProfileDraftRequest {
  onboardingStep?: OnboardingStep;
  jobCategoryIds?: number[];
  primaryJobCategoryId?: number;
  regionId?: number;
  careerLevel?: CareerLevel;
  preferenceNotes?: string[];
  excludeKeywords?: string[];
  techStacks?: string[];
}

export interface ProfileUpdateRequest {
  jobCategoryIds?: number[];
  primaryJobCategoryId?: number;
  regionId?: number;
  careerLevel?: CareerLevel;
  preferenceNotes?: string[];
  excludeKeywords?: string[];
  techStacks?: string[];
}
