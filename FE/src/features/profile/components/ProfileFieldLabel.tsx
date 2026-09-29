import { type ReactNode } from 'react';
import Badge from '@/components/common/Badge';

interface ProfileFieldLabelProps {
  children: ReactNode;
  status?: '필수' | '선택' | '읽기 전용';
}

export default function ProfileFieldLabel({ children, status }: ProfileFieldLabelProps) {
  return (
    <div className="flex items-center gap-2">
      <h2 className="text-label-medium font-medium text-text-primary">{children}</h2>
      {status && (
        <Badge color={status === '필수' ? 'primary' : 'disabled'} type="outline">
          {status}
        </Badge>
      )}
    </div>
  );
}
