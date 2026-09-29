import { type ComponentPropsWithoutRef } from 'react';
import { Link } from 'react-router';
import ChevronRightIcon from '@/assets/icons/icon-chevron-right.svg?react';
import HomeIcon from '@/assets/icons/icon-home.svg?react';
import { cn } from '@/utils/cn';

type BreadcrumbItem = {
  label: string;
  to?: string;
  current?: boolean;
};

type BreadcrumbProps = ComponentPropsWithoutRef<'nav'> & {
  items: BreadcrumbItem[];
};

export default function Breadcrumb({ className, items, ...props }: BreadcrumbProps) {
  return (
    <nav aria-label="breadcrumb" className={cn('max-w-full', className)} {...props}>
      <ol className="flex flex-wrap items-center gap-2 rounded-2xl border border-gray-200 bg-white p-4 text-sm text-gray-700">
        <li className="inline-flex items-center gap-1 text-gray-500 hover:text-gray-900">
          <Link to="/" className="inline-flex items-center gap-1 text-gray-500 hover:text-gray-900">
            <HomeIcon className="size-4" /> 홈
          </Link>
        </li>

        {items.map((item) => (
          <li key={item.label} className="inline-flex items-center gap-2">
            <ChevronRightIcon className="size-4 text-gray-300" />
            {item.to && !item.current ? (
              <Link to={item.to} className="text-gray-700 transition-colors hover:text-gray-900">
                {item.label}
              </Link>
            ) : (
              <span aria-current="page" className="font-semibold text-gray-900">
                {item.label}
              </span>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
}
