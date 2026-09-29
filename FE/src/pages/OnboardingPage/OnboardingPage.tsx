import { useEffect, useState } from 'react';
import { Navigate, useNavigate, useOutletContext } from 'react-router';
import type { MainLayoutOutletContext } from '@/components/layout/MainLayout';
import OnboardingConfirmStep from '@/features/onboarding/components/OnboardingConfirmStep';
import OnboardingProfileStep from '@/features/onboarding/components/OnboardingProfileStep';
import useOnboardingFlow from '@/features/onboarding/hooks/useOnboardingFlow';
import useOnboardingMetadata from '@/features/onboarding/hooks/useOnboardingMetadata';
import { useAuthStore } from '@/store/useAuthStore';
import { cn } from '@/utils/cn';

type OnboardingStep = 'profile' | 'confirm';
type StepTransitionDirection = 'forward' | 'backward';
export default function OnboardingPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState<OnboardingStep>('profile');
  const [transitionDirection, setTransitionDirection] =
    useState<StepTransitionDirection>('forward');
  const [shouldAnimateStep, setShouldAnimateStep] = useState(false);
  const { setCarouselActiveIndex } = useOutletContext<MainLayoutOutletContext>();
  const onboardingCompleted = useAuthStore((state) => state.onboardingCompleted);
  const { jobCategories, regions, isPending: isMetadataPending } = useOnboardingMetadata();

  useEffect(() => {
    const carouselActiveIndex = { profile: 0, confirm: 1 }[step];
    setCarouselActiveIndex(carouselActiveIndex);
  }, [setCarouselActiveIndex, step]);

  const changeStep = (nextStep: OnboardingStep, direction: StepTransitionDirection) => {
    setTransitionDirection(direction);
    setShouldAnimateStep(true);
    setStep(nextStep);
  };
  const {
    draft,
    resumeName,
    saveError,
    isSaving,
    completeError,
    isCompleting,
    saveProfile,
    saveDraftSnapshot,
    complete,
  } = useOnboardingFlow({
    jobCategories,
    regions,
    isMetadataPending,
    onProfileSaved: () => changeStep('confirm', 'forward'),
  });

  if (onboardingCompleted) {
    return <Navigate to="/recommendations" replace />;
  }

  let stepContent;

  if (step === 'profile') {
    stepContent = (
      <OnboardingProfileStep
        onDocumentsClick={(values) => {
          saveDraftSnapshot(values);
          navigate('/onboarding/documents');
        }}
        onAptitudeTestClick={(values) => {
          saveDraftSnapshot(values);
          navigate('/onboarding/aptitude-test');
        }}
        submitError={saveError}
        isSubmitting={isSaving}
        onNext={saveProfile}
        aptitudeTestCompleted={draft?.jobTestCompleted}
        draft={draft}
      />
    );
  } else if (draft) {
    stepContent = (
      <OnboardingConfirmStep
        draft={draft}
        resumeName={resumeName}
        onBack={() => changeStep('profile', 'backward')}
        onStart={complete}
        isStarting={isCompleting}
        startError={completeError}
      />
    );
  } else {
    stepContent = null;
  }

  return (
    <div className="flex min-h-0 w-full flex-1 overflow-x-clip">
      <div
        key={step}
        className={cn(
          'flex min-h-0 w-full flex-1',
          shouldAnimateStep && 'onboarding-step-enter',
          shouldAnimateStep &&
            transitionDirection === 'backward' &&
            'onboarding-step-enter--backward',
        )}
      >
        {stepContent}
      </div>
    </div>
  );
}
