import { cmsApiClient } from '@/lib/cms-api-client';
import Link from 'next/link';
import SyncSingleItemButton from '../components/SyncSingleItemButton';
import SyncButton from '../components/SyncButton';

export default async function PagesListPage() {
  let pages = [];
  let error = null;

  try {
    const response = await cmsApiClient.getAllPages();
    pages = response.data || [];
  } catch (err: any) {
    error = err.message;
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Pages</h1>
        <div className="flex gap-4">
          <SyncButton catalogId="contentCatalog" />
          <Link
            href="/cms/pages/new"
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Create Page
          </Link>
        </div>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          Error loading pages: {error}
        </div>
      )}

      {pages.length === 0 && !error && (
        <div className="text-center py-12 text-gray-500">
          <p className="text-lg">No pages found</p>
          <p className="mt-2">Create your first page to get started</p>
        </div>
      )}

      {pages.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Title</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Slug</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Meta Title</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Status</th>
                <th className="text-right py-3 px-4 font-semibold text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {pages.map((page: any) => (
                <tr key={page.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="py-3 px-4 font-semibold text-gray-900">{page.title}</td>
                  <td className="py-3 px-4">
                    <code className="text-sm bg-gray-100 text-gray-800 border border-gray-200 px-2 py-1 rounded font-mono">{page.slug}</code>
                  </td>
                  <td className="py-3 px-4 text-gray-700">{page.metaTitle || '-'}</td>
                  <td className="py-3 px-4">
                    {page.syncStatus === 'SYNCED' && <span className="px-2 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full">🟢 Synced</span>}
                    {page.syncStatus === 'OUT_OF_SYNC' && <span className="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs font-semibold rounded-full">🟡 Out of Sync</span>}
                    {page.syncStatus === 'NOT_SYNCED' && <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs font-semibold rounded-full">⚪ Not Synced</span>}
                  </td>
                  <td className="py-3 px-4 text-right">
                    <div className="flex gap-2 justify-end">
                      <a
                        href={`http://localhost:3000${page.slug}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-blue-600 hover:text-blue-800"
                      >
                        View
                      </a>
                      <Link
                        href={`/cms/pages/${page.id}/manage`}
                        className="text-purple-600 hover:text-purple-800"
                      >
                        Manage Content
                      </Link>
                      <Link
                        href={`/cms/pages/${page.id}/edit`}
                        className="text-gray-600 hover:text-gray-800"
                      >
                        Edit Page
                      </Link>
                      <SyncSingleItemButton entityType="Page" itemId={page.id} />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
