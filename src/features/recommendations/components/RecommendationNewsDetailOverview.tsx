interface RecommendationNewsDetailOverviewProps {
  summary: string;
}

export default function RecommendationNewsDetailOverview({
  summary,
}: RecommendationNewsDetailOverviewProps) {
  return (
    <div className="flex w-full flex-col gap-2">
      <p className="text-label-small font-medium text-text-tertiary">요약</p>
      <div className="w-full rounded-sm bg-gray-200 p-4">
        <p className="whitespace-pre-line text-body-small font-medium text-text-secondary">
          {summary}
        </p>
      </div>
    </div>
  );
}
