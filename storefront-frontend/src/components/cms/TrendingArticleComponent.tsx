import type { TrendingArticleComponent as TrendingArticleComponentType } from '@/types';

export default function TrendingArticleComponent({
  title,
  articleSlugs,
}: TrendingArticleComponentType) {
  // Note: In a real implementation, this would fetch articles by slugs from an API
  // For now, displaying as a placeholder component
  
  if (!articleSlugs || articleSlugs.length === 0) {
    return null;
  }

  return (
    <div className="my-12 bg-gradient-to-r from-purple-50 to-pink-50 p-8 rounded-lg">
      <h2 className="text-3xl font-bold mb-6 text-gray-900 flex items-center gap-2">
        <span className="text-2xl">🔥</span>
        {title}
      </h2>
      <div className="space-y-4">
        {articleSlugs.map((slug, index) => (
          <article 
            key={slug || index}
            className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow flex gap-4"
          >
            <div className="flex-shrink-0 w-16 h-16 bg-gradient-to-br from-purple-200 to-pink-200 rounded-lg flex items-center justify-center">
              <span className="text-2xl">📌</span>
            </div>
            <div className="flex-1">
              <h3 className="text-xl font-semibold mb-2 text-gray-900">
                Trending Article: {slug}
              </h3>
              <p className="text-gray-600 text-sm mb-2">
                This trending article is getting lots of attention...
              </p>
              <a 
                href={`/articles/${slug}`} 
                className="text-purple-600 hover:text-purple-700 font-medium text-sm"
              >
                Read article →
              </a>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
