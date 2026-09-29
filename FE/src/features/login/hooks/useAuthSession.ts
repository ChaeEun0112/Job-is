import { useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import { getCurrentSession } from '@/api/auth';
import { QUERY_KEYS } from '@/constants/queryKey';
import { clearAuth, setOnboardingCompleted, setUserId, useAuthStore } from '@/store/useAuthStore';

export default function useAuthSession() {
  const accessToken = useAuthStore((state) => state.accessToken);
  const { data: session, isError } = useQuery({
    queryKey: QUERY_KEYS.AUTH.SESSION(),
    queryFn: getCurrentSession,
    enabled: Boolean(accessToken),
    retry: false,
  });

  useEffect(() => {
    if (!session) return;

    setUserId(session.userId);
    setOnboardingCompleted(session.onboardingCompleted);
  }, [session]);

  useEffect(() => {
    if (isError) clearAuth();
  }, [isError]);
}
