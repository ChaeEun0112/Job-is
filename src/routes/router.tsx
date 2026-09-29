import { Navigate, createBrowserRouter } from 'react-router';
import avatarDefaultProfile from '@/assets/images/avatar-default-profile.png';
import {
  AccountRecoveryPage,
  AccountWithdrawalPage,
  AuthLayout,
  CallbackPage,
  ExplorePage,
  JobDetailExpiredPage,
  JobDetailPage,
  LandingLayout,
  LandingPage,
  LoginPage,
  MainLayout,
  NotFoundPage,
  OnboardingPage,
  PolicyPage,
  ProfileDocumentsPage,
  ProfilePage,
  QuizPage,
  RecommendationNewsDetailPage,
  RecommendationsPage,
  SavedJobsPage,
  SettingsPage,
  SystemErrorPage,
  UnsubscribePage,
} from '@/routes/lazyComponents';

const MAIN_NAVIGATION_TABS = [
  { label: '오늘의 추천', path: '/recommendations' },
  { label: '탐색', path: '/explore' },
  { label: '저장 목록', path: '/saved' },
];

const MAIN_TAB_HEADER = {
  variant: 'tab' as const,
  tabs: MAIN_NAVIGATION_TABS,
  profileImageUrl: avatarDefaultProfile,
};

const LANDING_METADATA = {
  title: 'Job.is | 매일 아침, 맞춤 취업 뉴스레터',
  description: '오늘 들어온 공고 중 나에게 맞는 채용 정보를 매일 아침 편지로 받아보세요.',
  robots: 'index,follow',
};

const PRIVATE_METADATA = {
  title: 'Job.is',
  description: '개인화된 취업 추천을 확인하세요.',
  robots: 'noindex,nofollow',
};

export const router = createBrowserRouter([
  {
    element: <LandingLayout />,
    handle: { seo: LANDING_METADATA },
    children: [{ path: '/', element: <LandingPage /> }],
  },
  {
    element: <AuthLayout />,
    handle: { seo: PRIVATE_METADATA },
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/login/fail', element: <LoginPage state="failed" /> },
      { path: '/login/email-required', element: <LoginPage state="email-required" /> },
      { path: '/policy', element: <PolicyPage /> },
      { path: '/oauth/callback', element: <CallbackPage /> },
      { path: '/account/recovery', element: <AccountRecoveryPage /> },
      { path: '/system-error', element: <SystemErrorPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
  {
    element: <MainLayout />,
    handle: { seo: PRIVATE_METADATA },
    children: [
      {
        path: '/onboarding',
        element: <OnboardingPage />,
        handle: {
          header: { variant: 'carousel', totalSteps: 2, activeIndex: 0 },
        },
      },
      {
        path: '/onboarding/documents',
        element: <ProfileDocumentsPage />,
        handle: {
          header: { variant: 'carousel', totalSteps: 2, activeIndex: 0 },
        },
      },
      {
        path: '/onboarding/aptitude-test',
        element: <QuizPage />,
        handle: {
          header: { variant: 'carousel', totalSteps: 2, activeIndex: 0 },
        },
      },
      {
        path: '/recommendations',
        element: <RecommendationsPage />,
        handle: {
          header: {
            ...MAIN_TAB_HEADER,
            activeIndex: 0,
          },
          seo: {
            title: '오늘의 추천 | Job.is',
            description: '오늘의 맞춤 채용 공고와 추천 이유를 확인하세요.',
            robots: 'noindex,nofollow',
          },
        },
      },
      {
        path: '/recommendations/deck',
        element: <RecommendationsPage screen="deck" />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/recommendations/news',
        element: <RecommendationsPage screen="news" />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/recommendations/news/:id',
        element: <RecommendationNewsDetailPage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/recommendations/complete',
        element: <RecommendationsPage screen="complete" />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/recommendations/archive',
        element: <RecommendationsPage screen="archive" />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/recommendations/revisit',
        element: <Navigate to="/recommendations/archive" replace />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/recommendations/empty-candidates',
        element: <RecommendationsPage screen="empty-candidates" />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/explore',
        element: <ExplorePage />,
        handle: {
          header: { ...MAIN_TAB_HEADER, activeIndex: 1 },
          seo: {
            title: '공고 탐색 | Job.is',
            description: '직무, 지역, 경력 조건으로 채용 공고를 직접 찾아보세요.',
            robots: 'noindex,nofollow',
          },
        },
      },
      {
        path: '/saved',
        element: <SavedJobsPage />,
        handle: {
          header: { ...MAIN_TAB_HEADER, activeIndex: 2 },
          seo: {
            title: '저장 목록 | Job.is',
            description: '저장한 채용 공고와 활동 내역을 확인하세요.',
            robots: 'noindex,nofollow',
          },
        },
      },
      {
        path: '/profile',
        element: <ProfilePage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/profile/documents',
        element: <ProfileDocumentsPage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/profile/aptitude-test',
        element: <QuizPage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/settings',
        element: <Navigate to="/settings/notifications" replace />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/settings/notifications',
        element: <SettingsPage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/settings/account',
        element: <SettingsPage screen="account" />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/settings/account/withdraw',
        element: <AccountWithdrawalPage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/settings/privacy',
        element: <SettingsPage screen="privacy" />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/unsubscribe',
        element: <UnsubscribePage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: -1 } },
      },
      {
        path: '/jobs/:id',
        element: <JobDetailPage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      {
        path: '/jobs/:id/expired',
        element: <JobDetailExpiredPage />,
        handle: { header: { ...MAIN_TAB_HEADER, activeIndex: 0 } },
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
