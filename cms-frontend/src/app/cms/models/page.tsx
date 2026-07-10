'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { cmsApiClient } from '@/lib/cms-api-client';

interface ModelInfo {
  type: string;
  displayName: string;
}

export default function ModelsRegistryPage() {
  const [models, setModels] = useState<ModelInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchModels() {
      try {
        const res = await cmsApiClient.getTypes();
        setModels(res.data || []);
      } catch (err: any) {
        console.error('Error fetching CMS models:', err);
        setError(err.message || 'Failed to load models');
      } finally {
        setLoading(false);
      }
    }
    fetchModels();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 text-red-700 p-4 rounded-lg font-sans">
        Error loading models: {error}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="relative rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 p-6 text-white overflow-hidden shadow-lg">
        <div className="relative z-10">
          <h1 className="text-3xl font-extrabold font-sans">Domain Models Registry</h1>
          <p className="mt-2 text-blue-100 font-sans max-w-xl">
            Explore and inspect metadata-driven storefront schemas. Click any model to view, search, and manage its generic entity records.
          </p>
        </div>
        <div className="absolute top-0 right-0 transform translate-x-12 -translate-y-12 opacity-10 pointer-events-none">
          <svg width="400" height="400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1">
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {models.map((model) => (
          <div
            key={model.type}
            className="group bg-white rounded-xl border border-gray-150 p-6 hover:shadow-xl hover:border-blue-300 transition-all duration-300 flex flex-col justify-between"
          >
            <div>
              <div className="h-12 w-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center group-hover:bg-blue-600 group-hover:text-white transition-colors duration-300 mb-4">
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                </svg>
              </div>
              <h2 className="text-xl font-bold text-gray-900 group-hover:text-blue-600 transition-colors duration-300 font-sans">
                {model.displayName}
              </h2>
              <p className="text-sm text-gray-500 mt-1 font-mono">
                Type Code: {model.type}
              </p>
            </div>
            
            <div className="mt-6 pt-4 border-t border-gray-100 flex items-center justify-between">
              <span className="text-xs text-gray-400 font-sans">Autodiscovered Schema</span>
              <Link
                href={`/cms/models/${model.type}`}
                className="inline-flex items-center justify-center px-4 py-2 text-sm font-semibold text-blue-600 bg-blue-50 rounded-lg group-hover:bg-blue-600 group-hover:text-white hover:bg-blue-100 transition-all duration-200"
              >
                See Data
                <svg className="ml-1.5 h-4 w-4 transform group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
