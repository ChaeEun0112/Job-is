const SKELETON_ITEMS = Array.from({ length: 6 }, (_, index) => index);

export default function ExploreJobGridSkeleton() {
  return (
    <div
      className="grid w-full grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-2 lg:grid-cols-3"
      aria-busy="true"
      aria-live="polite"
    >
      {SKELETON_ITEMS.map((index) => (
        <article key={index} className="flex w-full flex-col items-start gap-3">
          <div className="h-49.25 w-full animate-pulse rounded-md border border-white bg-gray-200" />
          <div className="flex w-full items-start gap-3 px-2.5">
            <div className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
              <div className="h-5.5 w-3/4 animate-pulse rounded bg-gray-200" />
              <div className="h-5 w-1/2 animate-pulse rounded bg-gray-200" />
              <div className="h-5 w-2/5 animate-pulse rounded bg-gray-200" />
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
