'use client';

import { useState, useEffect, use } from 'react';
import { useRouter } from 'next/navigation';
import { cmsApiClient } from '@/lib/cms-api-client';

import ImageUploader from '@/components/ImageUploader';

export default function EditProductForm({ params }: { params: Promise<{ id: string }> }) {
  const unwrappedParams = use(params);
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    imageUrl: '',
    price: '',
    description: '',
  });

  useEffect(() => {
    loadProduct();
  }, []);

  const loadProduct = async () => {
    try {
      const response = await cmsApiClient.getProduct(parseInt(unwrappedParams.id));
      setFormData({
        ...response.data,
        price: response.data.price.toString(),
      });
      setLoading(false);
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      const productData = {
        ...formData,
        price: parseFloat(formData.price),
      };
      await cmsApiClient.updateProduct(parseInt(unwrappedParams.id), productData);
      router.push('/cms/products');
      router.refresh();
    } catch (err: any) {
      setError(err.message);
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    setError(null);

    try {
      await cmsApiClient.deleteProduct(parseInt(unwrappedParams.id));
      router.push('/cms/products');
      router.refresh();
    } catch (err: any) {
      setError(err.message);
      setDeleting(false);
      setShowDeleteConfirm(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-gray-600">Loading product...</div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Edit Product</h1>
        <button
          onClick={() => setShowDeleteConfirm(true)}
          disabled={deleting}
          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:bg-gray-400"
        >
          Delete Product
        </button>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          {error}
        </div>
      )}

      {showDeleteConfirm && (
        <div className="mb-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <p className="text-yellow-800 font-medium mb-3">
            Are you sure you want to delete this product? This action cannot be undone.
          </p>
          <div className="flex gap-2">
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 disabled:bg-gray-400"
            >
              {deleting ? 'Deleting...' : 'Yes, Delete'}
            </button>
            <button
              onClick={() => setShowDeleteConfirm(false)}
              disabled={deleting}
              className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6 max-w-2xl">
        <div>
          <label htmlFor="code" className="block text-sm font-medium text-gray-700 mb-1">
            Product Code *
          </label>
          <input
            type="text"
            id="code"
            name="code"
            value={formData.code}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div>
          <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
            Product Name *
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div>
          <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-1">
            Price *
          </label>
          <input
            type="number"
            id="price"
            name="price"
            value={formData.price}
            onChange={handleChange}
            required
            step="0.01"
            min="0"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div>
          <label htmlFor="imageUrl" className="block text-sm font-medium text-gray-700 mb-1">
            Product Image
          </label>
          <div className="mt-1">
            <ImageUploader
              value={formData.imageUrl || ''}
              onChange={(url) => setFormData(prev => ({ ...prev, imageUrl: url }))}
              placeholder="https://images.unsplash.com/photo-..."
            />
          </div>
        </div>

        <div>
          <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
            Description
          </label>
          <textarea
            id="description"
            name="description"
            value={formData.description || ''}
            onChange={handleChange}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
          <button
            type="button"
            onClick={() => router.back()}
            className="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
