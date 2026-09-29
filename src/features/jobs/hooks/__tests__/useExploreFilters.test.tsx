import { afterEach, beforeAll, beforeEach, describe, expect, it, jest } from '@jest/globals';
import { act, renderHook } from '@testing-library/react';
import type { SearchJobsParams } from '@/api/types/jobs.types';
import type useExploreFiltersHook from '../useExploreFilters';
import type * as ExploreJobsModule from '../useExploreJobs';
import type * as JobFilterOptionsModule from '../useJobFilterOptions';

jest.mock('@/api/jobs', () => ({
  isJobCareerRange: (value: string) => ['NEWCOMER', 'JUNIOR', 'SENIOR'].includes(value),
}));

jest.mock('../useExploreJobs', () => ({
  useExploreJobs: jest.fn(),
}));

jest.mock('../useJobFilterOptions', () => ({
  useCareerLevels: jest.fn(),
  useEmploymentTypes: jest.fn(),
  useJobCategories: jest.fn(),
  useRegions: jest.fn(),
}));

const mockedUseExploreJobs = jest.mocked(
  (jest.requireMock('../useExploreJobs') as typeof ExploreJobsModule).useExploreJobs,
);
const mockedUseJobCategories = jest.mocked(
  (jest.requireMock('../useJobFilterOptions') as typeof JobFilterOptionsModule).useJobCategories,
);
const mockedUseRegions = jest.mocked(
  (jest.requireMock('../useJobFilterOptions') as typeof JobFilterOptionsModule).useRegions,
);
const mockedUseCareerLevels = jest.mocked(
  (jest.requireMock('../useJobFilterOptions') as typeof JobFilterOptionsModule).useCareerLevels,
);
const mockedUseEmploymentTypes = jest.mocked(
  (jest.requireMock('../useJobFilterOptions') as typeof JobFilterOptionsModule).useEmploymentTypes,
);

let useExploreFilters: typeof useExploreFiltersHook;

function getLastSearchParams(): SearchJobsParams {
  const params = mockedUseExploreJobs.mock.calls.at(-1)?.[0];

  if (!params) {
    throw new Error('탐색 공고 조회 파라미터가 없습니다.');
  }

  return params;
}

describe('useExploreFilters', () => {
  beforeAll(async () => {
    ({ default: useExploreFilters } = await import('../useExploreFilters'));
  });

  beforeEach(() => {
    jest.useFakeTimers();
    mockedUseExploreJobs.mockReturnValue({
      data: { content: [], page: 0, size: 24, totalElements: 0, totalPages: 2, isLast: false },
      isLoading: false,
      isPlaceholderData: false,
      isFetching: false,
    } as never);
    mockedUseJobCategories.mockReturnValue({ data: [] } as never);
    mockedUseRegions.mockReturnValue({ data: [] } as never);
    mockedUseCareerLevels.mockReturnValue({ data: [] } as never);
    mockedUseEmploymentTypes.mockReturnValue({ data: [] } as never);
  });

  afterEach(() => {
    jest.useRealTimers();
    jest.clearAllMocks();
  });

  it('입력 중에는 디바운스 후 키워드 검색을 적용한다', () => {
    const { result } = renderHook(() => useExploreFilters());

    act(() => result.current.onKeywordChange('프론트'));
    expect(getLastSearchParams().keyword).toBeUndefined();

    act(() => jest.advanceTimersByTime(300));
    expect(getLastSearchParams()).toMatchObject({ keyword: '프론트', page: 0 });
  });

  it('검색 제출은 디바운스를 기다리지 않고 첫 페이지부터 조회한다', () => {
    const { result } = renderHook(() => useExploreFilters());

    act(() => result.current.goToNextPage());
    expect(getLastSearchParams().page).toBe(1);

    act(() => result.current.onKeywordSubmit('백엔드'));
    expect(getLastSearchParams()).toMatchObject({ keyword: '백엔드', page: 0 });
  });

  it('선택한 세부 직군을 categoryChildren 검색 파라미터로 전달한다', () => {
    const { result } = renderHook(() => useExploreFilters());

    act(() => result.current.toggleJobRole('백엔드 개발자'));

    expect(getLastSearchParams()).toMatchObject({
      categoryChildren: ['백엔드 개발자'],
      page: 0,
    });
  });

  it('고용 형태는 한글 라벨로 표시하고 API 값으로 검색한다', () => {
    mockedUseEmploymentTypes.mockReturnValue({ data: ['regular', 'contract', 'intern'] } as never);
    const { result } = renderHook(() => useExploreFilters());

    expect(result.current.employmentTypeOptions).toEqual([
      { label: '정규직', value: 'regular' },
      { label: '계약직', value: 'contract' },
      { label: '인턴', value: 'intern' },
    ]);

    act(() => result.current.handleEmploymentTypeChange('contract'));

    expect(getLastSearchParams()).toMatchObject({ employmentTypes: ['contract'], page: 0 });
    expect(result.current.activeFilters).toContainEqual(
      expect.objectContaining({ key: 'contract', label: '계약직' }),
    );
  });
});
