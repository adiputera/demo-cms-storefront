import { cmsApiClient } from '@/lib/cms-api-client';
import Link from 'next/link';
import SyncButton from '../components/SyncButton';
import StatusBadge from '../components/StatusBadge';

export default async function ArticlesListPage() {
  let articles = [];
  let error = null;

  try {
    const response = await cmsApiClient.getAllArticles();
    articles = response.data || [];
  } catch (err: any) {
    error = err.message;
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Articles</h1>
        <div className="flex gap-4">
          <SyncButton catalogId="articleCatalog" />
          <Link
            href="/cms/articles/new"
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Create Article
          </Link>
        </div>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          Error loading articles: {error}
        </div>
      )}

      {articles.length === 0 && !error && (
        <div className="text-center py-12 text-gray-500">
          <p className="text-lg">No articles found</p>
          <p className="mt-2">Create your first article to get started</p>
        </div>
      )}

      {articles.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {articles.map((article: any) => (
            <div key={article.id} className="border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow relative bg-white">
              <div className="absolute top-2 right-2 z-10">
                <StatusBadge status={article.syncStatus} />
              </div>
              <div className="p-4 pt-8">
                <h3 className="font-semibold text-xl text-gray-900 mb-1 line-clamp-1">{article.title}</h3>
                <p className="text-xs text-blue-600 mb-2 font-mono bg-blue-50 px-2 py-1 rounded inline-block">/{article.slug}</p>
                <p className="mt-2 text-gray-600 text-sm line-clamp-3">{article.body}</p>
                <div className="mt-4 flex items-center justify-between border-t pt-3">
                  <span className="text-sm font-medium text-gray-500">
                    ID: {article.id}
                  </span>
                  <div className="flex gap-3">
                    <Link
                      href={`/cms/articles/${article.id}/edit`}
                      className="text-sm font-semibold text-blue-600 hover:text-blue-800"
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
