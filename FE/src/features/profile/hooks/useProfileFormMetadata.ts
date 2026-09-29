import { useQuery } from '@tanstack/react-query';
import { getCareerLevels, getJobCategories, getRegions, getTechStacks } from '@/api/jobs';
import { getProfile } from '@/api/profile';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useProfileFormMetadata(loadProfile: boolean) {
  const techStacksQuery = useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.TECH_STACKS(),
    queryFn: getTechStacks,
  });
  const jobCategoriesQuery = useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.JOB_CATEGORIES(),
    queryFn: getJobCategories,
  });
  const regionsQuery = useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.REGIONS(),
    queryFn: getRegions,
  });
  const careerLevelsQuery = useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.CAREER_LEVELS(),
    queryFn: getCareerLevels,
  });
  const profileQuery = useQuery({
    queryKey: QUERY_KEYS.PROFILE.BASE(),
    queryFn: getProfile,
    enabled: loadProfile,
  });

  return {
    techStackMetadata: techStacksQuery.data ?? [],
    jobCategoryMetadata: jobCategoriesQuery.data ?? [],
    regionMetadata: regionsQuery.data ?? [],
    careerLevelMetadata: careerLevelsQuery.data ?? [],
    profile: profileQuery.data,
    isProfilePending: loadProfile && profileQuery.isPending,
  };
}
