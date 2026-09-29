import { Dropdown, MultiSelect, Tag } from '@/components/common';
import type { MultiSelectOption } from '@/components/common/MultiSelect';
import Search from '@/components/common/Search';

interface ExploreFiltersProps {
  keyword: string;
  onKeywordChange: (keyword: string) => void;
  onKeywordSubmit: (keyword: string) => void;
  categoryOptions: MultiSelectOption[];
  selectedJobRoles: string[];
  onToggleJobRole: (value: string) => void;
  regionOptions: MultiSelectOption[];
  selectedRegion: string;
  onRegionChange: (value: string) => void;
  careerLevelOptions: MultiSelectOption[];
  selectedCareerLevel: string;
  onCareerLevelChange: (value: string) => void;
  employmentTypeOptions: MultiSelectOption[];
  selectedEmploymentType: string;
  onEmploymentTypeChange: (value: string) => void;
  isRemoteSelected: boolean;
  onToggleRemote: () => void;
  isAlwaysOpenIncluded: boolean;
  onToggleAlwaysOpen: () => void;
}

export default function ExploreFilters({
  keyword,
  onKeywordChange,
  onKeywordSubmit,
  categoryOptions,
  selectedJobRoles,
  onToggleJobRole,
  regionOptions,
  selectedRegion,
  onRegionChange,
  careerLevelOptions,
  selectedCareerLevel,
  onCareerLevelChange,
  employmentTypeOptions,
  selectedEmploymentType,
  onEmploymentTypeChange,
  isRemoteSelected,
  onToggleRemote,
  isAlwaysOpenIncluded,
  onToggleAlwaysOpen,
}: ExploreFiltersProps) {
  return (
    <div className="flex w-full flex-col gap-3">
      <Search
        placeholder="직무 · 회사 · 키워드"
        className="w-206.75"
        value={keyword}
        onChange={(event) => onKeywordChange(event.target.value)}
        onSearchSubmit={onKeywordSubmit}
      />
      <div className="flex w-full flex-wrap items-center gap-2">
        <MultiSelect
          placeholder="세부 직군"
          options={categoryOptions}
          selectedValues={selectedJobRoles}
          onToggle={onToggleJobRole}
          className="w-50 shrink-0"
        />
        <Dropdown
          placeholder="지역"
          className="w-50 shrink-0"
          options={regionOptions}
          value={selectedRegion}
          onChange={onRegionChange}
        />
        <Dropdown
          placeholder="경력"
          className="w-50 shrink-0"
          options={careerLevelOptions}
          value={selectedCareerLevel}
          onChange={onCareerLevelChange}
        />
        <Dropdown
          placeholder="고용 형태"
          className="w-50 shrink-0"
          options={employmentTypeOptions}
          value={selectedEmploymentType}
          onChange={onEmploymentTypeChange}
        />
        <Tag variant="select" label="원격" selected={isRemoteSelected} onClick={onToggleRemote} />
        <Tag
          variant="select"
          label="상시포함"
          selected={isAlwaysOpenIncluded}
          onClick={onToggleAlwaysOpen}
        />
      </div>
    </div>
  );
}
