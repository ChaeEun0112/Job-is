import Button from '@/components/common/Button';
import Tag from '@/components/common/Tag';

interface QuizResultProps {
  resultName: string;
  resultSummary: string;
  resultTags: string[];
  onApply: () => void;
  onRetry: () => void;
  isApplying?: boolean;
  errorMessage?: string;
}

export default function QuizResult({
  resultName,
  resultSummary,
  resultTags,
  onApply,
  onRetry,
  isApplying = false,
  errorMessage,
}: QuizResultProps) {
  return (
    <section className="flex w-full max-w-190 flex-col items-center gap-5 overflow-hidden rounded-md border border-gray-200 bg-white px-8 py-6">
      <h1 className="w-full text-center text-label-medium font-medium text-text-primary">
        {resultName}
      </h1>

      <div className="flex w-full flex-wrap justify-center gap-2.5">
        {resultTags.map((tag) => (
          <Tag key={tag} variant="hash" label={tag} />
        ))}
      </div>

      <p className="w-full text-center text-label-medium font-medium text-text-secondary">
        {resultSummary}
      </p>

      {errorMessage && <p className="text-label-medium text-danger-500">{errorMessage}</p>}

      <div className="flex w-full flex-wrap items-center justify-center gap-2.5">
        <Button className="w-37.5" onClick={onApply} disabled={isApplying}>
          프로필에 반영
        </Button>
        <Button variant="outline" className="w-37.5" onClick={onRetry} disabled={isApplying}>
          다시 응시
        </Button>
      </div>
    </section>
  );
}
