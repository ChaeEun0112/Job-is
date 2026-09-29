import { afterEach, beforeAll, describe, expect, it, jest } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import type ModalComponent from '../Modal';

jest.mock(
  '@/assets/icons/icon-close.svg?react',
  () => ({
    __esModule: true,
    default: () => <svg />,
  }),
  { virtual: true },
);

let Modal: typeof ModalComponent;

describe('Modal', () => {
  beforeAll(async () => {
    ({ default: Modal } = await import('../Modal'));
  });

  afterEach(() => {
    document.body.innerHTML = '';
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
    document.documentElement.style.removeProperty('--scrollbar-width');
  });

  it('열리면 첫 조작 요소에 포커스하고 닫히면 이전 포커스를 복원한다', () => {
    const trigger = document.createElement('button');
    document.body.append(trigger);
    trigger.focus();

    const { unmount } = render(
      <Modal title="삭제 확인" onClose={jest.fn()}>
        <p>삭제할까요?</p>
      </Modal>,
    );

    expect(document.activeElement).toBe(screen.getByRole('button', { name: '닫기' }));
    expect(document.body.style.overflow).toBe('hidden');

    unmount();

    expect(document.activeElement).toBe(trigger);
    expect(document.body.style.overflow).toBe('');
  });
});
