import useDismissableOpen from '@/hooks/useDismissableOpen';

interface HeaderProfileMenuProps {
  profileImageUrl?: string;
  onProfileClick: () => void;
  onSettingsClick: () => void;
}

export default function HeaderProfileMenu({
  profileImageUrl,
  onProfileClick,
  onSettingsClick,
}: HeaderProfileMenuProps) {
  const { isOpen, setIsOpen, containerRef } = useDismissableOpen();

  const closeMenuAndRun = (callback: () => void) => {
    setIsOpen(false);
    callback();
  };

  return (
    <div ref={containerRef} className="relative mr-2 shrink-0">
      <button
        type="button"
        className="size-9 cursor-pointer rounded-full"
        onClick={() => setIsOpen((previous) => !previous)}
        aria-label="프로필 메뉴 열기"
        aria-expanded={isOpen}
        aria-controls="profile-menu"
      >
        {profileImageUrl ? (
          <img src={profileImageUrl} alt="" className="size-full rounded-full object-cover" />
        ) : (
          <span className="block size-full rounded-full bg-gray-100" />
        )}
      </button>
      {isOpen && (
        <div
          id="profile-menu"
          className="header-profile-menu-enter absolute top-full right-0 z-20 mt-2 flex w-32 flex-col overflow-hidden rounded-sm border border-gray-200 bg-white shadow-md"
        >
          <button
            type="button"
            className="cursor-pointer px-4 py-3 text-left text-label-medium font-medium text-text-primary transition-colors hover:bg-gray-50"
            onClick={() => closeMenuAndRun(onProfileClick)}
          >
            프로필
          </button>
          <button
            type="button"
            className="cursor-pointer px-4 py-3 text-left text-label-medium font-medium text-text-primary transition-colors hover:bg-gray-50"
            onClick={() => closeMenuAndRun(onSettingsClick)}
          >
            설정
          </button>
        </div>
      )}
    </div>
  );
}
