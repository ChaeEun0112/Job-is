import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router';
import { exchangeOAuthCode } from '@/api/auth';
import {
  getPostLoginPath,
  setAccessToken,
  setOnboardingCompleted,
  setUserId,
} from '@/store/useAuthStore';

const DEFAULT_FAILURE_MESSAGE = '로그인이 취소됐거나 실패했어요. 다시 시도해 주세요.';

function readCallbackParams() {
  const query = new URLSearchParams(window.location.search);
  const hash = new URLSearchParams(window.location.hash.replace(/^#/, ''));

  return {
    code: query.get('code') ?? hash.get('code'),
    error: query.get('error') ?? hash.get('error'),
    restoreCode: query.get('restoreCode') ?? hash.get('restoreCode'),
    restorableUntil: query.get('restorableUntil') ?? hash.get('restorableUntil'),
  };
}

export default function useOAuthCallback() {
  const navigate = useNavigate();
  const hasRequested = useRef(false);

  useEffect(() => {
    if (hasRequested.current) return;

    hasRequested.current = true;
    const { code, error, restoreCode, restorableUntil } = readCallbackParams();

    if (restoreCode) {
      navigate('/account/recovery', {
        replace: true,
        state: { restoreCode, restorableUntil },
      });
      return;
    }

    if (error || !code) {
      navigate(`/login/fail?message=${encodeURIComponent(DEFAULT_FAILURE_MESSAGE)}`, {
        replace: true,
      });
      return;
    }

    void exchangeOAuthCode(code)
      .then((result) => {
        setAccessToken(result.accessToken);
        setUserId(result.userId);
        setOnboardingCompleted(result.onboardingCompleted);

        const destination = result.isNewUser
          ? '/policy'
          : getPostLoginPath(result.onboardingCompleted);
        navigate(destination, { replace: true });
      })
      .catch(() => {
        navigate(`/login/fail?message=${encodeURIComponent(DEFAULT_FAILURE_MESSAGE)}`, {
          replace: true,
        });
      });
  }, [navigate]);
}
