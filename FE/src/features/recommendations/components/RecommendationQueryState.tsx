import { type ReactNode } from 'react';
import { Button, NoticePanel } from '@/components/common';
import { Spinner } from '@/components/feedback';
import RecommendationScreenLayout from './RecommendationScreenLayout';

interface RecommendationQueryStateProps {
  isLoading: boolean;
  isError: boolean;
  errorTitle: string;
  onRetry: () => void;
  children: ReactNode;
}

export default function RecommendationQueryState({
  isLoading,
  isError,
  errorTitle,
  onRetry,
  children,
}: RecommendationQueryStateProps) {
  if (isLoading) {
    return (
      <RecommendationScreenLayout>
        <Spinner />
      </RecommendationScreenLayout>
    );
  }

  if (isError) {
    return (
      <RecommendationScreenLayout>
        <NoticePanel resultIconVariant="danger" title={errorTitle}>
          <Button onClick={onRetry}>다시 시도</Button>
        </NoticePanel>
      </RecommendationScreenLayout>
    );
  }

  return <>{children}</>;
}
