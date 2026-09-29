import { type ReactNode } from 'react';

export default function RecommendationScreenLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-0 w-full flex-1 items-center justify-center bg-gray-50 px-3 py-10">
      {children}
    </div>
  );
}
