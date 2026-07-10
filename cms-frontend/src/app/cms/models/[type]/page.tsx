'use client';

import React, { useEffect, useState, use } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { cmsApiClient } from '@/lib/cms-api-client';

interface FieldMetadata {
  name: string;
  displayName: string;
  type: string;
  order: number;
}

interface ItemMetadata {
  code: string;
  displayName: string;
  searchable: FieldMetadata[];
  columnShown: FieldMetadata[];
}

interface CmsRow {
  id: string;
  values: Record<string, any>;
}

export default function GenericDataTablePage({ params }: { params: Promise<{ type: string }> }) {
  const router = useRouter();
  const { type } = use(params);

  const [metadata, setMetadata] = useState<ItemMetadata | null>(null);
  const [data, setData] = useState<CmsRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Search filter criteria state
  const [filters, setFilters] = useState<Record<string, { value: string; operator: string }>>({});

  // Fetch schema metadata at startup
  useEffect(() => {
    async function fetchMetadata() {
      try {
        const res = await cmsApiClient.getMetadata(type);
        setMetadata(res.data);
        
        // Initialize filters state with default operators
        const initialFilters: typeof filters = {};
        res.data.searchable.forEach((f: FieldMetadata) => {
          initialFilters[f.name] = {
            value: '',
            operator: f.type === 'number' ? 'EQUALS' : 'CONTAINS',
          };
        });
        setFilters(initialFilters);
      } catch (err: any) {
        console.error('Error fetching metadata:', err);
        setError(err.message || 'Failed to load model metadata');
      }
    }
    fetchMetadata();
  }, [type]);

  // Function to execute generic query search
  const executeQuery = async () => {
    if (!metadata) return;
    setSearching(true);
    try {
      const criteriaList = Object.entries(filters)
        .filter(([_, filter]) => filter.value.trim() !== '')
        .map(([field, filter]) => ({
          field,
          value: filter.value,
          operator: filter.operator,
        }));

      const res = await cmsApiClient.searchData(type, criteriaList);
      setData(res.data || []);
    } catch (err: any) {
      console.error('Error searching data:', err);
      setError(err.message || 'Failed to fetch record list');
    } finally {
      setSearching(false);
      setLoading(false);
    }
  };

  // Trigger search when metadata is ready or filters change (debounced)
  useEffect(() => {
    if (!metadata) return;
    const delayDebounceFn = setTimeout(() => {
      executeQuery();
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [filters, metadata]);

  // Reset all search criteria
  const handleResetFilters = () => {
    if (!metadata) return;
    const reset: typeof filters = {};
    metadata.searchable.forEach((f) => {
      reset[f.name] = {
        value: '',
        operator: f.type === 'number' ? 'EQUALS' : 'CONTAINS',
      };
    });
    setFilters(reset);
  };

  // Format cell value gracefully for table presentation
  const formatCellValue = (value: any, fieldMeta?: FieldMetadata) => {
    if (value === null || value === undefined) {
      return <span className="text-gray-400 font-sans">—</span>;
    }
    
    // Render IMAGE type as thumbnail
    if (fieldMeta?.type?.toUpperCase() === 'IMAGE' && typeof value === 'string' && value.trim()) {
      return (
        <div className="flex items-center">
          <img 
            src={value} 
            alt="Preview" 
            className="h-12 w-12 object-cover rounded border border-gray-200"
            onError={(e) => {
              // Fallback if image fails to load
              e.currentTarget.style.display = 'none';
              e.currentTarget.nextElementSibling?.classList.remove('hidden');
            }}
          />
          <span className="hidden text-xs text-gray-500 font-mono truncate max-w-[200px]">{value}</span>
        </div>
      );
    }
    
    if (Array.isArray(value)) {
      if (value.length === 0) return <span className="text-gray-400 font-sans">—</span>;
      const isReferenceArray = value.every(item => item && typeof item === 'object' && ('displayName' in item || 'id' in item));
      if (isReferenceArray) {
        return (
          <div className="flex flex-wrap gap-1.5">
            {value.map((item, idx) => (
              <span key={idx} className="inline-flex items-center px-2 py-0.5 rounded bg-blue-50 text-blue-700 text-xs font-semibold font-sans border border-blue-100">
                {item.displayName || item.id}
              </span>
            ))}
          </div>
        );
      }
      return <span className="font-mono text-xs text-gray-600 font-sans">{JSON.stringify(value)}</span>;
    }
    if (typeof value === 'object') {
      if (value.formatted !== undefined) {
        return <span className="font-semibold text-gray-900 font-sans">{value.formatted}</span>;
      }
      if ('displayName' in value || 'id' in value) {
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded bg-blue-50 text-blue-700 text-xs font-semibold font-sans border border-blue-100">
            {value.displayName || value.id}
          </span>
        );
      }
      return <span className="font-mono text-xs text-gray-600 font-sans">{JSON.stringify(value)}</span>;
    }
    if (typeof value === 'boolean') {
      return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium font-sans ${value ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
          {value ? 'True' : 'False'}
        </span>
      );
    }
    if (typeof value === 'string' && value.length > 0 && value === value.toUpperCase() && /^[A-Z_]+$/.test(value)) {
      // Render enum-like values (all uppercase with underscores) as badges
      return (
        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium font-sans bg-purple-100 text-purple-800">
          {value}
        </span>
      );
    }
    return <span className="text-gray-700 font-sans">{value.toString()}</span>;
  };

  if (error) {
    return (
      <div className="space-y-4">
        <Link href="/cms/models" className="text-sm font-semibold text-blue-600 hover:text-blue-800 flex items-center font-sans">
          ← Back to Registry
        </Link>
        <div className="bg-red-50 text-red-700 p-4 rounded-lg font-sans">
          {error}
        </div>
      </div>
    );
  }

  if (!metadata) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header Area */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between border-b border-gray-150 pb-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Link href="/cms/models" className="text-xs font-semibold text-blue-600 hover:text-blue-800 font-sans">
              Registry
            </Link>
            <span className="text-gray-300 text-xs">/</span>
            <span className="text-xs text-gray-500 font-sans">{metadata.displayName} Data</span>
          </div>
          <h1 className="text-3xl font-extrabold text-gray-900 font-sans flex flex-wrap items-center gap-3">
            {metadata.displayName} List
            <span className="text-sm bg-blue-50 text-blue-700 px-2.5 py-0.5 rounded-full font-mono font-medium break-all">
              type: {metadata.code}
            </span>
          </h1>
        </div>
        <div className="mt-4 md:mt-0 flex gap-2">
          <Link
            href={`/cms/models/${metadata.code}/create`}
            className="inline-flex items-center justify-center px-4 py-2 text-sm font-semibold text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 transition-colors font-sans"
          >
            + Create New
          </Link>
          <Link
            href="/cms/models"
            className="inline-flex items-center justify-center px-4 py-2 text-sm font-semibold text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-sans"
          >
            ← Back to Registry
          </Link>
        </div>
      </div>

      {/* Dynamic Search Filters section */}
      {metadata.searchable.length > 0 && (
        <div className="bg-gray-50 rounded-xl border border-gray-150 p-6 space-y-4 shadow-sm">
          <div className="flex justify-between items-center pb-2 border-b border-gray-200">
            <h2 className="text-sm font-semibold text-gray-800 font-sans flex items-center gap-2">
              <svg className="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
              </svg>
              Filter Records
            </h2>
            <button
              onClick={handleResetFilters}
              className="text-xs text-blue-600 hover:text-blue-800 font-medium font-sans"
            >
              Reset Filters
            </button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {metadata.searchable.map((field) => (
              <div key={field.name} className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-gray-600 font-sans">
                  {field.displayName}
                </label>
                <div className="flex gap-2">
                  <select
                    value={filters[field.name]?.operator || (field.type === 'number' ? 'EQUALS' : 'CONTAINS')}
                    onChange={(e) => {
                      setFilters(prev => ({
                        ...prev,
                        [field.name]: {
                          ...prev[field.name],
                          operator: e.target.value,
                        }
                      }));
                    }}
                    className="bg-white border border-gray-300 rounded-lg text-xs px-2 py-1.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none font-sans"
                  >
                    {field.type === 'number' ? (
                      <>
                        <option value="EQUALS">=</option>
                        <option value="MORE_THAN">&gt;</option>
                        <option value="LESS_THAN">&lt;</option>
                      </>
                    ) : (
                      <>
                        <option value="CONTAINS">Contains</option>
                        <option value="EQUALS">Equals</option>
                      </>
                    )}
                  </select>
                  <input
                    type={field.type === 'number' ? 'number' : 'text'}
                    value={filters[field.name]?.value || ''}
                    placeholder={`Filter by ${field.displayName.toLowerCase()}...`}
                    onChange={(e) => {
                      setFilters(prev => ({
                        ...prev,
                        [field.name]: {
                          ...prev[field.name],
                          value: e.target.value,
                        }
                      }));
                    }}
                    className="flex-1 bg-white border border-gray-300 rounded-lg text-xs px-3 py-1.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none font-sans"
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Dynamic Data Table */}
      <div className="bg-white border border-gray-150 rounded-xl overflow-hidden shadow-sm relative">
        {/* Searching Loader Overlay */}
        {searching && (
          <div className="absolute inset-0 bg-white bg-opacity-60 z-10 flex justify-center items-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        )}

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-150">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider font-sans">
                  ID
                </th>
                {metadata.columnShown.map((col) => (
                  <th
                    key={col.name}
                    className="px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider font-sans"
                  >
                    {col.displayName}
                  </th>
                ))}
                <th className="hidden sm:table-cell px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider font-sans">
                  Created At
                </th>
                <th className="px-6 py-3.5 text-right text-xs font-bold text-gray-500 uppercase tracking-wider font-sans">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-100">
              {loading ? (
                // Table skeleton
                Array.from({ length: 3 }).map((_, idx) => (
                  <tr key={idx} className="animate-pulse">
                    <td className="px-6 py-4 whitespace-nowrap"><div className="h-4 bg-gray-200 rounded w-8"></div></td>
                    {metadata.columnShown.map((col) => (
                      <td key={col.name} className="px-6 py-4 whitespace-nowrap">
                        <div className="h-4 bg-gray-200 rounded w-24"></div>
                      </td>
                    ))}
                    <td className="hidden sm:table-cell px-6 py-4 whitespace-nowrap"><div className="h-4 bg-gray-200 rounded w-32"></div></td>
                    <td className="px-6 py-4 whitespace-nowrap text-right"><div className="h-4 bg-gray-200 rounded w-16 ml-auto"></div></td>
                  </tr>
                ))
              ) : data.length === 0 ? (
                <tr>
                  <td
                    colSpan={metadata.columnShown.length + 3}
                    className="px-6 py-12 text-center text-sm text-gray-500 font-sans"
                  >
                    <svg className="mx-auto h-12 w-12 text-gray-300 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2-2v7m16 0a2 2 0 012 2v4a2 2 0 01-2 2H6a2 2 0 01-2-2v-4a2 2 0 012-2m16 0h-2m-12 0H4" />
                    </svg>
                    No records found for this model.
                  </td>
                </tr>
              ) : (
                data.map((row) => (
                  <tr key={row.id} className="hover:bg-gray-50/55 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-xs font-mono text-gray-400">
                      {row.id}
                    </td>
                    {metadata.columnShown.map((col) => (
                      <td key={col.name} className="px-6 py-4 whitespace-nowrap text-sm">
                        {formatCellValue(row.values[col.name], col)}
                      </td>
                    ))}
                    <td className="hidden sm:table-cell px-6 py-4 whitespace-nowrap text-xs text-gray-500 font-mono">
                      {row.values.createdAt ? new Date(row.values.createdAt).toLocaleString() : '—'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                      <Link
                        href={`/cms/models/${metadata.code}/${row.id}/edit`}
                        className="text-blue-600 hover:text-blue-900 mr-3 font-sans font-semibold"
                      >
                        Edit
                      </Link>
                      <button
                        onClick={async () => {
                          if (confirm('Are you sure you want to delete this record?')) {
                            try {
                              await cmsApiClient.deleteEntity(metadata.code, row.id);
                              executeQuery();
                            } catch (err: any) {
                              alert(err.message || 'Failed to delete record');
                            }
                          }
                        }}
                        className="text-red-600 hover:text-red-900 font-sans font-semibold"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
