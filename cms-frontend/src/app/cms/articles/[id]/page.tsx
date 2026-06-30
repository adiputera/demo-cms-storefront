'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { cmsApiClient } from '@/lib/cms-api-client';

export default function EditArticleForm() {
  const router = useRouter();
  const params = useParams();
  const articleId = typeof params.id === 'string' ? parseInt(params.id) : null;
  
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [formData, setFormData] = useState({
    title: '',
    slug: '',
    body: '',
  });

  useEffect(() => {
    if (!articleId) return;
    
    const fetchArticle = async () => {
      try {
        const response = await cmsApiClient.getArticle(articleId);
        if (response.data) {
          setFormData({
            title: response.data.title || '',
            slug: response.data.slug || '',
            body: response.data.body || '',
          });
        }
        setFetching(false);
      } catch (err: any) {
        setError(err.message);
        setFetching(false);
      }
    };
    
    fetchArticle();
  }, [articleId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!articleId) return;
    
    setLoading(true);
    setError(null);

    try {
      await cmsApiClient.updateArticle(articleId, formData);
      router.push('/cms/articles');
      router.refresh();
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };
  
  const handleDelete = async () => {
    if (!articleId) return;
    
    if (confirm('Are you sure you want to delete this article?')) {
      try {
        await cmsApiClient.deleteArticle(articleId);
        router.push('/cms/articles');
        router.refresh();
      } catch (err: any) {
        setError(err.message);
      }
    }
  };

  if (fetching) {
    return <div className="text-center py-12">Loading article...</div>;
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Edit Article</h1>
        <button
          type="button"
          onClick={handleDelete}
          className="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors"
        >
          Delete Article
        </button>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6 max-w-2xl">
        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
            Article Title *
          </label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div>
          <label htmlFor="slug" className="block text-sm font-medium text-gray-700 mb-1">
            Slug *
          </label>
          <input
            type="text"
            id="slug"
            name="slug"
            value={formData.slug}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div>
          <label htmlFor="body" className="block text-sm font-medium text-gray-700 mb-1">
            Body
          </label>
          <textarea
            id="body"
            name="body"
            value={formData.body}
            onChange={handleChange}
            rows={10}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={loading}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {loading ? 'Saving...' : 'Save Changes'}
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
