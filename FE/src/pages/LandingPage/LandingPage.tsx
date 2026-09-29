import { Navigate, useNavigate, useOutletContext } from 'react-router';
import type { LandingOutletContext } from '@/components/layout/LandingLayout';
import LandingContent from '@/features/landing/components/LandingContent';
import { getPostLoginPath, useAuthStore } from '@/store/useAuthStore';

export default function LandingPage() {
  const navigate = useNavigate();
  const landingContext = useOutletContext<LandingOutletContext>();
  const accessToken = useAuthStore((state) => state.accessToken);
  const onboardingCompleted = useAuthStore((state) => state.onboardingCompleted);

  if (accessToken) {
    return <Navigate to={getPostLoginPath(onboardingCompleted)} replace />;
  }

  return (
    <div className="flex min-h-0 w-full flex-1 flex-col items-center justify-center px-3 py-4">
      <LandingContent
        {...landingContext}
        onStart={() => navigate('/login')}
        onLogin={() => navigate('/login')}
      />
    </div>
  );
}
