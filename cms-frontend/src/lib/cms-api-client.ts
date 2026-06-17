const CMS_API_URL = process.env.NEXT_PUBLIC_CMS_API_URL || 'http://localhost:8081/api/cms';

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}

class CMSApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  // Pages
  async getAllPages() {
    const response = await fetch(`${this.baseUrl}/pages`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch pages: ${response.statusText}`);
    }
    return response.json();
  }

  async getPage(id: number) {
    const response = await fetch(`${this.baseUrl}/pages/${id}`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch page: ${response.statusText}`);
    }
    return response.json();
  }

  async createPage(page: any) {
    const response = await fetch(`${this.baseUrl}/pages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(page),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create page');
    }
    return response.json();
  }

  async updatePage(id: number, page: any) {
    const response = await fetch(`${this.baseUrl}/pages/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(page),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update page');
    }
    return response.json();
  }

  async deletePage(id: number) {
    const response = await fetch(`${this.baseUrl}/pages/${id}`, {
      method: 'DELETE',
      cache: 'no-store',
    });
    if (!response.ok) {
      throw new Error('Failed to delete page');
    }
    return response.json();
  }

  // Products
  async getAllProducts() {
    const response = await fetch(`${this.baseUrl}/products`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch products: ${response.statusText}`);
    }
    return response.json();
  }

  async getProduct(id: number) {
    const response = await fetch(`${this.baseUrl}/products/${id}`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch product: ${response.statusText}`);
    }
    return response.json();
  }

  async createProduct(product: any) {
    const response = await fetch(`${this.baseUrl}/products`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(product),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create product');
    }
    return response.json();
  }

  async updateProduct(id: number, product: any) {
    const response = await fetch(`${this.baseUrl}/products/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(product),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update product');
    }
    return response.json();
  }

  async deleteProduct(id: number) {
    const response = await fetch(`${this.baseUrl}/products/${id}`, {
      method: 'DELETE',
      cache: 'no-store',
    });
    if (!response.ok) {
      throw new Error('Failed to delete product');
    }
    return response.json();
  }

  // Slots
  async getSlotsByPage(pageId: number) {
    const response = await fetch(`${this.baseUrl}/slots/page/${pageId}`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch slots: ${response.statusText}`);
    }
    return response.json();
  }

  async getSlot(id: number) {
    const response = await fetch(`${this.baseUrl}/slots/${id}`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch slot: ${response.statusText}`);
    }
    return response.json();
  }

  async createSlot(slot: any) {
    const response = await fetch(`${this.baseUrl}/slots`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(slot),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create slot');
    }
    return response.json();
  }

  async updateSlot(id: number, slot: any) {
    const response = await fetch(`${this.baseUrl}/slots/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(slot),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update slot');
    }
    return response.json();
  }

  async deleteSlot(id: number) {
    const response = await fetch(`${this.baseUrl}/slots/${id}`, {
      method: 'DELETE',
      cache: 'no-store',
    });
    if (!response.ok) {
      throw new Error('Failed to delete slot');
    }
  }

  // Components
  async getAllComponents() {
    const response = await fetch(`${this.baseUrl}/components`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch components: ${response.statusText}`);
    }
    return response.json();
  }

  async linkComponent(slotId: number, componentId: number, sortOrder?: number) {
    const response = await fetch(`${this.baseUrl}/components/slots/${slotId}/components/${componentId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sortOrder }),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to link component');
    }
  }

  async createComponent(component: any) {
    const response = await fetch(`${this.baseUrl}/components`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(component),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create component');
    }
    return response.json();
  }

  async updateComponent(id: number, component: any) {
    const response = await fetch(`${this.baseUrl}/components/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(component),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update component');
    }
    return response.json();
  }

  async reorderComponent(slotId: number, componentId: number, sortOrder: number) {
    const response = await fetch(`${this.baseUrl}/components/slots/${slotId}/components/${componentId}/reorder`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sortOrder }),
      cache: 'no-store',
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to reorder component');
    }
  }

  async removeComponentFromSlot(slotId: number, componentId: number) {
    const response = await fetch(`${this.baseUrl}/components/slots/${slotId}/components/${componentId}`, {
      method: 'DELETE',
      cache: 'no-store',
    });
    if (!response.ok) {
      throw new Error('Failed to remove component from slot');
    }
  }

  async deleteComponent(id: number) {
    const response = await fetch(`${this.baseUrl}/components/${id}`, {
      method: 'DELETE',
      cache: 'no-store',
    });
    if (!response.ok) {
      throw new Error('Failed to delete component');
    }
  }

  // Component Types & Schema Metadata
  async getComponentTypes() {
    const response = await fetch(`${this.baseUrl}/components/types`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch component types: ${response.statusText}`);
    }
    return response.json();
  }

  async getComponentSchema(type: string) {
    const response = await fetch(`${this.baseUrl}/components/types/${type}/schema`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch component schema: ${response.statusText}`);
    }
    return response.json();
  }

  // Dashboard Stats
  async getDashboardStats() {
    const response = await fetch(`${this.baseUrl}/dashboard/stats`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch dashboard stats: ${response.statusText}`);
    }
    return response.json();
  }
}

export const cmsApiClient = new CMSApiClient(CMS_API_URL);
