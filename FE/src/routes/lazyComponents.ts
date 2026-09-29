import { lazy } from 'react';

export const AuthLayout = lazy(() => import('@/components/layout/AuthLayout'));
export const LandingLayout = lazy(() => import('@/components/layout/LandingLayout'));
export const MainLayout = lazy(() => import('@/components/layout/MainLayout'));
export const AccountRecoveryPage = lazy(
  () => import('@/pages/AccountRecoveryPage/AccountRecoveryPage'),
);
export const AccountWithdrawalPage = lazy(
  () => import('@/pages/AccountWithdrawalPage/AccountWithdrawalPage'),
);
export const CallbackPage = lazy(() => import('@/pages/CallbackPage/CallbackPage'));
export const ExplorePage = lazy(() => import('@/pages/ExplorePage/ExplorePage'));
export const JobDetailExpiredPage = lazy(
  () => import('@/pages/JobDetailExpiredPage/JobDetailExpiredPage'),
);
export const JobDetailPage = lazy(() => import('@/pages/JobDetailPage/JobDetailPage'));
export const LandingPage = lazy(() => import('@/pages/LandingPage/LandingPage'));
export const LoginPage = lazy(() => import('@/pages/LoginPage/LoginPage'));
export const NotFoundPage = lazy(() => import('@/pages/NotFoundPage/NotFoundPage'));
export const OnboardingPage = lazy(() => import('@/pages/OnboardingPage/OnboardingPage'));
export const PolicyPage = lazy(() => import('@/pages/PolicyPage/PolicyPage'));
export const ProfileDocumentsPage = lazy(
  () => import('@/pages/ProfileDocumentsPage/ProfileDocumentsPage'),
);
export const ProfilePage = lazy(() => import('@/pages/ProfilePage/ProfilePage'));
export const QuizPage = lazy(() => import('@/pages/QuizPage/QuizPage'));
export const RecommendationNewsDetailPage = lazy(
  () => import('@/pages/RecommendationNewsDetailPage/RecommendationNewsDetailPage'),
);
export const RecommendationsPage = lazy(
  () => import('@/pages/RecommendationsPage/RecommendationsPage'),
);
export const SavedJobsPage = lazy(() => import('@/pages/SavedJobsPage/SavedJobsPage'));
export const SettingsPage = lazy(() => import('@/pages/SettingsPage/SettingsPage'));
export const SystemErrorPage = lazy(() => import('@/pages/SystemErrorPage/SystemErrorPage'));
export const UnsubscribePage = lazy(() => import('@/pages/UnsubscribePage/UnsubscribePage'));
