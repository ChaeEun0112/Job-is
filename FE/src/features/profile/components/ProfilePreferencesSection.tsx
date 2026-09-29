import Tag from '@/components/common/Tag';
import ProfileAutocompleteTagInput from '@/features/profile/components/ProfileAutocompleteTagInput';
import ProfileFieldLabel from '@/features/profile/components/ProfileFieldLabel';

interface ProfilePreferencesSectionProps {
  preferenceNote: string;
  techStacks: string[];
  techStackOptions: string[];
  onAddTechStack: (techStack: string) => void;
  onToggleTechStack: (techStack: string) => void;
  onPreferenceNoteChange: (value: string) => void;
}

export default function ProfilePreferencesSection({
  preferenceNote,
  techStacks,
  techStackOptions,
  onAddTechStack,
  onToggleTechStack,
  onPreferenceNoteChange,
}: ProfilePreferencesSectionProps) {
  const visibleTechStacks = [...new Set(techStacks)];

  return (
    <section className="flex flex-col gap-5">
      <div className="flex flex-col gap-2">
        <ProfileFieldLabel status="선택">선호 조건</ProfileFieldLabel>
        <input
          type="text"
          value={preferenceNote}
          placeholder="정규직 우선 · 재택 가능한 곳이면 좋겠어요…"
          onChange={(event) => onPreferenceNoteChange(event.target.value)}
          className="h-10 w-full rounded-[6px] border border-gray-700 bg-white px-4 text-label-medium font-medium text-text-secondary outline-none placeholder:text-gray-600 focus:border-primary-500"
        />
      </div>

      <div className="flex flex-col gap-2">
        <ProfileFieldLabel status="선택">기술 스택</ProfileFieldLabel>
        <div className="flex flex-wrap gap-2">
          {visibleTechStacks.map((techStack) => (
            <Tag
              key={techStack}
              variant="removable"
              label={techStack}
              onClick={() => onToggleTechStack(techStack)}
              className="h-10 px-3 text-label-large"
            />
          ))}
          <ProfileAutocompleteTagInput
            options={techStackOptions}
            selectedValues={techStacks}
            onAdd={onAddTechStack}
            placeholder="기술 검색"
            ariaLabel="기술 스택 검색"
          />
        </div>
      </div>
    </section>
  );
}
