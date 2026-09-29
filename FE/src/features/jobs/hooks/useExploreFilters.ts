import { useState } from 'react';
import { isJobCareerRange } from '@/api/jobs';
import type { JobSortOption } from '@/api/types/jobs.types';
import { mapJobSummary } from '@/features/jobs/mapJobSummary';
import type { ExploreActiveFilter } from '@/features/jobs/types/exploreJob';
import useDebounce from '@/hooks/useDebounce';
import { formatEmploymentType } from '@/utils/formatEmploymentType';
import { useExploreJobs } from './useExploreJobs';
import {
  useCareerLevels,
  useEmploymentTypes,
  useJobCategories,
  useRegions,
} from './useJobFilterOptions';

const PAGE_SIZE = 24;
const KEYWORD_DEBOUNCE_MS = 300;

export default function useExploreFilters() {
  const [page, setPage] = useState(0);
  const [keywordInput, setKeywordInput] = useState('');
  const debouncedKeyword = useDebounce(keywordInput, KEYWORD_DEBOUNCE_MS);
  const [submittedKeyword, setSubmittedKeyword] = useState<string | null>(null);
  const [selectedJobRoles, setSelectedJobRoles] = useState<string[]>([]);
  const [selectedRegion, setSelectedRegion] = useState('');
  const [selectedCareerLevel, setSelectedCareerLevel] = useState('');
  const [selectedEmploymentType, setSelectedEmploymentType] = useState('');
  const [isRemoteSelected, setIsRemoteSelected] = useState(false);
  const [isAlwaysOpenIncluded, setIsAlwaysOpenIncluded] = useState(true);
  const [sort, setSort] = useState<JobSortOption>('FIT');

  const categoriesQuery = useJobCategories();
  const regionsQuery = useRegions();
  const careerLevelsQuery = useCareerLevels();
  const employmentTypesQuery = useEmploymentTypes();
  const categoryOptions = (categoriesQuery.data ?? []).map((category) => ({
    label: category.name,
    value: category.name,
  }));
  const regionOptions = (regionsQuery.data ?? []).map((region) => ({
    label: region.name,
    value: region.name,
  }));
  const careerLevelOptions = (careerLevelsQuery.data ?? []).map((level) => ({
    label: level.description,
    value: level.key,
  }));
  const employmentTypeOptions = (employmentTypesQuery.data ?? []).map((type) => ({
    label: formatEmploymentType(type),
    value: type,
  }));

  const appliedKeyword = submittedKeyword ?? debouncedKeyword;

  const jobsQuery = useExploreJobs({
    keyword: appliedKeyword || undefined,
    categoryChildren: selectedJobRoles.length > 0 ? selectedJobRoles : undefined,
    cities: selectedRegion ? [selectedRegion] : undefined,
    careerRanges: isJobCareerRange(selectedCareerLevel) ? [selectedCareerLevel] : undefined,
    employmentTypes: selectedEmploymentType ? [selectedEmploymentType] : undefined,
    remoteOnly: isRemoteSelected,
    includeAlwaysOpen: isAlwaysOpenIncluded,
    sort,
    page,
    size: PAGE_SIZE,
  });

  const toggleJobRole = (value: string) => {
    setPage(0);
    setSelectedJobRoles((prev) =>
      prev.includes(value) ? prev.filter((role) => role !== value) : [...prev, value],
    );
  };

  const handleRegionChange = (value: string) => {
    setPage(0);
    setSelectedRegion(value);
  };

  const handleCareerLevelChange = (value: string) => {
    setPage(0);
    setSelectedCareerLevel(value);
  };

  const handleEmploymentTypeChange = (value: string) => {
    setPage(0);
    setSelectedEmploymentType(value);
  };

  const toggleRemote = () => {
    setPage(0);
    setIsRemoteSelected((prev) => !prev);
  };

  const toggleAlwaysOpen = () => {
    setPage(0);
    setIsAlwaysOpenIncluded((prev) => !prev);
  };

  const handleSortChange = (value: JobSortOption) => {
    setPage(0);
    setSort(value);
  };

  const changeKeyword = (keyword: string) => {
    setPage(0);
    setKeywordInput(keyword);
    setSubmittedKeyword(null);
  };

  const submitKeyword = (keyword: string) => {
    setPage(0);
    setKeywordInput(keyword);
    setSubmittedKeyword(keyword);
  };

  const activeFilters: ExploreActiveFilter[] = [
    ...selectedJobRoles.map((role) => ({
      key: role,
      label: role,
      onRemove: () => toggleJobRole(role),
    })),
    ...(selectedRegion
      ? [
          {
            key: selectedRegion,
            label: selectedRegion,
            onRemove: () => handleRegionChange(''),
          },
        ]
      : []),
    ...(selectedCareerLevel
      ? [
          {
            key: selectedCareerLevel,
            label:
              careerLevelOptions.find((option) => option.value === selectedCareerLevel)?.label ??
              selectedCareerLevel,
            onRemove: () => handleCareerLevelChange(''),
          },
        ]
      : []),
    ...(selectedEmploymentType
      ? [
          {
            key: selectedEmploymentType,
            label: formatEmploymentType(selectedEmploymentType),
            onRemove: () => handleEmploymentTypeChange(''),
          },
        ]
      : []),
    ...(isRemoteSelected
      ? [
          {
            key: 'remote',
            label: '원격',
            onRemove: () => setIsRemoteSelected(false),
          },
        ]
      : []),
  ];

  const hasActiveFilters = activeFilters.length > 0;
  const visibleJobs = (jobsQuery.data?.content ?? []).map(mapJobSummary);
  const isSearching = jobsQuery.isLoading;
  const hasResults = visibleJobs.length > 0;
  const totalPages = jobsQuery.data?.totalPages ?? 1;
  const resultCount = jobsQuery.data?.totalElements ?? 0;

  const resetFilters = () => {
    setPage(0);
    setSelectedJobRoles([]);
    setSelectedRegion('');
    setSelectedCareerLevel('');
    setSelectedEmploymentType('');
    setIsRemoteSelected(false);
    setIsAlwaysOpenIncluded(true);
  };

  const goToPreviousPage = () => setPage((previous) => Math.max(0, previous - 1));
  const goToNextPage = () => setPage((previous) => Math.min(totalPages - 1, previous + 1));

  return {
    keywordInput,
    onKeywordChange: changeKeyword,
    onKeywordSubmit: submitKeyword,
    categoryOptions,
    regionOptions,
    careerLevelOptions,
    employmentTypeOptions,
    selectedJobRoles,
    selectedRegion,
    selectedCareerLevel,
    selectedEmploymentType,
    isRemoteSelected,
    isAlwaysOpenIncluded,
    sort,
    toggleJobRole,
    handleRegionChange,
    handleCareerLevelChange,
    handleEmploymentTypeChange,
    toggleRemote,
    toggleAlwaysOpen,
    handleSortChange,
    activeFilters,
    hasActiveFilters,
    resetFilters,
    jobsQuery,
    visibleJobs,
    isSearching,
    hasResults,
    totalPages,
    resultCount,
    page,
    goToPreviousPage,
    goToNextPage,
  };
}
