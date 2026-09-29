import Button from '@/components/common/Button';

export default function LoginEmailRequiredContent() {
  return (
    <>
      <p className="text-center text-heading-medium font-semibold text-text-primary">
        브리핑을 보내려면 이메일이 필요해요
      </p>
      <div className="w-full max-w-104">
        <input
          type="email"
          aria-label="이메일 주소"
          placeholder="min****@naver.com"
          className="h-10 w-full rounded-sm border border-gray-700 bg-white px-4 text-label-medium font-medium text-gray-700 placeholder:text-gray-700 focus:border-primary-500 focus:outline-none"
        />
      </div>
      <Button className="w-full max-w-104">확인 메일 보내기</Button>
      <div className="flex w-full max-w-103.75 items-center justify-center rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6">
        <p className="text-center text-label-medium font-medium text-text-tertiary">
          메일함에서 확인 링크를 눌러주세요.
          <br />
          링크는 <span className="text-text-secondary">72시간</span> 동안 유효해요.
        </p>
      </div>
    </>
  );
}
