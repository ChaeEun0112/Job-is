import { useId } from 'react';
import Tag from '@/components/common/Tag';
import TextInput from '@/components/common/TextInput';
import Modal, { type ModalPresetProps } from './Modal';

interface ModalTagCommentProps extends ModalPresetProps {
  tagLabel: string;
  tags: string[];
  onRemoveTag: (index: number) => void;
  commentLabel: string;
  commentValue: string;
  onCommentChange: (value: string) => void;
  commentPlaceholder?: string;
}

export default function ModalTagComment({
  title,
  ref,
  tagLabel,
  tags,
  onRemoveTag,
  commentLabel,
  commentValue,
  onCommentChange,
  commentPlaceholder,
  footer,
  onClose,
  className,
}: ModalTagCommentProps) {
  const commentId = useId();

  return (
    <Modal ref={ref} title={title} onClose={onClose} footer={footer} className={className}>
      <p className="w-full text-base font-medium text-text-secondary">{tagLabel}</p>
      <div className="flex w-full gap-2.5">
        {tags.map((tag, index) => (
          <Tag
            key={`${tag}-${index}`}
            variant="removable"
            label={tag}
            onClick={() => onRemoveTag(index)}
          />
        ))}
      </div>
      <TextInput
        id={commentId}
        label={commentLabel}
        value={commentValue}
        onChange={(event) => onCommentChange(event.target.value)}
        placeholder={commentPlaceholder}
        className="w-full"
      />
    </Modal>
  );
}
