import { useMutation, useQueryClient } from '@tanstack/react-query';
import { isCareerLevel, updateProfile } from '@/api/profile';
import type { JobCategory, Region } from '@/api/types/jobs.types';
import { showAlert } from '@/components/feedback';
import { QUERY_KEYS } from '@/constants/queryKey';

interface ProfileSettingsValues {
  regions: string[];
  career: string;
  interests: string[];
  primaryInterest?: string;
  techStacks: string[];
  preferenceNotes: string[];
}

interface UseProfileUpdateOptions {
  jobCategories: JobCategory[];
  regions: Region[];
}

export const PROFILE_UPDATE_VALIDATION_ERROR_MESSAGE =
  '관심 직무, 희망 지역, 경력 단계를 등록된 목록에서 선택해주세요.';

function findByName<T extends { name: string }>(items: T[], name: string) {
  return items.find((item) => item.name === name);
}

export default function useProfileUpdate({ jobCategories, regions }: UseProfileUpdateOptions) {
  const queryClient = useQueryClient();
  const updateMutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: (profile) => {
      queryClient.setQueryData(QUERY_KEYS.PROFILE.BASE(), profile);
      showAlert('success', '프로필을 저장했어요.');
    },
    onError: () => {
      showAlert('danger', '프로필을 저장하지 못했어요. 잠시 후 다시 시도해주세요.');
    },
  });

  const saveProfile = async (values: ProfileSettingsValues) => {
    const selectedCategories = values.interests
      .map((interest) => findByName(jobCategories, interest))
      .filter((category): category is JobCategory => category !== undefined);
    const selectedRegion = findByName(regions, values.regions[0] ?? '');
    const primaryCategory = findByName(
      jobCategories,
      values.primaryInterest ?? values.interests[0] ?? '',
    );

    if (
      values.interests.length === 0 ||
      !selectedRegion ||
      selectedCategories.length !== values.interests.length ||
      !primaryCategory ||
      !isCareerLevel(values.career)
    ) {
      throw new Error(PROFILE_UPDATE_VALIDATION_ERROR_MESSAGE);
    }

    await updateMutation.mutateAsync({
      jobCategoryIds: selectedCategories.map((category) => category.id),
      primaryJobCategoryId: primaryCategory.id,
      regionId: selectedRegion.id,
      careerLevel: values.career,
      preferenceNotes: values.preferenceNotes,
      techStacks: values.techStacks,
    });
  };

  return {
    saveProfile,
    isSavingProfile: updateMutation.isPending,
  };
}
