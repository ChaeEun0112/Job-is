import { afterEach, describe, expect, it } from '@jest/globals';
import { renderHook } from '@testing-library/react';
import useBodyScrollLock from '../useBodyScrollLock';

describe('useBodyScrollLock', () => {
  afterEach(() => {
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
    document.documentElement.style.removeProperty('--scrollbar-width');
  });

  it('잠금 상태에서 본문 스크롤을 막고 해제하면 이전 상태로 복원한다', () => {
    const { rerender, unmount } = renderHook(
      ({ isLocked }: { isLocked: boolean }) => useBodyScrollLock(isLocked),
      { initialProps: { isLocked: true } },
    );

    expect(document.body.style.overflow).toBe('hidden');

    rerender({ isLocked: false });
    expect(document.body.style.overflow).toBe('');

    unmount();
    expect(document.body.style.overflow).toBe('');
  });

  it('중첩된 잠금은 마지막 잠금이 해제될 때만 본문 스크롤을 복원한다', () => {
    const firstLock = renderHook(() => useBodyScrollLock(true));
    const secondLock = renderHook(() => useBodyScrollLock(true));

    firstLock.unmount();
    expect(document.body.style.overflow).toBe('hidden');

    secondLock.unmount();
    expect(document.body.style.overflow).toBe('');
  });
});
