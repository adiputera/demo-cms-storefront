import type { LatestArticleComponent as LatestArticleComponentType } from '@/types';

export default function LatestArticleComponent({
  title,
  articleCount,
}: LatestArticleComponentType) {
  // Note: In a real implementation, this would fetch articles from an API
  // For now, displaying as a placeholder component
  
  return (
    <div className="my-12">
      <h2 className="text-3xl font-bold mb-6 text-gray-900">{title}</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: Math.min(articleCount, 6) }).map((_, index) => (
          <article 
            key={index}
            className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
          >
            <div className="h-48 bg-gradient-to-br from-blue-100 to-blue-200 flex items-center justify-center">
              <span className="text-blue-600 text-4xl font-bold">📰</span>
            </div>
            <div className="p-6">
              <h3 className="text-xl font-semibold mb-2 text-gray-900">
                Article {index + 1}
              </h3>
              <p className="text-gray-600 text-sm mb-4">
                Latest news and updates from our blog...
              </p>
              <a 
                href="#" 
                className="text-blue-600 hover:text-blue-700 font-medium text-sm"
              >
                Read more →
              </a>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
