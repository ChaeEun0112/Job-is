import type { JobDetailContentSection } from '@/features/jobs/types/jobDetail';

interface JobDetailContentProps {
  sections: JobDetailContentSection[];
  techStack: string[];
}

export default function JobDetailContent({ sections, techStack }: JobDetailContentProps) {
  return (
    <div className="flex w-full flex-col gap-3">
      <p className="text-label-small font-medium text-text-tertiary">상세 내용</p>
      <div className="mx-auto flex w-full max-w-[740px] flex-col items-start justify-center gap-2.5 rounded-xs border border-gray-200 bg-white p-6">
        {sections.map((section) => (
          <div key={section.heading} className="flex flex-col gap-1">
            <p className="text-body-small font-bold text-text-primary">{section.heading}</p>
            <ul className="flex flex-col gap-0.5 pl-6">
              {section.items.map((item, index) => (
                <li
                  key={`${section.heading}-${index}`}
                  className="list-disc text-body-small font-medium text-text-secondary"
                >
                  {item}
                </li>
              ))}
            </ul>
          </div>
        ))}
        <div className="flex flex-col gap-1">
          <p className="text-body-small font-bold text-text-primary">기술 스택</p>
          <div className="flex flex-wrap gap-2">
            {techStack.map((tech) => (
              <span
                key={tech}
                className="inline-flex h-8 items-center rounded-full border border-gray-200 bg-white px-3 text-body-small font-medium text-text-secondary"
              >
                {tech}
              </span>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
