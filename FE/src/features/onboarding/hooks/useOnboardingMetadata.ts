import { useQuery } from '@tanstack/react-query';
import { getJobCategories, getRegions } from '@/api/jobs';
import { QUERY_KEYS } from '@/constants/queryKey';

export default function useOnboardingMetadata() {
  const jobCategoriesQuery = useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.JOB_CATEGORIES(),
    queryFn: getJobCategories,
  });
  const regionsQuery = useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.REGIONS(),
    queryFn: getRegions,
  });

  return {
    jobCategories: jobCategoriesQuery.data ?? [],
    regions: regionsQuery.data ?? [],
    isPending: jobCategoriesQuery.isPending || regionsQuery.isPending,
  };
}
