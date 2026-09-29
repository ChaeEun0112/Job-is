import { useEffect, useRef, useState } from 'react';

export default function useDismissableOpen<T extends HTMLElement = HTMLDivElement>(
  closeOnEscape = true,
) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<T>(null);

  useEffect(() => {
    if (!isOpen) return;

    function handlePointerDown(event: PointerEvent) {
      if (containerRef.current?.contains(event.target as Node)) return;
      setIsOpen(false);
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') setIsOpen(false);
    }

    document.addEventListener('pointerdown', handlePointerDown);
    if (closeOnEscape) document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('pointerdown', handlePointerDown);
      if (closeOnEscape) document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, closeOnEscape]);

  return { isOpen, setIsOpen, containerRef };
}
