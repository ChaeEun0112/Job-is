import { useId, type ComponentPropsWithRef } from 'react';
import Checkbox from '@/components/common/Checkbox';
import Modal, { type ModalPresetProps } from './Modal';

interface ModalCheckboxProps
  extends
    ModalPresetProps,
    Omit<ComponentPropsWithRef<'input'>, 'type' | 'className' | 'ref' | 'title'> {
  description: string;
  checkboxLabel: string;
}

export default function ModalCheckbox({
  title,
  ref,
  description,
  checkboxLabel,
  footer,
  onClose,
  className,
  id,
  ...checkboxProps
}: ModalCheckboxProps) {
  const generatedId = useId();
  const checkboxId = id ?? generatedId;

  return (
    <Modal ref={ref} title={title} onClose={onClose} footer={footer} className={className}>
      <p className="w-full whitespace-pre-line text-base font-medium text-text-secondary">
        {description}
      </p>
      <Checkbox id={checkboxId} label={checkboxLabel} {...checkboxProps} />
    </Modal>
  );
}
