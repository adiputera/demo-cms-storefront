'use client';

import React, { useEffect, useState } from 'react';
import { cmsApiClient, SearchCriteria } from '@/lib/cms-api-client';

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

interface ReferencePickerModalProps {
  isOpen: boolean;
  onClose: () => void;
  referenceType: string;
  onSelect: (selectedId: string, displayName: string) => void;
  onSelectMultiple?: (selected: { id: string; displayName: string }[]) => void;
  isMultiple?: boolean;
  initialSelected?: { id: string; displayName: string }[];
}

export default function ReferencePickerModal({
  isOpen,
  onClose,
  referenceType,
  onSelect,
  onSelectMultiple,
  isMultiple = false,
  initialSelected = [],
}: ReferencePickerModalProps) {
  const [metadata, setMetadata] = useState<ItemMetadata | null>(null);
  const [data, setData] = useState<CmsRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<Record<string, { value: string; operator: string }>>({});
  const [selectedItems, setSelectedItems] = useState<{ id: string; displayName: string }[]>([]);

  useEffect(() => {
    if (isOpen) {
      setSelectedItems(initialSelected || []);
    }
  }, [isOpen, initialSelected]);

  useEffect(() => {
    if (!isOpen || !referenceType || referenceType === 'Void') return;

    async function fetchMetadata() {
      setLoading(true);
      setError(null);
      try {
        const res = await cmsApiClient.getMetadata(referenceType.toLowerCase());
        setMetadata(res.data);

        const initialFilters: typeof filters = {};
        res.data.searchable.forEach((f: FieldMetadata) => {
          initialFilters[f.name] = {
            value: '',
            operator: f.type === 'number' ? 'EQUALS' : 'CONTAINS',
          };
        });
        setFilters(initialFilters);
      } catch (err: any) {
        console.error('Error fetching metadata for reference picker:', err);
        setError(err.message || 'Failed to load reference model metadata');
        setLoading(false);
      }
    }
    fetchMetadata();
  }, [isOpen, referenceType]);

  const executeQuery = async () => {
    if (!metadata || !isOpen) return;
    setSearching(true);
    try {
      const criteriaList = Object.entries(filters)
        .filter(([_, filter]) => filter.value.trim() !== '')
        .map(([field, filter]) => ({
          field,
          value: filter.value,
          operator: filter.operator,
        }));

      const res = await cmsApiClient.searchData(referenceType.toLowerCase(), criteriaList);
      setData(res.data || []);
    } catch (err: any) {
      console.error('Error searching reference data:', err);
      setError(err.message || 'Failed to fetch record list');
    } finally {
      setSearching(false);
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!metadata || !isOpen) return;
    const delayDebounceFn = setTimeout(() => {
      executeQuery();
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [filters, metadata, isOpen]);

  const formatCellValue = (value: any) => {
    if (value === null || value === undefined) {
      return <span className="text-gray-400 font-sans">—</span>;
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
    return <span className="text-gray-700 font-sans">{value.toString()}</span>;
  };

  const getRowLabel = (row: CmsRow): string => {
    // Attempt to guess display name label
    const nameField = Object.keys(row.values).find(
      (k) =>
        k.toLowerCase().includes('name') ||
        k.toLowerCase().includes('title') ||
        k.toLowerCase().includes('code')
    );
    if (nameField && row.values[nameField]) {
      const val = row.values[nameField];
      return typeof val === 'object' && val.formatted !== undefined ? val.formatted : val.toString();
    }
    return `ID: ${row.id}`;
  };

  const handleToggleSelect = (row: CmsRow) => {
    const label = getRowLabel(row);
    const rowId = row.id;
    const isSelected = selectedItems.some((item) => String(item.id) === String(rowId));

    if (isMultiple) {
      if (isSelected) {
        setSelectedItems((prev) => prev.filter((item) => String(item.id) !== String(rowId)));
      } else {
        setSelectedItems((prev) => [...prev, { id: rowId, displayName: label }]);
      }
    } else {
      onSelect(rowId, label);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-xl max-w-4xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        {/* Header */}
        <div className="px-6 py-4 bg-gray-50 border-b border-gray-150 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-gray-900 font-sans">
              Select {metadata?.displayName || referenceType}
            </h2>
            <p className="text-xs text-gray-500 font-sans">
              Choose a record to reference in the field
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 focus:outline-none transition-colors"
          >
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {error && (
            <div className="bg-red-50 text-red-700 p-4 rounded-lg text-sm font-sans">
              {error}
            </div>
          )}

          {metadata && metadata.searchable.length > 0 && (
            <div className="bg-gray-50 rounded-xl border border-gray-150 p-4 grid grid-cols-1 md:grid-cols-2 gap-4">
              {metadata.searchable.map((field) => (
                <div key={field.name} className="flex flex-col gap-1">
                  <label className="text-xs font-semibold text-gray-600 font-sans">
                    {field.displayName}
                  </label>
                  <input
                    type={field.type === 'number' ? 'number' : 'text'}
                    value={filters[field.name]?.value || ''}
                    placeholder={`Search by ${field.displayName.toLowerCase()}...`}
                    onChange={(e) => {
                      setFilters((prev) => ({
                        ...prev,
                        [field.name]: {
                          ...prev[field.name],
                          value: e.target.value,
                        },
                      }));
                    }}
                    className="bg-white border border-gray-300 rounded-lg text-xs px-3 py-2 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
                  />
                </div>
              ))}
            </div>
          )}

          {/* Table container */}
          <div className="border border-gray-150 rounded-xl overflow-hidden shadow-sm relative min-h-[250px]">
            {searching && (
              <div className="absolute inset-0 bg-white bg-opacity-65 z-10 flex justify-center items-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            )}

            <div className="overflow-x-auto max-h-[350px]">
              <table className="min-w-full divide-y divide-gray-150">
                <thead className="bg-gray-50 sticky top-0 z-10">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-bold text-gray-500 uppercase tracking-wider font-sans">
                      ID
                    </th>
                    {metadata?.columnShown.map((col) => (
                      <th
                        key={col.name}
                        className="px-6 py-3 text-left text-xs font-bold text-gray-500 uppercase tracking-wider font-sans"
                      >
                        {col.displayName}
                      </th>
                    ))}
                    <th className="px-6 py-3 text-right text-xs font-bold text-gray-500 uppercase tracking-wider font-sans">
                      Action
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-100">
                  {loading ? (
                    Array.from({ length: 3 }).map((_, idx) => (
                      <tr key={idx} className="animate-pulse">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="h-4 bg-gray-200 rounded w-8"></div>
                        </td>
                        {metadata?.columnShown.map((col) => (
                          <td key={col.name} className="px-6 py-4 whitespace-nowrap">
                            <div className="h-4 bg-gray-200 rounded w-24"></div>
                          </td>
                        ))}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="h-4 bg-gray-200 rounded w-16 ml-auto"></div>
                        </td>
                      </tr>
                    ))
                  ) : data.length === 0 ? (
                    <tr>
                      <td
                        colSpan={(metadata?.columnShown.length || 0) + 2}
                        className="px-6 py-12 text-center text-sm text-gray-500 font-sans"
                      >
                        No records found.
                      </td>
                    </tr>
                  ) : (
                    data.map((row) => (
                      <tr key={row.id} className="hover:bg-gray-50/50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap text-xs font-mono text-gray-400">
                          {row.id}
                        </td>
                        {metadata?.columnShown.map((col) => (
                          <td key={col.name} className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                            {formatCellValue(row.values[col.name])}
                          </td>
                        ))}
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          {selectedItems.some((item) => String(item.id) === String(row.id)) ? (
                            <button
                              onClick={() => handleToggleSelect(row)}
                              className="inline-flex items-center px-3 py-1.5 border border-red-300 text-xs font-semibold rounded-lg text-red-700 bg-red-50 hover:bg-red-100 focus:outline-none transition-colors"
                            >
                              Deselect
                            </button>
                          ) : (
                            <button
                              onClick={() => handleToggleSelect(row)}
                              className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-semibold rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none transition-colors"
                            >
                              Select
                            </button>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-gray-50 border-t border-gray-150 flex justify-end gap-2">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm font-semibold text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          {isMultiple && (
            <button
              onClick={() => {
                if (onSelectMultiple) {
                  onSelectMultiple(selectedItems);
                }
                onClose();
              }}
              className="px-4 py-2 text-sm font-semibold text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 transition-colors"
            >
              Choose ({selectedItems.length})
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
