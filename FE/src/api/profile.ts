import { api } from '@/api/base/request';
import {
  CAREER_LEVELS,
  type CareerLevel,
  type ConfirmProfileFileRequest,
  type ConfirmProfileFileResponse,
  type PresignedUrlRequest,
  type PresignedUrlResponse,
  type ProfileDraftRequest,
  type ProfileDraftResponse,
  type ProfileFile,
  type ProfileFileDownloadUrlResponse,
  type ProfileResponse,
  type ProfileUpdateRequest,
} from './types/profile.types';

export function isCareerLevel(value: string): value is CareerLevel {
  return CAREER_LEVELS.includes(value as CareerLevel);
}

export async function getProfile() {
  return api.get<ProfileResponse>('/api/profile');
}

export async function updateProfile(request: ProfileUpdateRequest) {
  return api.patch<ProfileResponse>('/api/profile', request);
}

export async function getProfileDraft() {
  return api.get<ProfileDraftResponse>('/api/profile/draft');
}

export async function saveProfileDraft(request: ProfileDraftRequest) {
  return api.put<ProfileDraftResponse>('/api/profile/draft', request);
}

export async function completeOnboarding() {
  return api.post<string>('/api/profile/onboarding/complete');
}

export async function getProfileFiles() {
  return api.get<ProfileFile[]>('/api/profile/files');
}

export async function issueProfileFilePresignedUrl(request: PresignedUrlRequest) {
  return api.post<PresignedUrlResponse>('/api/profile/files/presigned-url', request);
}

export async function confirmProfileFile(request: ConfirmProfileFileRequest) {
  return api.post<ConfirmProfileFileResponse>('/api/profile/files', request);
}

export async function deleteProfileFile(fileId: number) {
  return api.delete<string>(`/api/profile/files/${fileId}`);
}

export async function getProfileFileDownloadUrl(fileId: number) {
  return api.get<ProfileFileDownloadUrlResponse>(`/api/profile/files/${fileId}/download-url`);
}
