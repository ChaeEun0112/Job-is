import Tag from '@/components/common/Tag';
import ProfileFieldLabel from '@/features/profile/components/ProfileFieldLabel';

interface ProfileLifestyleSectionProps {
  tags: string[];
}

export default function ProfileLifestyleSection({ tags }: ProfileLifestyleSectionProps) {
  return (
    <section className="border-b border-gray-400 pb-5">
      <ProfileFieldLabel status="읽기 전용">성향 결과 태그</ProfileFieldLabel>
      <div className="mt-5 flex flex-wrap gap-2">
        {tags.map((tag) => (
          <Tag key={tag} variant="plain" label={tag} className="h-10 px-3 text-label-large" />
        ))}
      </div>
    </section>
  );
}
