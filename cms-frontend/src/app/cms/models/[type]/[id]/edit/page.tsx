'use client';

import React, { use, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { cmsApiClient } from '@/lib/cms-api-client';
import CmsForm from '@/components/cms/CmsForm';

export default function GenericEditPage({
  params,
}: {
  params: Promise<{ type: string; id: string }>;
}) {
  const router = useRouter();
  const { type, id } = use(params);

  const [initialData, setInitialData] = useState<Record<string, any> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchEntityData() {
      try {
        const res = await cmsApiClient.getEntity(type, id);
        // Backend returns CmsRowDTO with values map
        setInitialData(res.data.values || {});
      } catch (err: any) {
        console.error('Error fetching entity data:', err);
        setError(err.message || 'Failed to load entity record');
      } finally {
        setLoading(false);
      }
    }
    fetchEntityData();
  }, [type, id]);

  const handleSubmit = async (data: Record<string, any>) => {
    await cmsApiClient.updateEntity(type, id, data);
    router.push(`/cms/models/${type}`);
    router.refresh();
  };

  const handleCancel = () => {
    router.push(`/cms/models/${type}`);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-4 max-w-2xl mx-auto">
        <div className="bg-red-50 text-red-700 p-4 rounded-lg font-sans">
          {error}
        </div>
        <button
          onClick={handleCancel}
          className="px-4 py-2 border border-gray-300 rounded-lg bg-white text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors"
        >
          Back to List
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Breadcrumbs */}
      <div className="flex flex-wrap items-center gap-2 mb-1">
        <Link href="/cms/models" className="text-xs font-semibold text-blue-600 hover:text-blue-800 font-sans">
          Registry
        </Link>
        <span className="text-gray-300 text-xs">/</span>
        <Link href={`/cms/models/${type}`} className="text-xs font-semibold text-blue-600 hover:text-blue-800 font-sans">
          {type.charAt(0).toUpperCase() + type.slice(1)} List
        </Link>
        <span className="text-gray-300 text-xs">/</span>
        <span className="text-xs text-gray-500 font-sans">Edit (ID: {id})</span>
      </div>

      {initialData && (
        <CmsForm
          type={type}
          initialData={initialData}
          mode="update"
          onSubmit={handleSubmit}
          onCancel={handleCancel}
        />
      )}
    </div>
  );
}
