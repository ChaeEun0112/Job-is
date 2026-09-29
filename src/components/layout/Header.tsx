import { type ReactNode } from 'react';
import { Link, useNavigate } from 'react-router';
import logoImage from '@/assets/icons/logo.svg';
import Button from '@/components/common/Button';
import CarouselIndicator from '@/components/common/CarouselIndicator';
import Tab from '@/components/common/Tab';
import { cn } from '@/utils/cn';
import HeaderProfileMenu from './HeaderProfileMenu';

interface HeaderTabItem {
  label: string;
}

type HeaderBaseProps = { className?: string };

type HeaderProps =
  | (HeaderBaseProps & { variant?: 'default' })
  | (HeaderBaseProps & {
      variant: 'carousel';
      totalSteps: number;
      activeIndex: number;
      onExit?: () => void;
    })
  | (HeaderBaseProps & {
      variant: 'tab';
      tabs: HeaderTabItem[];
      activeIndex: number;
      onTabChange?: (index: number) => void;
      profileImageUrl?: string;
    })
  | (HeaderBaseProps & {
      variant: 'adm';
      tabs: HeaderTabItem[];
      activeIndex: number;
      onTabChange?: (index: number) => void;
      adminEmail: string;
      onLogout?: () => void;
    });

function HeaderShell({ className, children }: { className?: string; children: ReactNode }) {
  return (
    <header
      className={cn(
        'sticky top-0 z-10 flex w-full items-center justify-center bg-white px-3 pt-3 pb-4',
        className,
      )}
    >
      <div className="flex w-full max-w-300 items-center justify-between gap-10">{children}</div>
    </header>
  );
}

function HeaderTabShell({ className, children }: { className?: string; children: ReactNode }) {
  return (
    <header
      className={cn(
        'sticky top-0 z-10 flex w-full items-center justify-center bg-white px-3 pt-3 pb-4',
        className,
      )}
    >
      <div className="flex h-15 w-full max-w-300 items-center">{children}</div>
    </header>
  );
}

function HeaderLogo({ suffix, to = '/' }: { suffix?: ReactNode; to?: string }) {
  return (
    <Link to={to} className="flex h-15 shrink-0 items-center gap-1 max-sm:h-10">
      <img src={logoImage} alt="Job.is" className="h-15 w-auto max-sm:h-10" />
      {suffix && (
        <span className="text-display-small leading-none font-bold whitespace-nowrap text-text-secondary">
          {suffix}
        </span>
      )}
    </Link>
  );
}

function HeaderTabNav({
  tabs,
  activeIndex,
  onTabChange,
  className,
}: {
  tabs: HeaderTabItem[];
  activeIndex: number;
  onTabChange?: (index: number) => void;
  className?: string;
}) {
  return (
    <nav className={cn('flex min-w-0 items-center gap-10', className)}>
      {tabs.map((tab, index) => (
        <Tab
          key={tab.label}
          label={tab.label}
          active={index === activeIndex}
          className="text-text-primary"
          onClick={() => onTabChange?.(index)}
        />
      ))}
    </nav>
  );
}

export default function Header(props: HeaderProps) {
  const { className } = props;
  const navigate = useNavigate();
  const homeDestination = '/recommendations';

  if (props.variant === 'carousel') {
    return (
      <HeaderShell className={className}>
        <HeaderLogo to={homeDestination} />
        <CarouselIndicator variant="dot" total={props.totalSteps} activeIndex={props.activeIndex} />
        <Button variant="outline" className="h-12.5 w-24" onClick={props.onExit}>
          나가기
        </Button>
      </HeaderShell>
    );
  }

  if (props.variant === 'tab') {
    return (
      <HeaderTabShell className={className}>
        <HeaderLogo to={homeDestination} />
        <HeaderTabNav
          tabs={props.tabs}
          activeIndex={props.activeIndex}
          onTabChange={props.onTabChange}
          className="h-10 flex-1 px-12.5 max-sm:gap-3 max-sm:px-3"
        />
        <HeaderProfileMenu
          profileImageUrl={props.profileImageUrl}
          onProfileClick={() => navigate('/profile')}
          onSettingsClick={() => navigate('/settings/notifications')}
        />
      </HeaderTabShell>
    );
  }

  if (props.variant === 'adm') {
    return (
      <HeaderShell className={className}>
        <HeaderLogo suffix="ADM" />
        <HeaderTabNav
          tabs={props.tabs}
          activeIndex={props.activeIndex}
          onTabChange={props.onTabChange}
        />
        <div className="flex shrink-0 items-center gap-4">
          <p className="text-label-medium font-medium whitespace-nowrap text-black">
            운영자: {props.adminEmail}
          </p>
          <button
            type="button"
            onClick={props.onLogout}
            className="cursor-pointer text-label-medium font-medium whitespace-nowrap text-text-tertiary underline decoration-solid decoration-from-font [text-underline-position:from-font]"
          >
            로그아웃
          </button>
        </div>
      </HeaderShell>
    );
  }

  return (
    <HeaderShell className={className}>
      <HeaderLogo />
      <Button className="h-12.5" onClick={() => navigate('/login')}>
        시작하기
      </Button>
    </HeaderShell>
  );
}
