import Button from '@/components/common/Button';
import ResultIcon from '@/components/feedback/ResultIcon';

export default function WithdrawalCompleteContent({ onHome }: { onHome: () => void }) {
  return (
    <section className="flex min-h-74 w-full max-w-190 flex-col items-center gap-5 overflow-hidden rounded-md border border-gray-200 bg-white p-6">
      <ResultIcon variant="success" />

      <h1 className="text-heading-medium font-semibold text-text-primary">탈퇴가 완료됐어요.</h1>

      <p className="text-label-medium font-medium text-text-secondary">
        그동안 함께 해주셔서 감사합니다.
      </p>

      <div className="rounded-xs border border-dashed border-gray-400 bg-gray-200 p-6 text-label-medium font-medium whitespace-pre-line text-text-tertiary">
        {
          '30일 안에 같은 소셜(카카오/구글)로 다시 로그인하면 계정을 복구할 수 있어요.\n30일이 지나면 모든 정보가 완전히 삭제돼요.'
        }
      </div>

      <Button className="w-full max-w-120" onClick={onHome}>
        홈으로
      </Button>
    </section>
  );
}
