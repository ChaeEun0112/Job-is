import ProfileSettingsForm, {
  type ProfileSettingsFormValues,
} from '@/features/profile/components/ProfileSettingsForm';
import type { ProfileHydrationSource } from '@/features/profile/hooks/useProfileSettingsForm';

interface OnboardingProfileStepProps {
  onNext: (values: ProfileSettingsFormValues) => void;
  onDocumentsClick: (values: ProfileSettingsFormValues) => void;
  onAptitudeTestClick: (values: ProfileSettingsFormValues) => void;
  submitError?: string;
  isSubmitting?: boolean;
  aptitudeTestCompleted?: boolean;
  draft?: ProfileHydrationSource | null;
}

export default function OnboardingProfileStep({
  onNext,
  onDocumentsClick,
  onAptitudeTestClick,
  submitError,
  isSubmitting,
  aptitudeTestCompleted,
  draft,
}: OnboardingProfileStepProps) {
  return (
    <div className="flex w-full flex-1 items-start justify-center bg-gray-50 px-5 py-16 lg:py-30">
      <ProfileSettingsForm
        title="지금은 IT•개발 직군을 우선 큐레이션해요"
        onSubmit={onNext}
        submitLabel="저장"
        onDocumentsClick={onDocumentsClick}
        onAptitudeTestClick={onAptitudeTestClick}
        loadProfile={false}
        submitError={submitError}
        isSubmitting={isSubmitting}
        aptitudeTestCompleted={aptitudeTestCompleted}
        initialProfile={draft}
      />
    </div>
  );
}
