import type { NavigationComponent as NavigationComponentType } from '@/types';
import Link from 'next/link';

export default function NavigationComponent({
  displayText,
  url,
  icon,
}: NavigationComponentType) {
  return (
    <Link
      href={url}
      className="flex items-center gap-2 text-gray-700 hover:text-blue-600 transition-colors py-2 px-4 hover:bg-gray-50 rounded"
    >
      {icon && <span className="text-lg">{icon}</span>}
      <span>{displayText}</span>
    </Link>
  );
}
