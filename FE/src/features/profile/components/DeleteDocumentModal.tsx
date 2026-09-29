import { createPortal } from 'react-dom';
import Button from '@/components/common/Button';
import Modal from '@/components/common/Modal';

interface DeleteDocumentModalProps {
  documentLabel: string;
  onCancel: () => void;
  onConfirm: () => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export default function DeleteDocumentModal({
  documentLabel,
  onCancel,
  onConfirm,
  isSubmitting,
  errorMessage,
}: DeleteDocumentModalProps) {
  return createPortal(
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black-alpha-40 px-5"
      role="presentation"
      onMouseDown={(event) => {
        if (!isSubmitting && event.target === event.currentTarget) onCancel();
      }}
    >
      <Modal
        role="dialog"
        aria-modal="true"
        aria-describedby="delete-document-description"
        title={`${documentLabel}를 삭제할까요?`}
        onClose={isSubmitting ? () => {} : onCancel}
        className="min-h-0 max-w-120 shadow-xl"
        footer={
          <>
            <Button
              variant="outline"
              className="h-12 border-gray-300"
              onClick={onCancel}
              disabled={isSubmitting}
            >
              취소
            </Button>
            <Button className="h-12" onClick={onConfirm} disabled={isSubmitting}>
              {isSubmitting ? '삭제 중…' : '삭제'}
            </Button>
          </>
        }
      >
        <p
          id="delete-document-description"
          className="text-body-medium font-medium text-text-secondary"
        >
          삭제하면 즉시 파기되며 복구할 수 없어요.
        </p>
        {errorMessage && (
          <p className="mt-3 text-label-small font-medium text-danger-500" role="alert">
            {errorMessage}
          </p>
        )}
      </Modal>
    </div>,
    document.body,
  );
}
