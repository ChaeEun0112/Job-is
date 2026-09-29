import {
  useEffect,
  useId,
  useRef,
  type ComponentProps,
  type ComponentPropsWithRef,
  type ReactNode,
} from 'react';
import CloseIcon from '@/assets/icons/icon-close.svg?react';
import useBodyScrollLock from '@/hooks/useBodyScrollLock';
import { cn } from '@/utils/cn';

interface ModalProps extends ComponentPropsWithRef<'div'> {
  title: string;
  onClose?: () => void;
  footer?: ReactNode;
}

const FOCUSABLE_SELECTOR =
  'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])';

export default function Modal({
  className,
  ref,
  title,
  onClose,
  footer,
  children,
  ...props
}: ModalProps) {
  const dialogRef = useRef<HTMLDivElement>(null);
  const onCloseRef = useRef(onClose);
  const titleId = useId();

  useBodyScrollLock(true);

  useEffect(() => {
    onCloseRef.current = onClose;
  }, [onClose]);

  useEffect(() => {
    const node = dialogRef.current;
    const previouslyFocused = document.activeElement as HTMLElement | null;
    const focusable = node
      ? Array.from(node.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR))
      : [];

    (focusable[0] ?? node)?.focus();

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        onCloseRef.current?.();
        return;
      }
      if (event.key !== 'Tab' || !node) return;
      const focusableElements = Array.from(node.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR));
      if (focusableElements.length === 0) {
        event.preventDefault();
        node.focus();
        return;
      }
      const first = focusableElements[0];
      const last = focusableElements[focusableElements.length - 1];
      const isFocusOutsideModal = !node.contains(document.activeElement);

      if (event.shiftKey && (document.activeElement === first || isFocusOutsideModal)) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && (document.activeElement === last || isFocusOutsideModal)) {
        event.preventDefault();
        first.focus();
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      previouslyFocused?.focus();
    };
  }, []);

  return (
    <div
      ref={(node) => {
        dialogRef.current = node;
        if (typeof ref === 'function') {
          return ref(node) ?? undefined;
        }
        if (ref) ref.current = node;
      }}
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
      tabIndex={-1}
      className={cn(
        'flex min-h-[296px] w-full flex-col items-start rounded-md border border-gray-200 bg-white p-6 outline-none',
        className,
      )}
      {...props}
    >
      {onClose && (
        <div className="flex h-6 w-full items-end justify-end">
          <button
            type="button"
            onClick={onClose}
            aria-label="닫기"
            className="flex size-6 items-center justify-center"
          >
            <CloseIcon className="size-6" />
          </button>
        </div>
      )}
      <div className="flex w-full flex-1 flex-col gap-4 px-4 py-2">
        <p id={titleId} className="w-full text-2xl leading-normal font-bold text-text-primary">
          {title}
        </p>
        {children}
      </div>
      {footer && <div className="flex w-full items-center justify-end gap-2 p-4">{footer}</div>}
    </div>
  );
}

export type ModalPresetProps = Pick<
  ComponentProps<typeof Modal>,
  'title' | 'onClose' | 'footer' | 'className' | 'ref'
>;
