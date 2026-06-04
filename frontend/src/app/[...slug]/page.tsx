import type { Metadata } from 'next';
import { apiClient } from '@/lib/api-client';
import Breadcrumbs from '@/components/Breadcrumbs';
import SlotRenderer from '@/components/SlotRenderer';

interface PageProps {
  params: Promise<{
    slug: string[];
  }>;
}

// Generate metadata for SEO
export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const resolvedParams = await params;
  const slug = '/' + (resolvedParams.slug ? resolvedParams.slug.join('/') : '');
  
  try {
    const page = await apiClient.getPageBySlug(slug);
    
    return {
      title: page.metaTitle || page.title,
      description: page.metaDescription,
      keywords: page.metaKeywords,
      robots: {
        index: page.robotsIndex !== false,
        follow: page.robotsFollow !== false,
      },
      alternates: {
        canonical: page.canonicalUrl,
      },
      openGraph: {
        title: page.ogTitle || page.title,
        description: page.ogDescription || page.metaDescription,
        images: page.ogImage ? [page.ogImage] : undefined,
      },
    };
  } catch (error) {
    console.error('Error generating metadata:', error);
    return {
      title: 'Page Not Found',
    };
  }
}

export default async function DynamicPage({ params }: PageProps) {
  const resolvedParams = await params;
  const slug = '/' + (resolvedParams.slug ? resolvedParams.slug.join('/') : '');
  
  try {
    const page = await apiClient.getPageBySlug(slug);
    
    // Fetch slot details if slots exist
    let slotsWithComponents = page.slots || [];
    if (page.slots && page.slots.length > 0) {
      const slotIds = page.slots.map(slot => slot.id);
      const { slots } = await apiClient.getSlotsByIds(slotIds);
      slotsWithComponents = slots;
    }
    
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <Breadcrumbs
            breadcrumbs={page.breadcrumbs}
            currentPage={page.breadcrumbTitle || page.title}
          />
          
          <h1 className="text-4xl font-bold mb-8 text-gray-900">{page.title}</h1>
          
          <div className="space-y-8">
            {slotsWithComponents
              .sort((a, b) => {
                // Sort slots by their position (hero, content, footer)
                const order = ['hero', 'content', 'footer'];
                return order.indexOf(a.code) - order.indexOf(b.code);
              })
              .map((slot) => (
                <SlotRenderer
                  key={slot.id}
                  slot={slot}
                  className={slot.code === 'footer' ? 'border-t pt-8 mt-12' : ''}
                />
              ))}
          </div>
        </div>
      </div>
    );
  } catch (error) {
    console.error('Error loading page:', error);
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">Page Not Found</h1>
          <p className="text-gray-600">The page you're looking for doesn't exist.</p>
        </div>
      </div>
    );
  }
}
