'use client';

import { useState, useEffect, use } from 'react';
import { useRouter } from 'next/navigation';
import { cmsApiClient } from '@/lib/cms-api-client';
import SyncSingleItemButton from '../../../components/SyncSingleItemButton';
import ImageUploader from '@/components/ImageUploader';

interface Slot {
  id: number;
  code: string;
  name: string;
  syncStatus?: string;
  components: Component[];
}


interface Component {
  id: number;
  uid: string;
  name: string;
  type: string;
  sortOrder: number;
  syncStatus?: string;
  [key: string]: any;
}

export default function PageManagementPage({ params }: { params: Promise<{ id: string }> }) {
  const unwrappedParams = use(params);
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<any>(null);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<Slot | null>(null);
  const [showAddSlot, setShowAddSlot] = useState(false);
  const [showAddComponent, setShowAddComponent] = useState(false);
  const [editingComponent, setEditingComponent] = useState<Component | null>(null);

  useEffect(() => {
    loadPageData();
  }, []);

  const loadPageData = async () => {
    try {
      setLoading(true);
      const pageData = await cmsApiClient.getPage(parseInt(unwrappedParams.id));
      const slotsData = await cmsApiClient.getSlotsByPage(parseInt(unwrappedParams.id));
      setPage(pageData.data);
      setSlots(slotsData);
      setLoading(false);
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };

  const handleAddSlot = async (slotData: { code: string; name: string }) => {
    try {
      await cmsApiClient.createSlot({
        ...slotData,
        pageId: parseInt(unwrappedParams.id),
      });
      setShowAddSlot(false);
      await loadPageData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleDeleteSlot = async (slotId: number) => {
    if (!confirm('Delete this slot and all its components?')) return;
    
    try {
      await cmsApiClient.deleteSlot(slotId);
      await loadPageData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleAddComponent = async (componentData: any, isExisting?: boolean) => {
    try {
      if (isExisting) {
        await cmsApiClient.linkComponent(componentData.slotId, componentData.componentId, componentData.sortOrder);
      } else {
        await cmsApiClient.createComponent(componentData);
      }
      setShowAddComponent(false);
      setSelectedSlot(null);
      await loadPageData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleUpdateComponent = async (id: number, componentData: any) => {
    try {
      await cmsApiClient.updateComponent(id, componentData);
      setEditingComponent(null);
      await loadPageData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleDeleteComponent = async (slotId: number, id: number) => {
    if (!confirm('Remove this component from the slot?')) return;
    
    try {
      await cmsApiClient.removeComponentFromSlot(slotId, id);
      await loadPageData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleMoveComponent = async (slotId: number, componentId: number, direction: 'up' | 'down') => {
    const slot = slots.find(s => s.id === slotId);
    if (!slot) return;

    const componentIndex = slot.components.findIndex(c => c.id === componentId);
    const newOrder = direction === 'up' ? componentIndex - 1 : componentIndex + 1;
    
    if (newOrder < 0 || newOrder >= slot.components.length) return;

    try {
      await cmsApiClient.reorderComponent(slotId, componentId, newOrder);
      await loadPageData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  if (loading) {
    return (
      <div className="p-8">
        <p className="text-gray-600">Loading page management...</p>
      </div>
    );
  }

  if (!page) {
    return (
      <div className="p-8">
        <p className="text-red-600">Page not found</p>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-6">
        <button
          onClick={() => router.push('/cms/pages')}
          className="text-blue-600 hover:text-blue-800 mb-4"
        >
          ← Back to Pages
        </button>
        <h1 className="text-3xl font-bold text-gray-900">Manage Page: {page.title}</h1>
        <p className="text-gray-600 mt-2">Slug: {page.slug}</p>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-md">
          <p className="text-red-800">{error}</p>
        </div>
      )}

      {/* Slots Section */}
      <div className="mb-8">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold text-gray-900">Slots</h2>
          <button
            onClick={() => setShowAddSlot(true)}
            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
          >
            + Add Slot
          </button>
        </div>

        {slots.length === 0 ? (
          <p className="text-gray-600">No slots yet. Add your first slot to get started.</p>
        ) : (
          <div className="space-y-6">
            {slots.map((slot) => (
              <div key={slot.id} className="border border-gray-300 rounded-lg p-6 bg-white">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-center gap-3">
                    <div>
                      <h3 className="text-xl font-bold text-gray-900">{slot.name}</h3>
                      <p className="text-sm text-gray-500">Code: {slot.code}</p>
                    </div>
                    {slot.syncStatus === 'SYNCED' && <span className="px-2 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full">🟢 Synced</span>}
                    {slot.syncStatus === 'OUT_OF_SYNC' && <span className="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs font-semibold rounded-full">🟡 Out of Sync</span>}
                    {slot.syncStatus === 'NOT_SYNCED' && <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs font-semibold rounded-full">⚪ Not Synced</span>}
                  </div>
                  <div className="space-x-2">
                    <button
                      onClick={() => {
                        setSelectedSlot(slot);
                        setShowAddComponent(true);
                      }}
                      className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700"
                    >
                      + Add Component
                    </button>
                    <button
                      onClick={() => handleDeleteSlot(slot.id)}
                      className="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700"
                    >
                      Delete Slot
                    </button>
                    <SyncSingleItemButton
                      entityType="Slot"
                      itemId={slot.id}
                      className="px-3 py-1 bg-green-600 text-white text-sm rounded hover:bg-green-700"
                    />
                  </div>
                </div>

                {/* Components in this slot */}
                {slot.components.length === 0 ? (
                  <p className="text-gray-500 text-sm">No components in this slot</p>
                ) : (
                  <div className="space-y-3">
                    {slot.components
                      .sort((a, b) => a.sortOrder - b.sortOrder)
                      .map((component, index) => (
                        <div
                          key={component.id}
                          className="border border-gray-200 rounded p-4 bg-gray-50"
                        >
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <div className="flex items-center gap-3">
                                <span className="px-2 py-1 bg-purple-100 text-purple-800 text-xs font-semibold rounded">
                                  {component.type}
                                </span>
                                <span className="font-semibold text-gray-900">{component.name}</span>
                                <span className="text-xs text-gray-500">#{component.sortOrder}</span>
                                {component.syncStatus === 'SYNCED' && <span className="px-2 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full">🟢 Synced</span>}
                                {component.syncStatus === 'OUT_OF_SYNC' && <span className="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs font-semibold rounded-full">🟡 Out of Sync</span>}
                                {component.syncStatus === 'NOT_SYNCED' && <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs font-semibold rounded-full">⚪ Not Synced</span>}
                              </div>
                              <p className="text-sm text-gray-600 mt-1">UID: {component.uid}</p>
                              <ComponentPreview component={component} />
                            </div>
                            <div className="flex gap-2">
                              <button
                                onClick={() => handleMoveComponent(slot.id, component.id, 'up')}
                                disabled={index === 0}
                                className="px-2 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                ↑
                              </button>
                              <button
                                onClick={() => handleMoveComponent(slot.id, component.id, 'down')}
                                disabled={index === slot.components.length - 1}
                                className="px-2 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                ↓
                              </button>
                              <button
                                onClick={() => setEditingComponent(component)}
                                className="px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeleteComponent(slot.id, component.id)}
                                className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700"
                              >
                                Remove
                              </button>
                              <SyncSingleItemButton
                                entityType="Component"
                                itemId={component.id}
                                className="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700"
                              />
                            </div>
                          </div>
                        </div>
                      ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Slot Modal */}
      {showAddSlot && (
        <SlotFormModal
          onClose={() => setShowAddSlot(false)}
          onSave={handleAddSlot}
        />
      )}

      {/* Add Component Modal */}
      {showAddComponent && selectedSlot && (
        <ComponentFormModal
          slotId={selectedSlot.id}
          onClose={() => {
            setShowAddComponent(false);
            setSelectedSlot(null);
          }}
          onSave={handleAddComponent}
        />
      )}

      {/* Edit Component Modal */}
      {editingComponent && (
        <ComponentFormModal
          component={editingComponent}
          slotId={slots.find(s => s.components.some(c => c.id === editingComponent.id))?.id || 0}
          onClose={() => setEditingComponent(null)}
          onSave={(data) => handleUpdateComponent(editingComponent.id, data)}
        />
      )}
    </div>
  );
}

// Component Preview
function ComponentPreview({ component }: { component: Component }) {
  const renderPreview = () => {
    switch (component.type) {
      case 'BANNER':
        return (
          <div className="text-sm text-gray-600 mt-2">
            <p><strong>Title:</strong> {component.title || 'N/A'}</p>
            <p><strong>Subtitle:</strong> {component.subtitle || 'N/A'}</p>
            {component.ctaText && <p><strong>CTA:</strong> {component.ctaText} → {component.ctaUrl}</p>}
          </div>
        );
      case 'PARAGRAPH':
        return (
          <div className="text-sm text-gray-600 mt-2">
            <p><strong>Title:</strong> {component.title || 'N/A'}</p>
            <p className="truncate"><strong>Content:</strong> {component.content?.substring(0, 100)}...</p>
          </div>
        );
      case 'PRODUCT_CAROUSEL':
        return (
          <div className="text-sm text-gray-600 mt-2">
            <p><strong>Title:</strong> {component.title || 'N/A'}</p>
            <p><strong>Products:</strong> {Array.isArray(component.productCodes) ? component.productCodes.join(', ') : component.productCodes}</p>
          </div>
        );
      case 'NAVIGATION':
        return (
          <div className="text-sm text-gray-600 mt-2">
            <p><strong>Text:</strong> {component.displayText || 'N/A'}</p>
            <p><strong>URL:</strong> {component.url || 'N/A'}</p>
            {component.icon && <p><strong>Icon:</strong> {component.icon}</p>}
          </div>
        );
      case 'QUICK_MENU':
        return (
          <div className="text-sm text-gray-600 mt-2">
            <p><strong>Title:</strong> {component.title || 'N/A'}</p>
            <p><strong>URL:</strong> {component.url || 'N/A'}</p>
            <p><strong>Image:</strong> {component.imageUrl || 'N/A'}</p>
          </div>
        );
      case 'PRODUCT_DETAIL':
        return (
          <div className="text-sm text-gray-600 mt-2">
            <p><strong>Title Override:</strong> {component.title || 'N/A'}</p>
            <p><strong>Show Price:</strong> {component.showPrice !== false ? 'Yes' : 'No'}</p>
            <p><strong>Show Description:</strong> {component.showDescription !== false ? 'Yes' : 'No'}</p>
          </div>
        );
      default:
        return null;
    }
  };

  return <div>{renderPreview()}</div>;
}

// Slot Form Modal
function SlotFormModal({
  onClose,
  onSave,
}: {
  onClose: () => void;
  onSave: (data: { code: string; name: string }) => void;
}) {
  const [code, setCode] = useState('');
  const [name, setName] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave({ code, name });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 className="text-xl font-bold mb-4">Add New Slot</h3>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Code</label>
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              required
              placeholder="e.g., hero, content, footer"
              className="w-full px-3 py-2 border rounded-md"
            />
          </div>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="e.g., Hero Section"
              className="w-full px-3 py-2 border rounded-md"
            />
          </div>
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border rounded-md hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Add Slot
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// Component Form Modal (Dynamically generated using schema from backend)
function ComponentFormModal({
  component,
  slotId,
  onClose,
  onSave,
}: {
  component?: Component;
  slotId: number;
  onClose: () => void;
  onSave: (data: any) => void;
}) {
  const [componentTypes, setComponentTypes] = useState<any[]>([]);
  const [type, setType] = useState(component?.type || 'BANNER');
  const [uid, setUid] = useState(component?.uid || '');
  const [name, setName] = useState(component?.name || '');
  const [sortOrder, setSortOrder] = useState(component?.sortOrder || 0);
  
  const [fields, setFields] = useState<any>({});
  const [schema, setSchema] = useState<any>(null);
  const [loadingSchema, setLoadingSchema] = useState(true);
  const [products, setProducts] = useState<any[]>([]);
  const [productSearch, setProductSearch] = useState('');

  // Fetch available component types on mount
  useEffect(() => {
    const fetchTypes = async () => {
      try {
        const types = await cmsApiClient.getComponentTypes();
        setComponentTypes(types);
      } catch (err) {
        console.error('Error fetching component types:', err);
      }
    };
    fetchTypes();
  }, []);

  // Fetch schema whenever type changes
  useEffect(() => {
    const fetchSchema = async () => {
      try {
        setLoadingSchema(true);
        const schemaData = await cmsApiClient.getComponentSchema(type);
        setSchema(schemaData);
        
        if (schemaData && schemaData.fields && schemaData.fields.some((f: any) => f.type === 'multiple_products')) {
          try {
            const productsData = await cmsApiClient.getAllProducts();
            setProducts(productsData.data || productsData);
          } catch (err) {
            console.error('Error fetching products:', err);
          }
        }
        
        // Initialize fields from component values or schema defaults
        const initialFields: any = {};
        if (schemaData && schemaData.fields) {
          schemaData.fields.forEach((field: any) => {
            const val = component?.[field.name];
            if (field.type === 'array_string') {
              initialFields[field.name] = Array.isArray(val) ? val.join(', ') : val || '';
            } else if (field.type === 'multiple_products') {
              initialFields[field.name] = typeof val === 'string' ? val.split(',').map((s: string) => s.trim()).filter(Boolean) : (Array.isArray(val) ? val : []);
            } else if (field.type === 'boolean') {
              initialFields[field.name] = val !== undefined ? !!val : false;
            } else {
              initialFields[field.name] = val || '';
            }
          });
        }
        setFields(initialFields);
        setLoadingSchema(false);
      } catch (err) {
        console.error('Error fetching component schema:', err);
        setLoadingSchema(false);
      }
    };
    fetchSchema();
  }, [type, component]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Parse form values based on schema field types
    const parsedFields: any = {};
    if (schema && schema.fields) {
      schema.fields.forEach((field: any) => {
        const val = fields[field.name];
        if (field.type === 'array_string') {
          parsedFields[field.name] = typeof val === 'string'
            ? val.split(',').map((s: string) => s.trim()).filter(Boolean)
            : val || [];
        } else if (field.type === 'multiple_products') {
          parsedFields[field.name] = Array.isArray(val) ? val : (typeof val === 'string' ? val.split(',').map((s: string) => s.trim()).filter(Boolean) : []);
        } else if (field.type === 'boolean') {
          parsedFields[field.name] = !!val;
        } else {
          parsedFields[field.name] = val;
        }
      });
    }
    
    const baseData = {
      uid,
      name,
      type,
      sortOrder,
      slotId,
    };

    const componentData = { ...baseData, ...parsedFields };
    onSave(componentData);
  };

  const renderDynamicFields = () => {
    if (loadingSchema) {
      return (
        <div className="py-4 text-center text-gray-500 text-sm animate-pulse">
          Loading component details...
        </div>
      );
    }
    if (!schema || !schema.fields) {
      return (
        <div className="py-4 text-center text-red-500 text-sm">
          Failed to load fields schema.
        </div>
      );
    }

    return (
      <div className="space-y-4">
        {schema.fields.map((field: any) => (
          <div key={field.name} className="border-b border-gray-50 pb-3">
            <label className="block text-sm font-semibold text-gray-700 mb-1 flex items-center justify-between">
              <span>
                {field.displayName} {field.required && <span className="text-red-500">*</span>}
              </span>
            </label>
            {field.type === 'text' ? (
              <textarea
                value={fields[field.name] || ''}
                onChange={(e) => setFields({ ...fields, [field.name]: e.target.value })}
                required={field.required}
                placeholder={field.placeholder}
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
              />
            ) : field.type === 'boolean' ? (
              <div className="flex items-center mt-2">
                <input
                  type="checkbox"
                  id={`field-${field.name}`}
                  checked={!!fields[field.name]}
                  onChange={(e) => setFields({ ...fields, [field.name]: e.target.checked })}
                  className="h-4.5 w-4.5 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <label htmlFor={`field-${field.name}`} className="ml-2 text-sm text-gray-600 font-medium select-none">
                  Enable / Yes
                </label>
              </div>
            ) : field.type === 'image' ? (
              <div className="mt-2">
                <ImageUploader
                  value={fields[field.name] || ''}
                  onChange={(url) => setFields({ ...fields, [field.name]: url })}
                  placeholder={field.placeholder}
                  required={field.required}
                />
              </div>
            ) : field.type === 'multiple_products' ? (
              <div className="space-y-2 mt-2">
                <input
                  type="text"
                  placeholder="Search products..."
                  value={productSearch}
                  onChange={(e) => setProductSearch(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm mb-2"
                />
                <div className="max-h-60 overflow-y-auto border border-gray-200 rounded-md p-2 space-y-2 bg-gray-50">
                  {products.length === 0 && <p className="text-sm text-gray-500">No products found.</p>}
                  {products
                    .filter(p => p.name.toLowerCase().includes(productSearch.toLowerCase()) || p.code.toLowerCase().includes(productSearch.toLowerCase()))
                    .map(p => {
                      const isChecked = (fields[field.name] || []).includes(p.code);
                      return (
                        <label key={p.id} className="flex items-start space-x-3 cursor-pointer p-1 hover:bg-gray-100 rounded">
                          <input
                            type="checkbox"
                            checked={isChecked}
                            onChange={(e) => {
                              const currentSelected = fields[field.name] || [];
                              if (e.target.checked) {
                                setFields({ ...fields, [field.name]: [...currentSelected, p.code] });
                              } else {
                                setFields({ ...fields, [field.name]: currentSelected.filter((c: string) => c !== p.code) });
                              }
                            }}
                            className="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                          />
                          <div>
                            <p className="text-sm font-medium text-gray-900">{p.name}</p>
                            <p className="text-xs text-gray-500">{p.code} - ${p.price}</p>
                          </div>
                        </label>
                      );
                  })}
                </div>
              </div>
            ) : (
              <input
                type="text"
                value={fields[field.name] || ''}
                onChange={(e) => setFields({ ...fields, [field.name]: e.target.value })}
                required={field.required}
                placeholder={field.placeholder}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
              />
            )}
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-start justify-center z-50 overflow-y-auto">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl my-12">
        <h3 className="text-xl font-bold mb-4">
          {component ? 'Edit Component' : 'Add New Component'}
        </h3>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Component Type</label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
              disabled={!!component}
              className="w-full px-3 py-2 border rounded-md"
            >
              {componentTypes.length > 0 ? (
                componentTypes.map((t) => (
                  <option key={t.type} value={t.type}>
                    {t.displayName}
                  </option>
                ))
              ) : (
                <>
                  <option value="BANNER">Banner</option>
                  <option value="PARAGRAPH">Paragraph</option>
                  <option value="PRODUCT_CAROUSEL">Product Carousel</option>
                  <option value="NAVIGATION">Navigation</option>
                  <option value="QUICK_MENU">Quick Menu</option>
                  <option value="PRODUCT_DETAIL">Product Details</option>
                </>
              )}
            </select>
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">UID (Unique Identifier)</label>
            <input
              type="text"
              value={uid}
              onChange={(e) => setUid(e.target.value)}
              required
              placeholder="e.g., hero-banner-1"
              className="w-full px-3 py-2 border rounded-md"
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="e.g., Main Hero Banner"
              className="w-full px-3 py-2 border rounded-md"
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Sort Order</label>
            <input
              type="number"
              value={sortOrder}
              onChange={(e) => setSortOrder(parseInt(e.target.value))}
              required
              className="w-full px-3 py-2 border rounded-md"
            />
          </div>

          <div className="mb-4 border-t border-gray-100 pt-4">
            <label className="block text-sm font-semibold mb-2 text-gray-900">Component-Specific Fields</label>
            {renderDynamicFields()}
          </div>

          <div className="flex justify-end gap-2 border-t border-gray-100 pt-4 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border rounded-md hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              {component ? 'Update' : 'Create'} Component
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
