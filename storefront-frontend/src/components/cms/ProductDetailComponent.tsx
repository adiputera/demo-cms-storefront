import type { Product } from '@/types';
import Image from 'next/image';

interface ProductDetailComponentProps {
  product?: Product;
  title?: string;
  showPrice?: boolean;
  showDescription?: boolean;
}

export default function ProductDetailComponent({
  product,
  title,
  showPrice = true,
  showDescription = true,
}: ProductDetailComponentProps) {
  if (!product) {
    return (
      <div className="p-8 border border-dashed border-gray-300 rounded-lg text-center bg-gray-50">
        <p className="text-gray-500 font-medium">Product Details Component (No product context)</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100 max-w-5xl mx-auto my-8">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 p-8 md:p-12">
        {/* Left Side: Product Image */}
        <div className="relative h-96 w-full rounded-xl overflow-hidden shadow-inner bg-gray-50 flex items-center justify-center border border-gray-100">
          <Image
            src={product.imageUrl}
            alt={product.name}
            fill
            className="object-contain p-6 hover:scale-105 transition-transform duration-500"
            sizes="(max-width: 768px) 100vw, 50vw"
            priority
          />
        </div>

        {/* Right Side: Product Details */}
        <div className="flex flex-col justify-between">
          <div>
            {/* Optional Section Title */}
            {title && (
              <span className="text-xs font-bold uppercase tracking-widest text-blue-600 mb-2 block">
                {title}
              </span>
            )}
            
            <h1 className="text-3xl md:text-4xl font-extrabold text-gray-900 leading-tight mb-4">
              {product.name}
            </h1>
            
            <div className="flex items-center gap-2 mb-6">
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-50 text-green-700 border border-green-200">
                In Stock
              </span>
              <span className="text-xs text-gray-500">Free shipping</span>
            </div>

            {showPrice !== false && (
              <div className="mb-6">
                <span className="text-3xl md:text-4xl font-black text-blue-600">
                  ${product.price.toFixed(2)}
                </span>
              </div>
            )}

            {showDescription !== false && (
              <div className="border-t border-gray-100 pt-6">
                <h3 className="text-sm font-semibold text-gray-900 mb-2">Description</h3>
                <p className="text-gray-600 leading-relaxed text-sm">
                  {product.description}
                </p>
              </div>
            )}
          </div>

          <div className="mt-8 space-y-3">
            <button className="w-full bg-blue-600 text-white font-bold py-3.5 px-6 rounded-xl hover:bg-blue-700 active:bg-blue-800 transition shadow-lg shadow-blue-200">
              Add to Cart
            </button>
            <button className="w-full bg-gray-50 text-gray-900 font-semibold py-3.5 px-6 rounded-xl hover:bg-gray-100 border border-gray-200 transition">
              Add to Wishlist
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
