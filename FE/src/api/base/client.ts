import { client, tokenClient } from '@/api/base/axios';
import { assertApiSuccess, toRequestError, unwrapApiResponse } from '@/api/base/error';
import type { ApiEnvelope } from '@/api/types';
import { clearAuth, getAccessToken, setAccessToken } from '@/store/useAuthStore';
import type { AxiosError, InternalAxiosRequestConfig } from 'axios';

interface CsrfTokenResponse {
  token: string;
  headerName: string;
}

interface TokenReissueResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

async function getCsrfHeaders() {
  const csrfData = await unwrapApiResponse(
    tokenClient.get<ApiEnvelope<CsrfTokenResponse>>('/api/auth/csrf'),
  );

  return { [csrfData.headerName]: csrfData.token };
}

export async function postWithCsrf<T>(url: string, withAuthorization = false) {
  const accessToken = withAuthorization ? getAccessToken() : null;

  return unwrapApiResponse(
    tokenClient.post<ApiEnvelope<T>>(url, undefined, {
      headers: {
        ...(await getCsrfHeaders()),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
    }),
  );
}

async function reissueAccessToken() {
  const reissueData = await postWithCsrf<TokenReissueResponse>('/api/auth/token/reissue');

  return reissueData.accessToken;
}

client.interceptors.request.use((config) => {
  const accessToken = getAccessToken();
  if (accessToken) {
    config.headers.set('Authorization', `Bearer ${accessToken}`);
  }
  return config;
});

interface RetriableRequestConfig extends InternalAxiosRequestConfig {
  hasRetriedAfterReissue?: boolean;
}

let isRefreshing = false;
let pendingRequests: Array<(accessToken: string | null) => void> = [];

function resolvePendingRequests(accessToken: string | null) {
  pendingRequests.forEach((resolve) => resolve(accessToken));
  pendingRequests = [];
}

client.interceptors.response.use(assertApiSuccess, async (error: AxiosError) => {
  const config = error.config as RetriableRequestConfig | undefined;
  const isReissueRequest = config?.url?.includes('/api/auth/token/reissue');

  if (
    error.response?.status !== 401 ||
    !config ||
    config.hasRetriedAfterReissue ||
    isReissueRequest
  ) {
    return Promise.reject(toRequestError(error));
  }

  config.hasRetriedAfterReissue = true;

  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      pendingRequests.push((accessToken) => {
        if (!accessToken) {
          reject(toRequestError(error));
          return;
        }

        config.headers.set('Authorization', `Bearer ${accessToken}`);
        resolve(client(config));
      });
    });
  }

  isRefreshing = true;

  try {
    const accessToken = await reissueAccessToken();
    setAccessToken(accessToken);
    resolvePendingRequests(accessToken);
    config.headers.set('Authorization', `Bearer ${accessToken}`);
    return await client(config);
  } catch (reissueError) {
    resolvePendingRequests(null);
    clearAuth();
    return Promise.reject(toRequestError(reissueError));
  } finally {
    isRefreshing = false;
  }
});
