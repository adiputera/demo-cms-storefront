import type { QuickMenuComponent as QuickMenuComponentType } from '@/types';
import Image from 'next/image';
import Link from 'next/link';

export default function QuickMenuComponent({
  title,
  imageUrl,
  url,
}: QuickMenuComponentType) {
  return (
    <Link
      href={url}
      className="block bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden"
    >
      <div className="relative h-32 w-full">
        <Image
          src={imageUrl}
          alt={title}
          fill
          className="object-cover"
        />
      </div>
      <div className="p-4">
        <h3 className="font-semibold text-gray-900">{title}</h3>
      </div>
    </Link>
  );
}
