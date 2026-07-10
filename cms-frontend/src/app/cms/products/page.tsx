import { cmsApiClient } from '@/lib/cms-api-client';
import Link from 'next/link';
import SyncButton from '../components/SyncButton';
import StatusBadge from '../components/StatusBadge';

export default async function ProductsListPage() {
  let products = [];
  let error = null;

  try {
    const response = await cmsApiClient.getAllProducts();
    products = response.data || [];
  } catch (err: any) {
    error = err.message;
  }

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Products</h1>
        <div className="flex flex-wrap gap-3">
          <SyncButton catalogId="productCatalog" />
          <Link
            href="/cms/products/new"
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-semibold font-sans"
          >
            Create Product
          </Link>
        </div>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          Error loading products: {error}
        </div>
      )}

      {products.length === 0 && !error && (
        <div className="text-center py-12 text-gray-500">
          <p className="text-lg">No products found</p>
          <p className="mt-2">Create your first product to get started</p>
        </div>
      )}

      {products.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map((product: any) => (
            <div key={product.id} className="border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow relative">
              <div className="absolute top-2 right-2 z-10">
                <StatusBadge status={product.syncStatus} />
              </div>
              {product.imageUrl && (
                <div className="h-48 bg-gray-100 flex items-center justify-center">
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="max-h-full max-w-full object-contain"
                  />
                </div>
              )}
              <div className="p-4">
                <h3 className="font-semibold text-lg text-gray-900 mb-1">{product.name}</h3>
                <code className="text-sm bg-gray-100 px-2 py-1 rounded text-gray-600">
                  {product.code}
                </code>
                <p className="mt-2 text-gray-600 text-sm line-clamp-2">{product.description}</p>
                <div className="mt-3 flex items-center justify-between border-t pt-3 border-gray-100">
                  <span className="text-xl font-bold text-blue-600 font-sans">
                    ${product.price.toFixed(2)}
                  </span>
                  <div className="flex gap-2">
                    <Link
                      href={`/cms/products/${product.id}/edit`}
                      className="px-3 py-1 bg-blue-50 hover:bg-blue-100 text-blue-600 hover:text-blue-800 text-xs font-semibold rounded-md transition-colors font-sans"
                    >
                      Edit
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
