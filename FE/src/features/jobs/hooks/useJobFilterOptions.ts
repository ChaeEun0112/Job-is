import { useQuery } from '@tanstack/react-query';
import { getCareerLevels, getEmploymentTypes, getJobCategories, getRegions } from '@/api/jobs';
import { QUERY_KEYS } from '@/constants/queryKey';

const FILTER_METADATA_STALE_TIME = 60 * 60 * 1000;

export function useJobCategories() {
  return useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.JOB_CATEGORIES(),
    queryFn: getJobCategories,
    staleTime: FILTER_METADATA_STALE_TIME,
  });
}

export function useRegions() {
  return useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.REGIONS(),
    queryFn: getRegions,
    staleTime: FILTER_METADATA_STALE_TIME,
  });
}

export function useEmploymentTypes() {
  return useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.EMPLOYMENT_TYPES(),
    queryFn: getEmploymentTypes,
    staleTime: FILTER_METADATA_STALE_TIME,
  });
}

export function useCareerLevels() {
  return useQuery({
    queryKey: QUERY_KEYS.JOBS.FILTERS.CAREER_LEVELS(),
    queryFn: getCareerLevels,
    staleTime: FILTER_METADATA_STALE_TIME,
  });
}
