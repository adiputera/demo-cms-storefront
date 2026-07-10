import type { ProductCarouselComponent as ProductCarouselComponentType, Product } from '@/types';
import { apiClient } from '@/lib/api-client';
import ProductCard from '@/components/ProductCard';

export default async function ProductCarouselComponent({
  title,
  productCodes,
}: ProductCarouselComponentType) {
  const codes = Array.isArray(productCodes) 
    ? productCodes 
    : productCodes?.split(',').map(code => code.trim()).filter(Boolean) || [];
  
  // Fetch products
  let products: Product[] = [];
  try {
    if (codes.length > 0) {
      products = await apiClient.getProductsByCodes(codes);
    }
  } catch (error) {
    console.error('Error fetching products for carousel:', error);
  }

  if (products.length === 0) {
    return null;
  }

  return (
    <div className="my-12">
      {title && <h2 className="text-3xl font-bold mb-6 text-gray-900">{title}</h2>}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </div>
  );
}
