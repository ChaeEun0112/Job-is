import { Button, NoticePanel } from '@/components/common';
import Pagination from '@/components/common/Pagination';
import ExploreEmptyResults from '@/features/jobs/components/ExploreEmptyResults';
import ExploreFilters from '@/features/jobs/components/ExploreFilters';
import ExploreJobGrid from '@/features/jobs/components/ExploreJobGrid';
import ExploreJobGridSkeleton from '@/features/jobs/components/ExploreJobGridSkeleton';
import ExploreResultsToolbar from '@/features/jobs/components/ExploreResultsToolbar';
import useExploreFilters from '@/features/jobs/hooks/useExploreFilters';

export default function ExplorePage() {
  const {
    keywordInput,
    onKeywordChange,
    onKeywordSubmit,
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
  } = useExploreFilters();

  return (
    <div className="flex min-h-0 w-full flex-1 justify-center bg-gray-50 px-3 py-8">
      <div className="flex w-full max-w-300 flex-1 flex-col gap-6">
        <div className="flex flex-col gap-1">
          <h1 className="text-heading-medium font-bold text-text-primary">탐색</h1>
          <p className="text-label-medium font-medium text-text-secondary">
            추천이 메인, 탐색은 직접 찾고 싶을 때의 보조 동선
          </p>
        </div>
        <ExploreFilters
          keyword={keywordInput}
          onKeywordChange={onKeywordChange}
          onKeywordSubmit={onKeywordSubmit}
          categoryOptions={categoryOptions}
          selectedJobRoles={selectedJobRoles}
          onToggleJobRole={toggleJobRole}
          regionOptions={regionOptions}
          selectedRegion={selectedRegion}
          onRegionChange={handleRegionChange}
          careerLevelOptions={careerLevelOptions}
          selectedCareerLevel={selectedCareerLevel}
          onCareerLevelChange={handleCareerLevelChange}
          employmentTypeOptions={employmentTypeOptions}
          selectedEmploymentType={selectedEmploymentType}
          onEmploymentTypeChange={handleEmploymentTypeChange}
          isRemoteSelected={isRemoteSelected}
          onToggleRemote={toggleRemote}
          isAlwaysOpenIncluded={isAlwaysOpenIncluded}
          onToggleAlwaysOpen={toggleAlwaysOpen}
        />
        <ExploreResultsToolbar
          resultCount={resultCount}
          activeFilters={activeFilters}
          onReset={resetFilters}
          isLoading={isSearching}
          sort={sort}
          onSortChange={handleSortChange}
        />
        {isSearching ? (
          <ExploreJobGridSkeleton />
        ) : jobsQuery.isError ? (
          <div className="flex min-h-75 w-full flex-1">
            <NoticePanel
              resultIconVariant="danger"
              title="공고를 불러오지 못했어요"
              className="h-full max-w-none justify-center"
            >
              <Button onClick={() => jobsQuery.refetch()}>다시 시도</Button>
            </NoticePanel>
          </div>
        ) : hasResults ? (
          <>
            <ExploreJobGrid jobs={visibleJobs} />
            <Pagination
              currentPage={page + 1}
              totalPages={totalPages}
              label={`${page + 1}/${totalPages}`}
              onPrevious={goToPreviousPage}
              onNext={goToNextPage}
            />
          </>
        ) : (
          <ExploreEmptyResults
            activeFilters={hasActiveFilters ? activeFilters : []}
            onResetFilters={resetFilters}
          />
        )}
      </div>
    </div>
  );
}
