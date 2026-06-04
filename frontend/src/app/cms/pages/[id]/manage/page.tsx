'use client';

import { useState, useEffect, use } from 'react';
import { useRouter } from 'next/navigation';
import { cmsApiClient } from '@/lib/cms-api-client';

interface Slot {
  id: number;
  code: string;
  name: string;
  components: Component[];
}

interface Component {
  id: number;
  uid: string;
  name: string;
  type: string;
  sortOrder: number;
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
      setPage(pageData);
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

  const handleAddComponent = async (componentData: any) => {
    try {
      await cmsApiClient.createComponent(componentData);
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

  const handleDeleteComponent = async (id: number) => {
    if (!confirm('Delete this component?')) return;
    
    try {
      await cmsApiClient.deleteComponent(id);
      await loadPageData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleMoveComponent = async (componentId: number, direction: 'up' | 'down') => {
    const slot = slots.find(s => s.components.some(c => c.id === componentId));
    if (!slot) return;

    const componentIndex = slot.components.findIndex(c => c.id === componentId);
    const newOrder = direction === 'up' ? componentIndex - 1 : componentIndex + 1;
    
    if (newOrder < 0 || newOrder >= slot.components.length) return;

    try {
      await cmsApiClient.reorderComponent(componentId, newOrder);
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
                  <div>
                    <h3 className="text-xl font-bold text-gray-900">{slot.name}</h3>
                    <p className="text-sm text-gray-500">Code: {slot.code}</p>
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
                              </div>
                              <p className="text-sm text-gray-600 mt-1">UID: {component.uid}</p>
                              <ComponentPreview component={component} />
                            </div>
                            <div className="flex gap-2">
                              <button
                                onClick={() => handleMoveComponent(component.id, 'up')}
                                disabled={index === 0}
                                className="px-2 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                ↑
                              </button>
                              <button
                                onClick={() => handleMoveComponent(component.id, 'down')}
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
                                onClick={() => handleDeleteComponent(component.id)}
                                className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700"
                              >
                                Delete
                              </button>
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
            <p><strong>Links:</strong> {component.links?.length || 0} items</p>
          </div>
        );
      case 'QUICK_MENU':
        return (
          <div className="text-sm text-gray-600 mt-2">
            <p><strong>Tiles:</strong> {component.tiles?.length || 0} items</p>
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

// Component Form Modal (Simplified - shows type selector and basic fields)
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
  const [type, setType] = useState(component?.type || 'BANNER');
  const [uid, setUid] = useState(component?.uid || '');
  const [name, setName] = useState(component?.name || '');
  const [sortOrder, setSortOrder] = useState(component?.sortOrder || 0);
  
  // Type-specific fields
  const [fields, setFields] = useState<any>(component || {});

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const baseData = {
      uid,
      name,
      type,
      sortOrder,
      slotId,
    };

    const componentData = { ...baseData, ...fields };
    onSave(componentData);
  };

  const renderTypeSpecificFields = () => {
    switch (type) {
      case 'BANNER':
        return (
          <>
            <input
              type="text"
              placeholder="Image URL"
              value={fields.imageUrl || ''}
              onChange={(e) => setFields({ ...fields, imageUrl: e.target.value })}
              className="w-full px-3 py-2 border rounded-md mb-2"
            />
            <input
              type="text"
              placeholder="Title"
              value={fields.title || ''}
              onChange={(e) => setFields({ ...fields, title: e.target.value })}
              className="w-full px-3 py-2 border rounded-md mb-2"
            />
            <input
              type="text"
              placeholder="Subtitle"
              value={fields.subtitle || ''}
              onChange={(e) => setFields({ ...fields, subtitle: e.target.value })}
              className="w-full px-3 py-2 border rounded-md mb-2"
            />
            <input
              type="text"
              placeholder="CTA Text"
              value={fields.ctaText || ''}
              onChange={(e) => setFields({ ...fields, ctaText: e.target.value })}
              className="w-full px-3 py-2 border rounded-md mb-2"
            />
            <input
              type="text"
              placeholder="CTA URL"
              value={fields.ctaUrl || ''}
              onChange={(e) => setFields({ ...fields, ctaUrl: e.target.value })}
              className="w-full px-3 py-2 border rounded-md"
            />
          </>
        );
      case 'PARAGRAPH':
        return (
          <>
            <input
              type="text"
              placeholder="Title (optional)"
              value={fields.title || ''}
              onChange={(e) => setFields({ ...fields, title: e.target.value })}
              className="w-full px-3 py-2 border rounded-md mb-2"
            />
            <textarea
              placeholder="Content (HTML allowed)"
              value={fields.content || ''}
              onChange={(e) => setFields({ ...fields, content: e.target.value })}
              rows={6}
              className="w-full px-3 py-2 border rounded-md"
            />
          </>
        );
      case 'PRODUCT_CAROUSEL':
        return (
          <>
            <input
              type="text"
              placeholder="Title"
              value={fields.title || ''}
              onChange={(e) => setFields({ ...fields, title: e.target.value })}
              className="w-full px-3 py-2 border rounded-md mb-2"
            />
            <input
              type="text"
              placeholder="Product Codes (comma-separated)"
              value={Array.isArray(fields.productCodes) ? fields.productCodes.join(',') : fields.productCodes || ''}
              onChange={(e) => setFields({ ...fields, productCodes: e.target.value.split(',').map(s => s.trim()) })}
              className="w-full px-3 py-2 border rounded-md"
            />
            <p className="text-xs text-gray-500 mt-1">e.g., macbook-pro, iphone-15-pro</p>
          </>
        );
      default:
        return <p className="text-sm text-gray-600">Type-specific fields will appear here</p>;
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 overflow-y-auto">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl my-8">
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
              <option value="BANNER">Banner</option>
              <option value="PARAGRAPH">Paragraph</option>
              <option value="PRODUCT_CAROUSEL">Product Carousel</option>
              <option value="NAVIGATION">Navigation</option>
              <option value="QUICK_MENU">Quick Menu</option>
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

          <div className="mb-4">
            <label className="block text-sm font-medium mb-2">Component-Specific Fields</label>
            {renderTypeSpecificFields()}
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
              {component ? 'Update' : 'Create'} Component
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
