import type { BannerComponent as BannerComponentType } from '@/types';
import Image from 'next/image';
import Link from 'next/link';

export default function BannerComponent({
  imageUrl,
  altText,
  title,
  subtitle,
  ctaText,
  ctaUrl,
}: BannerComponentType) {
  return (
    <div className="relative w-full h-[500px] overflow-hidden rounded-lg">
      <Image
        src={imageUrl}
        alt={altText}
        fill
        className="object-cover"
        priority
      />
      <div className="absolute inset-0 bg-gradient-to-r from-black/60 to-transparent flex items-center">
        <div className="container mx-auto px-4">
          <div className="max-w-2xl text-white">
            <h1 className="text-5xl font-bold mb-4">{title}</h1>
            <p className="text-xl mb-8">{subtitle}</p>
            {ctaUrl && ctaText && (
              <Link
                href={ctaUrl}
                className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-semibold px-8 py-3 rounded-lg transition-colors"
              >
                {ctaText}
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
