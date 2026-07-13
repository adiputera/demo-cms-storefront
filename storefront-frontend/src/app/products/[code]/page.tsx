import { apiClient } from '@/lib/api-client';
import Breadcrumbs from '@/components/Breadcrumbs';
import SlotRenderer from '@/components/SlotRenderer';
import type { Metadata } from 'next';
import { notFound } from 'next/navigation';

import type { Slot, Breadcrumb } from '@/types';

interface ProductPageProps {
  params: Promise<{
    code: string;
  }>;
}

// Generate metadata for the product detail page
export async function generateMetadata({ params }: ProductPageProps): Promise<Metadata> {
  const { code } = await params;
  try {
    const product = await apiClient.getProductByCode(code);
    if (!product) {
      return { title: 'Product Not Found' };
    }
    
    // Attempt to get the CMS page metadata for products/detail template
    let page;
    try {
      page = await apiClient.getPageBySlug('/products/detail');
    } catch {
      // Ignored
    }

    return {
      title: product.name + (page?.metaTitle ? ` | ${page.metaTitle}` : ' | Storefront'),
      description: product.description || page?.metaDescription,
      openGraph: {
        title: product.name,
        description: product.description,
        images: product.imageUrl ? [product.imageUrl] : undefined,
      },
    };
  } catch (error) {
    console.error('Error generating product page metadata:', error);
    return {
      title: 'Product Details',
    };
  }
}

export default async function ProductDetailPage({ params }: ProductPageProps) {
  const { code } = await params;
  
  let product;
  try {
    product = await apiClient.getProductByCode(code);
  } catch (error) {
    console.error('Error fetching product:', error);
    return notFound();
  }

  if (!product) {
    return notFound();
  }

  // Load layout from CMS (template page with slug '/products/detail')
  let slotsWithComponents: Slot[] = [];
  let breadcrumbs: Breadcrumb[] = [];
  let pageTitle = product.name;

  try {
    // Attempt to load product specific CMS page first, e.g. /products/macbook-pro
    // Fallback to /products/detail template page if not found
    let page;
    try {
      page = await apiClient.getPageBySlug(`/products/${code}`);
    } catch {
      page = await apiClient.getPageBySlug('/products/detail');
    }

    if (page) {
      pageTitle = page.title || product.name;
      breadcrumbs = page.breadcrumbs || [];
      
      // Fetch components for slots
      if (page.slots && page.slots.length > 0) {
        const slotIds = page.slots.map(slot => slot.id);
        const { slots } = await apiClient.getSlotsByIds(slotIds);
        
        // Maintain the order defined by page.slots
        const slotsMap = new Map(slots.map(s => [s.id, s]));
        slotsWithComponents = page.slots
          .map(s => slotsMap.get(s.id))
          .filter((s): s is NonNullable<typeof s> => s !== undefined);
      }
    }
  } catch (error) {
    console.error('Error loading product layout CMS page, using default styling:', error);
  }

  // If no slots/components loaded, render a basic fallback layout
  const hasContent = slotsWithComponents.length > 0;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {breadcrumbs.length > 0 ? (
          <Breadcrumbs
            breadcrumbs={breadcrumbs}
            currentPage={product.name}
          />
        ) : (
          <div className="text-sm text-gray-500 mb-6">
            <a href="/" className="hover:text-blue-600 font-medium text-blue-600">Home</a> &gt; <span className="text-gray-900">{product.name}</span>
          </div>
        )}

        {hasContent ? (
          <div className="space-y-8">
            {slotsWithComponents
              .sort((a, b) => {
                const order = ['hero', 'content', 'footer'];
                return order.indexOf(a.code) - order.indexOf(b.code);
              })
              .map((slot) => (
                <SlotRenderer
                  key={slot.id}
                  slot={slot}
                  product={product}
                  className={slot.code === 'footer' ? 'border-t pt-8 mt-12' : ''}
                />
              ))}
          </div>
        ) : (
          // Fallback static layout if CMS page is missing or has no slots
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100 max-w-5xl mx-auto my-8 p-12">
            <h1 className="text-3xl font-extrabold text-gray-900 mb-4">{product.name}</h1>
            <p className="text-2xl text-blue-600 font-bold mb-4">${product.price.toFixed(2)}</p>
            <p className="text-gray-700 leading-relaxed">{product.description}</p>
          </div>
        )}
      </div>
    </div>
  );
}
