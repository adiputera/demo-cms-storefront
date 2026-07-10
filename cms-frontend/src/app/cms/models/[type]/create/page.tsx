'use client';

import React, { use } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { cmsApiClient } from '@/lib/cms-api-client';
import CmsForm from '@/components/cms/CmsForm';

export default function GenericCreatePage({ params }: { params: Promise<{ type: string }> }) {
  const router = useRouter();
  const { type } = use(params);

  const handleSubmit = async (data: Record<string, any>) => {
    await cmsApiClient.createEntity(type, data);
    router.push(`/cms/models/${type}`);
    router.refresh();
  };

  const handleCancel = () => {
    router.push(`/cms/models/${type}`);
  };

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
        <span className="text-xs text-gray-500 font-sans">New</span>
      </div>

      <CmsForm
        type={type}
        mode="create"
        onSubmit={handleSubmit}
        onCancel={handleCancel}
      />
    </div>
  );
}
