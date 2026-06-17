import type { Breadcrumb } from '@/types';
import Link from 'next/link';

interface BreadcrumbsProps {
  breadcrumbs: Breadcrumb[];
  currentPage: string;
}

export default function Breadcrumbs({ breadcrumbs, currentPage }: BreadcrumbsProps) {
  if (breadcrumbs.length === 0) {
    return null;
  }

  return (
    <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
      {breadcrumbs.map((breadcrumb, index) => (
        <span key={breadcrumb.slug} className="flex items-center">
          <Link
            href={breadcrumb.slug}
            className="hover:text-blue-600 transition-colors"
          >
            {breadcrumb.breadcrumbTitle}
          </Link>
          <span className="mx-2">/</span>
        </span>
      ))}
      <span className="text-gray-900 font-medium">{currentPage}</span>
    </nav>
  );
}
