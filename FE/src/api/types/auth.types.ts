export type OAuthProvider = 'google' | 'kakao';

export interface OAuthExchangeResponse {
  accessToken: string;
  userId: number;
  isNewUser: boolean;
  onboardingCompleted: boolean;
}

export interface CurrentSessionResponse {
  userId: number;
  email: string;
  socialType: 'GOOGLE' | 'KAKAO';
  status: 'ACTIVE' | 'DORMANT' | 'WITHDRAWN';
  onboardingCompleted: boolean;
  restorableUntil: string | null;
}

export interface AccountResponse {
  socialType: 'GOOGLE' | 'KAKAO';
  joinedAt: string;
  receivingEmail: string;
  emailVerified: boolean;
}

export interface ConsentRequest {
  termsAgreed: boolean;
  privacyAgreed: boolean;
  ageOver14Agreed: boolean;
  marketingAgreed?: boolean;
}

export type WithdrawalReasonCode =
  | 'RECOMMENDATION_MISMATCH'
  | 'EMAIL_TOO_FREQUENT'
  | 'NOT_JOB_SEEKING'
  | 'SERVICE_NOT_USED'
  | 'OTHER';

export interface WithdrawalRequest {
  reasonCode?: WithdrawalReasonCode;
  reasonDetail?: string;
}

export interface WithdrawalResponse {
  restorableUntil: string;
}

export interface RestoreRequest {
  restoreCode: string;
}
