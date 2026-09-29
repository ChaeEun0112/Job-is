import { NavLink } from 'react-router';
import { cn } from '@/utils/cn';

const SETTINGS_SECTIONS = [
  { label: '알림 수신', path: '/settings/notifications' },
  { label: '계정', path: '/settings/account' },
  { label: '개인정보 · 데이터', path: '/settings/privacy' },
];

export default function SettingsSidebar() {
  return (
    <aside className="w-75 shrink-0 max-md:w-full" aria-label="설정 메뉴">
      <nav className="flex flex-col gap-1.25 max-md:flex-row max-md:overflow-x-auto">
        {SETTINGS_SECTIONS.map((section) => (
          <NavLink
            key={section.path}
            to={section.path}
            className={({ isActive }) =>
              cn(
                'flex h-14 items-center justify-center rounded-sm border border-primary-400 bg-white px-5 text-label-large font-medium whitespace-nowrap text-text-primary',
                isActive && 'bg-primary-400',
              )
            }
          >
            {section.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
