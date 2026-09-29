import { useEffect, type ReactNode } from 'react';
import { useSearchParams } from 'react-router';
import Button from '@/components/common/Button';
import { ResultIcon } from '@/components/feedback';

export type SystemErrorVariant = 'server-error' | 'maintenance' | 'offline';

interface SystemErrorPageProps {
  variant?: SystemErrorVariant;
}

const PREVIEW_VARIANTS: SystemErrorVariant[] = ['server-error', 'maintenance', 'offline'];

function handleRetry() {
  window.location.reload();
}

const VARIANT_CONTENT: Record<
  SystemErrorVariant,
  {
    icon: 'danger' | 'warning' | 'loading';
    title: string;
    description: ReactNode;
    notice?: string;
    actions: ReactNode;
  }
> = {
  'server-error': {
    icon: 'danger',
    title: '잠시 문제가 생겼어요',
    description: (
      <>
        곧 정상으로 돌아올 거예요.
        <br />
        잠시 후 [다시 시도]를 눌러 주세요.
      </>
    ),
    actions: (
      <>
        <Button className="w-full max-w-104" onClick={handleRetry}>
          다시 시도
        </Button>
        <Button variant="outline" className="w-full max-w-104">
          문의하기
        </Button>
      </>
    ),
  },
  maintenance: {
    icon: 'warning',
    title: '잠깐 정비하고 있어요',
    description: '더 나은 레터로 곧 찾아뵐게요.',
    notice: '오전 9:00쯤 다시 열려요.',
    actions: (
      <Button className="w-full max-w-104" onClick={handleRetry}>
        다시 시도
      </Button>
    ),
  },
  offline: {
    icon: 'loading',
    title: '연결이 끊겼어요',
    description: (
      <>
        인터넷 연결을 확인하고 [다시 시도]를 눌러 주세요.
        <br />
        연결되면 자동으로 이어서 불러올게요.
      </>
    ),
    actions: (
      <Button className="w-full max-w-104" onClick={handleRetry}>
        다시 시도
      </Button>
    ),
  },
};

export default function SystemErrorPage({ variant = 'server-error' }: SystemErrorPageProps) {
  const [searchParams] = useSearchParams();

  // NOTE: ?preview= 쿼리스트링, 임시 확인용, 실제 트리거 연결 후 삭제 예정
  const previewVariant = PREVIEW_VARIANTS.find((item) => item === searchParams.get('preview'));
  const resolvedVariant = previewVariant ?? variant;
  const content = VARIANT_CONTENT[resolvedVariant];

  useEffect(() => {
    if (resolvedVariant !== 'offline') return;

    window.addEventListener('online', handleRetry);
    return () => window.removeEventListener('online', handleRetry);
  }, [resolvedVariant]);

  return (
    <div className="flex w-full flex-1 items-center justify-center bg-gray-50 px-3 py-8">
      <div className="flex w-full max-w-190 flex-col items-center gap-5 rounded-md border border-gray-200 bg-white p-6">
        <ResultIcon variant={content.icon} />

        <p className="text-center text-heading-medium font-semibold text-text-primary">
          {content.title}
        </p>

        <p className="text-center text-label-medium font-medium text-text-secondary">
          {content.description}
        </p>

        {content.notice && (
          <div className="flex w-full max-w-104 items-center justify-center rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6">
            <p className="text-center text-label-medium font-medium text-text-tertiary">
              {content.notice}
            </p>
          </div>
        )}

        {content.actions}
      </div>
    </div>
  );
}
