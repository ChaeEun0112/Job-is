import { Link } from 'react-router';
import { Spinner } from '@/components/feedback';

export default function LoginCallbackContent() {
  return (
    <>
      <Spinner />
      <p className="text-heading-medium font-semibold text-text-primary">로그인하고 있어요...</p>
      <p className="text-label-medium font-medium text-text-tertiary">잠시만요, 거의 다 됐어요</p>
      <Link
        to="/login"
        className="text-label-large font-medium text-text-tertiary underline decoration-solid decoration-from-font [text-underline-position:from-font]"
      >
        취소하고 돌아가기
      </Link>
    </>
  );
}
