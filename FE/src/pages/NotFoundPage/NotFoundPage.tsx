import { useNavigate } from 'react-router';
import Button from '@/components/common/Button';
import { ResultIcon } from '@/components/feedback';

export default function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <div className="flex w-full flex-1 items-center justify-center bg-gray-50 px-3 py-8">
      <div className="flex w-full max-w-190 flex-col items-center gap-5 rounded-md border border-gray-200 bg-white p-6">
        <ResultIcon variant="danger" />

        <p className="text-center text-heading-medium font-semibold text-text-primary">
          찾으시는 페이지가 없어요
        </p>

        <p className="text-center text-label-medium font-medium text-text-secondary">
          주소가 바뀌었거나 사라진 페이지일 수 있어요.
          <br />
          처음부터 다시 시작해 주세요.
        </p>

        <Button className="w-full max-w-104" onClick={() => navigate('/')}>
          처음으로
        </Button>
        <Button variant="outline" className="w-full max-w-104" onClick={() => navigate('/login')}>
          시작하기
        </Button>
      </div>
    </div>
  );
}
