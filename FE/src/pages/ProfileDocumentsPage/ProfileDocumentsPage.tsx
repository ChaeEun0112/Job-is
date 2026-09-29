import { Navigate, useLocation, useNavigate } from 'react-router';
import ProfileDocumentsManager from '@/features/profile/components/ProfileDocumentsManager';

export default function ProfileDocumentsPage() {
  const navigate = useNavigate();
  const { pathname, search } = useLocation();
  const isLegacyOnboardingPath = new URLSearchParams(search).get('from') === 'onboarding';
  const isFromOnboarding = pathname === '/onboarding/documents';
  const returnPath = isFromOnboarding ? '/onboarding' : '/profile';

  if (isLegacyOnboardingPath) {
    return <Navigate to="/onboarding/documents" replace />;
  }

  return (
    <div className="flex flex-1 items-start justify-center bg-gray-50 px-5 py-16 lg:py-30">
      <ProfileDocumentsManager
        onBack={() => navigate(returnPath)}
        backLabel={isFromOnboarding ? '이전' : '내 프로필로'}
      />
    </div>
  );
}
