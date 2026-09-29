import { useEffect } from 'react';

interface BodyScrollStyles {
  overflow: string;
  paddingRight: string;
  scrollbarWidth: string;
}

let lockCount = 0;
let previousStyles: BodyScrollStyles | undefined;

export default function useBodyScrollLock(isLocked: boolean) {
  useEffect(() => {
    if (!isLocked) return;

    if (lockCount === 0) {
      const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
      previousStyles = {
        overflow: document.body.style.overflow,
        paddingRight: document.body.style.paddingRight,
        scrollbarWidth: document.documentElement.style.getPropertyValue('--scrollbar-width'),
      };

      document.body.style.overflow = 'hidden';
      if (scrollbarWidth > 0) {
        document.body.style.paddingRight = `${scrollbarWidth}px`;
        document.documentElement.style.setProperty('--scrollbar-width', `${scrollbarWidth}px`);
      }
    }
    lockCount += 1;

    return () => {
      lockCount -= 1;
      if (lockCount !== 0 || !previousStyles) return;

      document.body.style.overflow = previousStyles.overflow;
      document.body.style.paddingRight = previousStyles.paddingRight;
      document.documentElement.style.setProperty(
        '--scrollbar-width',
        previousStyles.scrollbarWidth,
      );
      previousStyles = undefined;
    };
  }, [isLocked]);
}
