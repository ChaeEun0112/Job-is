import { Link, Navigate, useNavigate, useSearchParams } from 'react-router';
import LoginDefaultContent from '@/features/login/components/LoginDefaultContent';
import LoginEmailRequiredContent from '@/features/login/components/LoginEmailRequiredContent';
import LoginFailureContent from '@/features/login/components/LoginFailureContent';
import { getPostLoginPath, useAuthStore } from '@/store/useAuthStore';

type LoginPageState = 'default' | 'failed' | 'email-required';

interface LoginPageProps {
  state?: LoginPageState;
}

const DEFAULT_FAILURE_MESSAGE = '로그인에 실패했어요. 다시 시도해 주세요.';

export default function LoginPage({ state = 'default' }: LoginPageProps) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const failureMessage = searchParams.get('message') ?? DEFAULT_FAILURE_MESSAGE;
  const accessToken = useAuthStore((authState) => authState.accessToken);
  const onboardingCompleted = useAuthStore((authState) => authState.onboardingCompleted);

  if (accessToken) {
    return <Navigate to={getPostLoginPath(onboardingCompleted)} replace />;
  }

  const content =
    state === 'failed' ? (
      <LoginFailureContent message={failureMessage} onRetry={() => navigate('/login')} />
    ) : state === 'email-required' ? (
      <LoginEmailRequiredContent />
    ) : (
      <LoginDefaultContent />
    );

  return (
    <div className="flex w-full flex-1 items-center justify-center bg-gray-50 px-3 py-8">
      <div className="flex w-full max-w-190 flex-col items-center gap-5 rounded-md border border-gray-200 bg-white p-6">
        <h1 className="text-heading-xlarge font-bold text-text-primary">
          <Link to="/">Job.is</Link>
        </h1>
        {content}
      </div>
    </div>
  );
}
