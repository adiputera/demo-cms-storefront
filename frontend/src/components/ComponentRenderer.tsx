import type { Component } from '@/types';
import BannerComponent from '@/components/cms/BannerComponent';
import ParagraphComponent from '@/components/cms/ParagraphComponent';
import ProductCarouselComponent from '@/components/cms/ProductCarouselComponent';
import NavigationComponent from '@/components/cms/NavigationComponent';
import QuickMenuComponent from '@/components/cms/QuickMenuComponent';

const componentRegistry = {
  BANNER: BannerComponent,
  PARAGRAPH: ParagraphComponent,
  PRODUCT_CAROUSEL: ProductCarouselComponent,
  NAVIGATION: NavigationComponent,
  QUICK_MENU: QuickMenuComponent,
};

interface ComponentRendererProps {
  component: Component;
}

export default function ComponentRenderer({ component }: ComponentRendererProps) {
  const ComponentToRender = componentRegistry[component.type];

  if (!ComponentToRender) {
    console.error(`Unknown component type: ${component.type}`);
    return null;
  }

  return <ComponentToRender {...component} />;
}
