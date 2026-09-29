import { type ComponentPropsWithRef } from 'react';
import GoogleIcon from '@/assets/icons/icon-google.svg?react';
import KakaoIcon from '@/assets/icons/icon-kakao.svg?react';
import { cn } from '@/utils/cn';

interface SocialLoginButtonProps extends ComponentPropsWithRef<'button'> {
  provider: 'google' | 'kakao';
}

const PROVIDER_CONFIG = {
  google: {
    Icon: GoogleIcon,
    label: '구글 로그인',
    className: 'border border-solid border-gray-300 bg-white',
  },
  kakao: { Icon: KakaoIcon, label: '카카오 로그인', className: 'bg-[#fee500]' },
} as const;

export default function SocialLoginButton({
  className,
  ref,
  provider,
  type = 'button',
  ...props
}: SocialLoginButtonProps) {
  const { Icon, label, className: providerClassName } = PROVIDER_CONFIG[provider];

  return (
    <button
      ref={ref}
      type={type}
      className={cn(
        'relative flex h-11 w-full cursor-pointer items-center rounded-sm px-4 py-3',
        providerClassName,
        className,
      )}
      {...props}
    >
      <Icon className="size-5 shrink-0" />
      <span className="absolute inset-0 flex items-center justify-center text-label-medium font-medium text-text-primary">
        {label}
      </span>
    </button>
  );
}
