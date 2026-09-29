import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import {
  completeOnboarding,
  getProfileDraft,
  getProfileFiles,
  isCareerLevel,
  saveProfileDraft,
} from '@/api/profile';
import type { JobCategory, Region } from '@/api/types/jobs.types';
import { QUERY_KEYS } from '@/constants/queryKey';
import { setOnboardingCompleted } from '@/store/useAuthStore';

interface OnboardingProfileValues {
  regions: string[];
  career: string;
  interests: string[];
  techStacks: string[];
  preferenceNotes: string[];
}

interface UseOnboardingFlowOptions {
  jobCategories: JobCategory[];
  regions: Region[];
  isMetadataPending: boolean;
  onProfileSaved: () => void;
}

const VALIDATION_MESSAGES = {
  metadataPending: '선택 목록을 불러오는 중이에요. 잠시 후 다시 시도해주세요.',
  required: '관심 직무, 희망 지역, 경력 단계를 모두 선택해주세요.',
  invalidMetadata: '등록된 목록에서 관심 직무와 희망 지역을 선택해주세요.',
  invalidCareer: '등록된 경력 단계를 선택해주세요.',
} as const;

function normalizeMetadataName(value: string) {
  return value.replace(/[^\p{L}\p{N}]/gu, '').toLowerCase();
}

function findMetadataByName<T extends { name: string }>(items: T[], value: string) {
  const normalizedValue = normalizeMetadataName(value);
  return items.find((item) => normalizeMetadataName(item.name) === normalizedValue);
}

export default function useOnboardingFlow({
  jobCategories,
  regions,
  isMetadataPending,
  onProfileSaved,
}: UseOnboardingFlowOptions) {
  const navigate = useNavigate();
  const draftQuery = useQuery({
    queryKey: QUERY_KEYS.PROFILE.DRAFT(),
    queryFn: getProfileDraft,
  });
  const [resumeName, setResumeName] = useState<string>();
  const [saveError, setSaveError] = useState<string>();
  const [isSaving, setIsSaving] = useState(false);
  const [completeError, setCompleteError] = useState<string>();
  const [isCompleting, setIsCompleting] = useState(false);

  const saveProfile = async (values: OnboardingProfileValues) => {
    setSaveError(undefined);

    if (isMetadataPending) {
      setSaveError(VALIDATION_MESSAGES.metadataPending);
      return;
    }

    if (values.interests.length === 0 || values.regions.length === 0 || !values.career) {
      setSaveError(VALIDATION_MESSAGES.required);
      return;
    }

    const selectedCategories = values.interests
      .map((interest) => findMetadataByName(jobCategories, interest))
      .filter((category): category is JobCategory => category !== undefined);
    const selectedRegion = findMetadataByName(regions, values.regions[0] ?? '');

    if (selectedCategories.length !== values.interests.length || !selectedRegion) {
      setSaveError(VALIDATION_MESSAGES.invalidMetadata);
      return;
    }

    if (!isCareerLevel(values.career)) {
      setSaveError(VALIDATION_MESSAGES.invalidCareer);
      return;
    }

    setIsSaving(true);

    try {
      await saveProfileDraft({
        onboardingStep: 'PROFILE',
        jobCategoryIds: selectedCategories.map((category) => category.id),
        primaryJobCategoryId: selectedCategories[0]?.id,
        regionId: selectedRegion.id,
        careerLevel: values.career,
        preferenceNotes: values.preferenceNotes,
        excludeKeywords: [],
        techStacks: values.techStacks,
      });

      const [, profileFiles] = await Promise.all([
        draftQuery.refetch(),
        getProfileFiles().catch(() => []),
      ]);
      setResumeName(profileFiles.find((file) => file.category === 'RESUME')?.fileName);
      onProfileSaved();
    } catch {
      setSaveError('임시저장에 실패했어요. 잠시 후 다시 시도해주세요.');
    } finally {
      setIsSaving(false);
    }
  };

  const saveDraftSnapshot = (values: OnboardingProfileValues) => {
    const selectedCategories = values.interests
      .map((interest) => findMetadataByName(jobCategories, interest))
      .filter((category): category is JobCategory => category !== undefined);
    const selectedRegion = findMetadataByName(regions, values.regions[0] ?? '');

    saveProfileDraft({
      onboardingStep: 'PROFILE',
      jobCategoryIds: selectedCategories.map((category) => category.id),
      primaryJobCategoryId: selectedCategories[0]?.id,
      regionId: selectedRegion?.id,
      careerLevel: isCareerLevel(values.career) ? values.career : undefined,
      preferenceNotes: values.preferenceNotes,
      excludeKeywords: [],
      techStacks: values.techStacks,
    })
      .then(() => draftQuery.refetch())
      .catch(() => {});
  };

  const complete = async () => {
    setCompleteError(undefined);
    setIsCompleting(true);

    try {
      await saveProfileDraft({ onboardingStep: 'REVIEW' });
      await completeOnboarding();
      setOnboardingCompleted(true);
      navigate('/recommendations', { replace: true });
    } catch {
      setCompleteError('온보딩 완료 처리에 실패했어요. 잠시 후 다시 시도해주세요.');
      setIsCompleting(false);
    }
  };

  return {
    draft: draftQuery.data ?? null,
    resumeName,
    saveError,
    isSaving,
    completeError,
    isCompleting,
    saveProfile,
    saveDraftSnapshot,
    complete,
  };
}
