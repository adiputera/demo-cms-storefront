'use client';

import React, { useEffect, useState } from 'react';
import { cmsApiClient } from '@/lib/cms-api-client';
import ReferencePickerModal from './ReferencePickerModal';
import ImageUploader from '../ImageUploader';
import DateInput from './DateInput';
import DateTimeInput from './DateTimeInput';
import ArrayStringInput from './ArrayStringInput';
import FileUploader from './FileUploader';

interface FieldMetadata {
  name: string;
  displayName: string;
  type: 'STRING' | 'TEXT' | 'NUMBER' | 'BOOLEAN' | 'DATE' | 'DATETIME' | 'ARRAY_STRING' | 'REFERENCE' | 'IMAGE' | 'FILE' | 'ENUM';
  required: boolean;
  editableOnUpdate: boolean;
  placeholder: string;
  reference: string; // FQCN
  referenceCardinality?: 'SINGLE' | 'MULTIPLE';
  order: number;
  enumValues?: string[];
}

interface ItemMetadata {
  code: string;
  displayName: string;
  searchable: FieldMetadata[];
  columnShown: FieldMetadata[];
  fields: FieldMetadata[]; // Added list of fields from type metadata API
}

interface CmsFormProps {
  type: string;
  initialData?: Record<string, any>;
  mode: 'create' | 'update';
  onSubmit: (data: Record<string, any>) => Promise<void>;
  onCancel: () => void;
}

export default function CmsForm({
  type,
  initialData = {},
  mode,
  onSubmit,
  onCancel,
}: CmsFormProps) {
  const [fields, setFields] = useState<FieldMetadata[]>([]);
  const [displayName, setDisplayName] = useState('');
  const [formData, setFormData] = useState<Record<string, any>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Reference picker state
  const [pickerOpen, setPickerOpen] = useState(false);
  const [activePickerField, setActivePickerField] = useState<string | null>(null);
  const [activePickerType, setActivePickerType] = useState<string>('');

  useEffect(() => {
    async function fetchMetadataAndData() {
      setLoading(true);
      setError(null);
      try {
        // Fetch metadata to find the full schema fields list
        const apiData = await cmsApiClient.getMetadata(type);
        const meta = apiData.data;
        const metaFields = meta.fields || [];
        const finalFields = metaFields.length > 0 ? metaFields : [...meta.columnShown, ...meta.searchable];
        
        setFields(finalFields);
        setDisplayName(meta.displayName);

        // Prepopulate form data
        const initialFormValues: Record<string, any> = {};
        finalFields.forEach((f: FieldMetadata) => {
          if (initialData[f.name] !== undefined) {
            if (f.type === 'REFERENCE' && f.referenceCardinality === 'MULTIPLE') {
              const val = initialData[f.name];
              if (Array.isArray(val)) {
                initialFormValues[f.name] = val;
              } else if (typeof val === 'string' && val) {
                initialFormValues[f.name] = val.split(',').map(part => ({ id: part.trim(), displayName: part.trim() }));
              } else {
                initialFormValues[f.name] = [];
              }
            } else if (f.type === 'ARRAY_STRING') {
              const val = initialData[f.name];
              if (Array.isArray(val)) {
                initialFormValues[f.name] = val;
              } else {
                initialFormValues[f.name] = [];
              }
            } else {
              initialFormValues[f.name] = initialData[f.name];
            }
          } else {
            // Default values
            if (f.type === 'REFERENCE' && f.referenceCardinality === 'MULTIPLE') {
              initialFormValues[f.name] = [];
            } else if (f.type === 'ARRAY_STRING') {
              initialFormValues[f.name] = [];
            } else if (f.type === 'BOOLEAN') {
              initialFormValues[f.name] = false;
            } else {
              initialFormValues[f.name] = '';
            }
          }
        });
        setFormData(initialFormValues);
      } catch (err: any) {
        console.error('Error fetching metadata for form:', err);
        setError(err.message || 'Failed to initialize form');
      } finally {
        setLoading(false);
      }
    }
    fetchMetadataAndData();
  }, [type, JSON.stringify(initialData)]);

  const handleChange = (name: string, value: any) => {
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      // Before submitting, if any reference field values are stored as objects {id, displayName},
      // we pass the raw ID value to the backend since the backend ReferenceFieldHandler accepts it
      const payload: Record<string, any> = {};
      fields.forEach((f) => {
        const val = formData[f.name];
        if (f.type === 'REFERENCE') {
          if (f.referenceCardinality === 'MULTIPLE') {
            if (Array.isArray(val)) {
              payload[f.name] = val.map((item: any) => item.id);
            } else {
              payload[f.name] = [];
            }
          } else {
            if (val && typeof val === 'object') {
              payload[f.name] = val.id; // Extract reference ID
            } else {
              payload[f.name] = val;
            }
          }
        } else {
          payload[f.name] = val;
        }
      });

      await onSubmit(payload);
    } catch (err: any) {
      console.error('Error submitting form:', err);
      setError(err.message || 'Failed to save record');
    } finally {
      setSaving(false);
    }
  };

  const getReferenceTypeCode = (refClassStr: string) => {
    if (!refClassStr || refClassStr === 'java.lang.Void' || refClassStr.includes('Void')) return '';
    const parts = refClassStr.split('.');
    return parts[parts.length - 1].toLowerCase();
  };

  const openReferencePicker = (fieldName: string, refClassStr: string) => {
    const typeCode = getReferenceTypeCode(refClassStr);
    if (!typeCode) return;
    setActivePickerField(fieldName);
    setActivePickerType(typeCode);
    setPickerOpen(true);
  };

  const handleSelectReference = (selectedId: string, label: string) => {
    if (activePickerField) {
      const fieldMeta = fields.find(f => f.name === activePickerField);
      if (fieldMeta && fieldMeta.referenceCardinality === 'MULTIPLE') {
        const currentList = Array.isArray(formData[activePickerField]) ? formData[activePickerField] : [];
        if (!currentList.some((item: any) => item.id === selectedId)) {
          handleChange(activePickerField, [...currentList, { id: selectedId, displayName: label }]);
        }
      } else {
        handleChange(activePickerField, { id: selectedId, displayName: label });
      }
    }
    setPickerOpen(false);
    setActivePickerField(null);
  };

  const handleSelectMultipleReferences = (selectedList: { id: string; displayName: string }[]) => {
    if (activePickerField) {
      handleChange(activePickerField, selectedList);
    }
    setPickerOpen(false);
    setActivePickerField(null);
  };

  const handleRemoveReference = (fieldName: string, idToRemove: string) => {
    const currentList = Array.isArray(formData[fieldName]) ? formData[fieldName] : [];
    handleChange(fieldName, currentList.filter((item: any) => item.id !== idToRemove));
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="bg-white border border-gray-150 rounded-xl shadow-sm p-4 sm:p-6 max-w-2xl mx-auto">
      <div className="border-b border-gray-150 pb-4 mb-6">
        <h2 className="text-xl font-bold text-gray-900 font-sans">
          {mode === 'create' ? 'Create New' : 'Edit'} {displayName}
        </h2>
        <p className="text-xs text-gray-500 font-sans">
          Provide inputs for fields configured by the schema
        </p>
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 p-4 rounded-lg text-sm font-sans mb-6">
          {error}
        </div>
      )}

      <form onSubmit={handleFormSubmit} className="space-y-6">
        {fields
          .filter((field) => {
            // Filter out fields that are not editable for this operation
            // editableOnCreate = required || editableOnUpdate
            const isEditable = mode === 'create' ? (field.required || field.editableOnUpdate) : field.editableOnUpdate;
            return isEditable;
          })
          .map((field) => {
          const isRequired = field.required;
          const isEditable = mode === 'create' ? (field.required || field.editableOnUpdate) : field.editableOnUpdate;

          // Render inputs
          return (
            <div key={field.name} className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-gray-700 font-sans flex items-center gap-1">
                {field.displayName}
                {isRequired && <span className="text-red-500 font-bold">*</span>}
              </label>

              {field.type === 'BOOLEAN' ? (
                <div className="flex items-center h-10">
                  <input
                    type="checkbox"
                    checked={!!formData[field.name]}
                    disabled={!isEditable}
                    onChange={(e) => handleChange(field.name, e.target.checked)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer"
                  />
                  <span className="ml-2.5 text-sm text-gray-600 font-sans">Active</span>
                </div>
              ) : field.type === 'ENUM' ? (
                <select
                  value={formData[field.name] || ''}
                  disabled={!isEditable}
                  required={isRequired}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  className="bg-white border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none disabled:bg-gray-50 disabled:text-gray-500 font-sans"
                >
                  <option value="">-- Select {field.displayName} --</option>
                  {field.enumValues?.map((val) => (
                    <option key={val} value={val}>
                      {val}
                    </option>
                  ))}
                </select>
              ) : field.type === 'TEXT' ? (
                <textarea
                  value={formData[field.name] || ''}
                  disabled={!isEditable}
                  required={isRequired}
                  placeholder={field.placeholder || `Enter ${field.displayName.toLowerCase()}...`}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  rows={4}
                  className="bg-white border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none disabled:bg-gray-50 disabled:text-gray-500 font-sans"
                />
              ) : field.type === 'NUMBER' ? (
                <input
                  type="number"
                  step="any"
                  value={
                    formData[field.name] && typeof formData[field.name] === 'object'
                      ? formData[field.name].value
                      : formData[field.name] || ''
                  }
                  disabled={!isEditable}
                  required={isRequired}
                  placeholder={field.placeholder || '0.00'}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  className="bg-white border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none disabled:bg-gray-50 disabled:text-gray-500 font-sans"
                />
              ) : field.type === 'DATE' ? (
                <DateInput
                  value={formData[field.name] || ''}
                  onChange={(value) => handleChange(field.name, value)}
                  placeholder={field.placeholder || 'Select date...'}
                  required={isRequired}
                  disabled={!isEditable}
                />
              ) : field.type === 'DATETIME' ? (
                <DateTimeInput
                  value={formData[field.name] || ''}
                  onChange={(value) => handleChange(field.name, value)}
                  placeholder={field.placeholder || 'Select date and time...'}
                  required={isRequired}
                  disabled={!isEditable}
                />
              ) : field.type === 'ARRAY_STRING' ? (
                <ArrayStringInput
                  value={formData[field.name] || []}
                  onChange={(value) => handleChange(field.name, value)}
                  placeholder={field.placeholder || 'Type and press Enter to add...'}
                  required={isRequired}
                  disabled={!isEditable}
                />
              ) : field.type === 'IMAGE' ? (
                <ImageUploader
                  value={formData[field.name] || ''}
                  onChange={(url) => handleChange(field.name, url)}
                  placeholder={field.placeholder || 'Upload or paste image URL'}
                  required={isRequired}
                />
              ) : field.type === 'FILE' ? (
                <FileUploader
                  value={formData[field.name] || ''}
                  onChange={(url) => handleChange(field.name, url)}
                  placeholder={field.placeholder || 'Upload or paste file URL'}
                  required={isRequired}
                />
              ) : field.type === 'REFERENCE' ? (
                <div className="flex flex-col gap-2">
                  {field.referenceCardinality === 'MULTIPLE' ? (
                    <div className="flex flex-wrap gap-2 min-h-[42px] p-2 bg-gray-50 border border-gray-300 rounded-lg">
                      {Array.isArray(formData[field.name]) && formData[field.name].length > 0 ? (
                        formData[field.name].map((item: any) => (
                          <span
                            key={item.id}
                            className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-700 font-sans text-xs font-semibold rounded-full border border-blue-200"
                          >
                            {item.displayName || item.id}
                            {isEditable && (
                              <button
                                type="button"
                                onClick={() => handleRemoveReference(field.name, item.id)}
                                className="text-blue-500 hover:text-blue-700 font-bold focus:outline-none"
                              >
                                &times;
                              </button>
                            )}
                          </span>
                        ))
                      ) : (
                        <span className="text-gray-400 text-sm py-1 px-2 font-sans self-center">None selected</span>
                      )}
                    </div>
                  ) : (
                    <div className="flex-1 bg-gray-50 border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 flex items-center text-gray-700 min-h-[42px] font-sans">
                      {formData[field.name]?.displayName || formData[field.name]?.id || (
                        <span className="text-gray-400">None selected</span>
                      )}
                    </div>
                  )}
                  {isEditable && (
                    <button
                      type="button"
                      onClick={() => openReferencePicker(field.name, field.reference)}
                      className="px-4 py-2 border border-gray-300 rounded-lg bg-white text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed self-start min-h-[42px]"
                    >
                      {field.referenceCardinality === 'MULTIPLE' ? 'Add Reference' : 'Select'}
                    </button>
                  )}
                </div>
              ) : (
                <input
                  type="text"
                  value={formData[field.name] || ''}
                  disabled={!isEditable}
                  required={isRequired}
                  placeholder={field.placeholder || `Enter ${field.displayName.toLowerCase()}...`}
                  onChange={(e) => handleChange(field.name, e.target.value)}
                  className="bg-white border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none disabled:bg-gray-50 disabled:text-gray-500 font-sans"
                />
              )}
            </div>
          );
        })}

        {/* Form controls */}
        <div className="flex justify-end gap-3 border-t border-gray-150 pt-6 mt-8">
          <button
            type="button"
            onClick={onCancel}
            className="px-4 py-2 text-sm font-semibold text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="inline-flex items-center px-4 py-2 text-sm font-semibold rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none transition-colors disabled:bg-blue-400 disabled:cursor-wait"
          >
            {saving ? 'Saving...' : 'Save Record'}
          </button>
        </div>
      </form>

      {/* Reference Modal */}
      {pickerOpen && (
        <ReferencePickerModal
          isOpen={pickerOpen}
          referenceType={activePickerType}
          onClose={() => {
            setPickerOpen(false);
            setActivePickerField(null);
          }}
          onSelect={handleSelectReference}
          onSelectMultiple={handleSelectMultipleReferences}
          isMultiple={
            fields.find((f) => f.name === activePickerField)?.referenceCardinality === 'MULTIPLE'
          }
          initialSelected={
            (() => {
              if (!activePickerField) return [];
              const val = formData[activePickerField];
              if (Array.isArray(val)) return val;
              if (val && typeof val === 'object') return [val];
              return [];
            })()
          }
        />
      )}
    </div>
  );
}
