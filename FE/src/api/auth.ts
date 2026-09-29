import { postWithCsrf } from '@/api/base/client';
import { api } from '@/api/base/request';
import { ENV } from '@/api/env';
import type {
  AccountResponse,
  ConsentRequest,
  CurrentSessionResponse,
  OAuthExchangeResponse,
  OAuthProvider,
  RestoreRequest,
  WithdrawalRequest,
  WithdrawalResponse,
} from './types/auth.types';

export function getOAuthAuthorizeUrl(provider: OAuthProvider) {
  return `${ENV.API_BASE_URL}/api/auth/oauth/${provider}`;
}

export async function exchangeOAuthCode(loginCode: string) {
  return api.post<OAuthExchangeResponse>('/api/auth/oauth/exchange', { loginCode });
}

export async function getCurrentSession() {
  return api.get<CurrentSessionResponse>('/api/auth/me');
}

export async function getAccount() {
  return api.get<AccountResponse>('/api/auth/account');
}

export async function logout() {
  return postWithCsrf<string>('/api/auth/logout', true);
}

export async function restoreAccount(request: RestoreRequest) {
  return api.post<OAuthExchangeResponse>('/api/auth/restore', request);
}

export async function withdrawAccount(request: WithdrawalRequest) {
  return api.delete<WithdrawalResponse>('/api/auth/withdraw', {
    data: request,
  });
}

export async function postConsent(consent: ConsentRequest) {
  await api.post<void>('/api/auth/consent', consent);
}
