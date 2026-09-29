import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { restoreAccount } from '@/api/auth';
import {
  getPostLoginPath,
  setAccessToken,
  setOnboardingCompleted,
  setUserId,
} from '@/store/useAuthStore';

export default function useAccountRecovery() {
  const navigate = useNavigate();
  const restoreMutation = useMutation({
    mutationFn: restoreAccount,
    onSuccess: (result) => {
      setAccessToken(result.accessToken);
      setUserId(result.userId);
      setOnboardingCompleted(result.onboardingCompleted);
      navigate(getPostLoginPath(result.onboardingCompleted), { replace: true });
    },
  });

  return {
    restoreAccount: restoreMutation.mutate,
    isRestoring: restoreMutation.isPending,
    isRestoreError: restoreMutation.isError,
  };
}
