import { Link } from 'react-router';
import { PRIVACY_POLICY_URL, TERMS_OF_SERVICE_URL } from '@/constants/legalLinks';
import { cn } from '@/utils/cn';

type FooterProps = {
  className?: string;
};

export default function Footer({ className }: FooterProps) {
  return (
    <footer
      className={cn(
        'flex w-full items-center justify-center border-t border-gray-200 bg-white px-3 pt-4 pb-3',
        className,
      )}
    >
      <div className="flex w-full max-w-[1200px] items-center">
        <p className="shrink-0 text-body-medium font-medium whitespace-nowrap text-text-secondary">
          Job.is 매일 아침 3분, 맞춤 취업 뉴스레터
        </p>

        <nav className="flex flex-1 items-center justify-center gap-10 text-body-small font-medium whitespace-nowrap text-text-primary">
          <Link to="/" className="hover:text-primary-500">
            서비스소개
          </Link>
          <a
            href={TERMS_OF_SERVICE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-primary-500"
          >
            이용약관
          </a>
          <a
            href={PRIVACY_POLICY_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-primary-500"
          >
            개인정보처리방침
          </a>
          <button
            type="button"
            onClick={() => alert('문의하기는 준비 중이에요.')}
            className="cursor-pointer hover:text-primary-500"
          >
            문의하기
          </button>
        </nav>

        <p className="shrink-0 text-body-medium font-medium whitespace-nowrap text-text-secondary">
          © 2026 Job.is All rights reserved.
        </p>
      </div>
    </footer>
  );
}
