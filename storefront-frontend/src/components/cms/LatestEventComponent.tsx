import type { LatestEventComponent as LatestEventComponentType } from '@/types';

export default function LatestEventComponent({
  title,
  eventSlugs,
}: LatestEventComponentType) {
  // Note: In a real implementation, this would fetch events by slugs from an API
  // For now, displaying as a placeholder component
  
  if (!eventSlugs || eventSlugs.length === 0) {
    return null;
  }

  return (
    <div className="my-12">
      <h2 className="text-3xl font-bold mb-6 text-gray-900">{title}</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {eventSlugs.map((slug, index) => (
          <article 
            key={slug || index}
            className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow border-l-4 border-green-500"
          >
            <div className="p-6">
              <div className="flex items-start gap-4">
                <div className="flex-shrink-0 w-16 h-16 bg-gradient-to-br from-green-100 to-green-200 rounded-lg flex items-center justify-center">
                  <span className="text-3xl">📅</span>
                </div>
                <div className="flex-1">
                  <h3 className="text-xl font-semibold mb-2 text-gray-900">
                    Event: {slug}
                  </h3>
                  <div className="text-sm text-gray-600 space-y-1 mb-3">
                    <p className="flex items-center gap-2">
                      <span>📍</span>
                      <span>Event Location</span>
                    </p>
                    <p className="flex items-center gap-2">
                      <span>🕐</span>
                      <span>Coming Soon</span>
                    </p>
                  </div>
                  <p className="text-gray-600 text-sm mb-3">
                    Join us for this exciting event...
                  </p>
                  <a 
                    href={`/events/${slug}`} 
                    className="text-green-600 hover:text-green-700 font-medium text-sm"
                  >
                    View details →
                  </a>
                </div>
              </div>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
