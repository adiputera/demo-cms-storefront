import type {
  Page,
  Product,
  Slot,
  ErrorResponse,
} from '@/types';

const isServer = typeof window === 'undefined';
const STOREFRONT_API_URL = isServer
  ? (process.env.STOREFRONT_API_URL_INTERNAL || 'http://storefront-backend:8080/api')
  : (process.env.NEXT_PUBLIC_STOREFRONT_API_URL || 'http://localhost:8080/api');

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const errorData: ErrorResponse = await response.json();
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    }
    return response.json();
  }

  /**
   * Fetch a page by slug
   */
  async getPageBySlug(slug: string): Promise<Page> {
    const normalizedSlug = slug.startsWith('/') ? slug.substring(1) : slug;
    const url = normalizedSlug === '' ? `${this.baseUrl}/pages/index` : `${this.baseUrl}/pages/${normalizedSlug}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store', // For dynamic content
    });
    
    return this.handleResponse<Page>(response);
  }

  /**
   * Fetch multiple slots with their components
   */
  async getSlotsByIds(slotIds: number[]): Promise<{ slots: Slot[] }> {
    const response = await fetch(`${this.baseUrl}/slots/details`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ slotIds }),
      cache: 'no-store',
    });
    
    return this.handleResponse<{ slots: Slot[] }>(response);
  }

  /**
   * Fetch all products
   */
  async getAllProducts(): Promise<Product[]> {
    const response = await fetch(`${this.baseUrl}/products`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      next: { revalidate: 300 }, // Cache for 5 minutes
    });
    
    return this.handleResponse<Product[]>(response);
  }

  /**
   * Fetch a single product by code
   */
  async getProductByCode(code: string): Promise<Product> {
    const response = await fetch(`${this.baseUrl}/products/${code}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      next: { revalidate: 300 },
    });
    
    return this.handleResponse<Product>(response);
  }

  /**
   * Fetch multiple products by codes
   */
  async getProductsByCodes(codes: string[]): Promise<Product[]> {
    const products = await Promise.all(
      codes.map(code => this.getProductByCode(code))
    );
    return products.filter(product => product !== null);
  }
}

// Export singleton instance
export const apiClient = new ApiClient(STOREFRONT_API_URL);
