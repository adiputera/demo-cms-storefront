import Link from 'next/link';
import { cmsApiClient } from '@/lib/cms-api-client';
export default async function CMSDashboard() {
  let stats = { totalPages: 0, totalProducts: 0, totalComponents: 0 };
  try {
    const statsResponse = await cmsApiClient.getDashboardStats();
    if (statsResponse) {
      stats = statsResponse;
    }
  } catch (err) {
    console.error("Error fetching dashboard stats:", err);
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900 font-sans">CMS Dashboard</h1>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Pages Card */}
        <Link href="/cms/pages" className="block">
          <div className="border border-gray-200 rounded-lg p-6 hover:shadow-lg transition-shadow bg-white">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900 font-sans">Pages</h2>
              <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <p className="text-gray-600 font-sans text-sm">
              Manage pages, slots, and components for your storefront
            </p>
          </div>
        </Link>

        {/* Products Card */}
        <Link href="/cms/products" className="block">
          <div className="border border-gray-200 rounded-lg p-6 hover:shadow-lg transition-shadow bg-white">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900 font-sans">Products</h2>
              <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <p className="text-gray-600 font-sans text-sm">
              Create and manage products for product carousels
            </p>
          </div>
        </Link>
      </div>

      {/* Quick Stats */}
      <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="text-blue-600 text-sm font-medium font-sans">Total Pages</div>
          <div className="text-2xl font-bold text-blue-900 mt-1 font-sans">{stats.totalPages}</div>
        </div>
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="text-green-600 text-sm font-medium font-sans">Total Products</div>
          <div className="text-2xl font-bold text-green-900 mt-1 font-sans">{stats.totalProducts}</div>
        </div>
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
          <div className="text-purple-600 text-sm font-medium font-sans">Active Components</div>
          <div className="text-2xl font-bold text-purple-900 mt-1 font-sans">{stats.totalComponents}</div>
        </div>
      </div>
    </div>
  );
}
