import Tag from '@/components/common/Tag';
import ProfileAutocompleteTagInput from '@/features/profile/components/ProfileAutocompleteTagInput';
import ProfileFieldLabel from '@/features/profile/components/ProfileFieldLabel';
import { cn } from '@/utils/cn';

const DEFAULT_VISIBLE_INTEREST_COUNT = 4;

interface ProfileBasicInfoSectionProps {
  interests: string[];
  interestOptions: string[];
  regions: string[];
  regionOptions: string[];
  careerOptions: Array<{ key: string; description: string }>;
  career: string;
  onToggleInterest: (interest: string) => void;
  onToggleRegion: (region: string) => void;
  onAddRegion: (region: string) => void;
  onCareerChange: (career: string) => void;
}

export default function ProfileBasicInfoSection({
  interests,
  interestOptions,
  regions,
  regionOptions,
  careerOptions,
  career,
  onToggleInterest,
  onToggleRegion,
  onAddRegion,
  onCareerChange,
}: ProfileBasicInfoSectionProps) {
  const visibleInterestOptions = [
    ...new Set([...interestOptions.slice(0, DEFAULT_VISIBLE_INTEREST_COUNT), ...interests]),
  ];

  return (
    <section className="flex flex-col gap-5">
      <div className="flex flex-col gap-2">
        <ProfileFieldLabel status="필수">관심 분야 • 직군</ProfileFieldLabel>
        <div className="flex flex-wrap gap-2">
          {visibleInterestOptions.map((option) => (
            <Tag
              key={option}
              variant={interests.includes(option) ? 'removable' : 'select'}
              label={option}
              className={cn(
                'h-10 px-3 text-label-large',
                interests.includes(option) && 'border-primary-600 bg-primary-600 text-text-primary',
              )}
              onClick={() => onToggleInterest(option)}
              aria-pressed={interests.includes(option)}
            />
          ))}
          <ProfileAutocompleteTagInput
            options={interestOptions}
            selectedValues={interests}
            onAdd={onToggleInterest}
            placeholder="직무 검색"
            ariaLabel="관심 분야·직군 검색"
          />
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <ProfileFieldLabel status="필수">희망 지역</ProfileFieldLabel>
        <div className="flex flex-wrap gap-2">
          {regions.map((region) => (
            <Tag
              key={region}
              variant="removable"
              label={region}
              className="h-10 border-primary-600 bg-primary-600 px-3 text-label-large text-text-primary"
              onClick={() => onToggleRegion(region)}
              aria-pressed="true"
            />
          ))}
          <ProfileAutocompleteTagInput
            options={regionOptions}
            selectedValues={regions}
            onAdd={onAddRegion}
            placeholder="지역 검색"
            ariaLabel="희망 지역 검색"
          />
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <ProfileFieldLabel status="필수">경력 단계</ProfileFieldLabel>
        <div className="flex flex-wrap gap-2">
          {careerOptions.map((option) => (
            <Tag
              key={option.key}
              variant="select"
              label={option.description}
              selected={career === option.key}
              className={cn(
                'h-10 px-3 text-label-large',
                career === option.key && 'border-primary-600 bg-primary-600 text-text-primary',
              )}
              onClick={() => onCareerChange(option.key)}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
