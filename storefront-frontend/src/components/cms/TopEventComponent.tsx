import type { TopEventComponent as TopEventComponentType } from '@/types';

export default function TopEventComponent({
  title,
  eventSlug,
}: TopEventComponentType) {
  // Note: In a real implementation, this would fetch the event by slug from an API
  // For now, displaying as a placeholder component

  if (!eventSlug) {
    return null;
  }

  return (
    <div className="my-12">
      <div className="bg-gradient-to-r from-yellow-400 via-orange-400 to-red-400 rounded-lg overflow-hidden shadow-xl">
        <div className="p-8 md:p-12">
          <div className="max-w-3xl">
            <div className="inline-block bg-white/90 backdrop-blur-sm text-orange-600 px-4 py-2 rounded-full text-sm font-bold mb-4">
              ⭐ FEATURED EVENT
            </div>
            <h2 className="text-4xl md:text-5xl font-bold mb-4 text-white">
              {title}
            </h2>
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-6 mb-6 text-white">
              <h3 className="text-2xl font-semibold mb-3">
                {eventSlug}
              </h3>
              <div className="space-y-2 text-lg">
                <p className="flex items-center gap-3">
                  <span className="text-2xl">📍</span>
                  <span>Event Venue</span>
                </p>
                <p className="flex items-center gap-3">
                  <span className="text-2xl">📅</span>
                  <span>Event Date</span>
                </p>
                <p className="flex items-center gap-3">
                  <span className="text-2xl">🕐</span>
                  <span>Event Time</span>
                </p>
              </div>
            </div>
            <p className="text-white text-lg mb-6 leading-relaxed">
              Don&apos;t miss this incredible event! Join us for an unforgettable experience.
            </p>
            <a 
              href={`/events/${eventSlug}`}
              className="inline-block bg-white text-orange-600 hover:bg-gray-100 font-bold px-8 py-4 rounded-lg transition-colors text-lg shadow-lg"
            >
              Register Now →
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
