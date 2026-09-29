import { useState } from 'react';
import { Navigate, Outlet, useLocation, useMatches, useNavigate } from 'react-router';
import Footer from '@/components/layout/Footer';
import Header from '@/components/layout/Header';
import useRouteMetadata from '@/hooks/useRouteMetadata';
import { useAuthStore } from '@/store/useAuthStore';
import { cn } from '@/utils/cn';

type MainLayoutTab = {
  label: string;
  path?: string;
};

type MainLayoutHeaderHandle =
  | { variant?: 'default' }
  | { variant: 'carousel'; totalSteps: number; activeIndex: number; exitTo?: string }
  | {
      variant: 'tab';
      tabs: MainLayoutTab[];
      activeIndex: number;
      profileImageUrl?: string;
    };

export interface MainLayoutRouteHandle {
  header?: MainLayoutHeaderHandle;
}

export interface MainLayoutOutletContext {
  setCarouselActiveIndex: (activeIndex: number) => void;
}

export default function MainLayout() {
  useRouteMetadata();
  const navigate = useNavigate();
  const location = useLocation();
  const matches = useMatches();
  const accessToken = useAuthStore((state) => state.accessToken);
  const headerHandle = (matches.at(-1)?.handle as MainLayoutRouteHandle | undefined)?.header;
  const initialCarouselActiveIndex =
    headerHandle?.variant === 'carousel' ? headerHandle.activeIndex : 0;
  const [carouselActiveIndex, setCarouselActiveIndex] = useState(initialCarouselActiveIndex);
  const isRecommendationIntroPreview =
    location.pathname === '/recommendations' &&
    new URLSearchParams(location.search).get('preview') === 'intro';
  const transition = (location.state as { transition?: string } | null)?.transition;
  const shouldAnimateContent =
    location.pathname === '/onboarding' ||
    location.pathname === '/onboarding/documents' ||
    location.pathname === '/onboarding/aptitude-test' ||
    location.pathname === '/profile' ||
    location.pathname === '/profile/documents' ||
    location.pathname === '/profile/aptitude-test' ||
    transition === 'recommendation-flow' ||
    transition === 'settings-flow' ||
    transition === 'main-tab';

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="flex min-h-screen flex-col">
      {headerHandle?.variant === 'carousel' ? (
        <Header
          variant="carousel"
          totalSteps={headerHandle.totalSteps}
          activeIndex={carouselActiveIndex}
          onExit={() => navigate(headerHandle.exitTo ?? '/')}
        />
      ) : headerHandle?.variant === 'tab' ? (
        <Header
          variant="tab"
          tabs={headerHandle.tabs}
          activeIndex={headerHandle.activeIndex}
          onTabChange={(index) => {
            const path = headerHandle.tabs[index]?.path;
            if (!path) return;

            navigate(
              path,
              path === '/recommendations' ? undefined : { state: { transition: 'main-tab' } },
            );
          }}
          profileImageUrl={headerHandle.profileImageUrl}
          className={isRecommendationIntroPreview ? 'recommendation-intro-header-enter' : undefined}
        />
      ) : (
        <Header />
      )}
      <main className="flex min-h-0 flex-1 flex-col overflow-x-clip">
        <div
          key={location.pathname}
          className={cn(
            'flex min-h-0 flex-1 flex-col',
            shouldAnimateContent && 'page-content-enter',
          )}
        >
          <Outlet
            context={
              headerHandle?.variant === 'carousel'
                ? ({ setCarouselActiveIndex } satisfies MainLayoutOutletContext)
                : undefined
            }
          />
        </div>
      </main>
      <Footer />
    </div>
  );
}
