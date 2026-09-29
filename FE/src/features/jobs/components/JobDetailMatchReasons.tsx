import { Badge } from '@/components/common';

interface JobDetailMatchReasonsProps {
  matchScore: number;
  reasons: string[];
}

export default function JobDetailMatchReasons({ matchScore, reasons }: JobDetailMatchReasonsProps) {
  return (
    <div className="flex w-full flex-col gap-3">
      <div className="flex items-center gap-2">
        <p className="text-label-small font-medium text-text-tertiary">왜 추천했나요</p>
        <Badge type="outline" color="primary">
          적합도 {matchScore}%
        </Badge>
      </div>
      {reasons.length > 0 && (
        <ul className="flex flex-col gap-1.5 pl-5">
          {reasons.map((reason, index) => (
            <li
              key={`${reason}-${index}`}
              className="list-disc text-body-small font-medium text-text-secondary"
            >
              {reason}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
