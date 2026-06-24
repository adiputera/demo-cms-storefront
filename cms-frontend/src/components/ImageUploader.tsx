'use client';

import { useState, useRef } from 'react';
import { cmsApiClient } from '@/lib/cms-api-client';

interface ImageUploaderProps {
  value: string;
  onChange: (url: string) => void;
  placeholder?: string;
  required?: boolean;
}

export default function ImageUploader({ value, onChange, placeholder, required }: ImageUploaderProps) {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Optional: add client side file type validation
    if (!file.type.startsWith('image/')) {
      setError('Please select an image file (e.g., .jpg, .png, .webp).');
      return;
    }

    setUploading(true);
    setError(null);

    try {
      const response = await cmsApiClient.uploadMedia(file);
      if (response.url) {
        onChange(response.url);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to upload image.');
    } finally {
      setUploading(false);
      // Reset input so the same file can be selected again if needed
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  return (
    <div className="space-y-3">
      {value ? (
        <div className="relative group rounded-md border border-gray-200 overflow-hidden w-full max-w-xs aspect-video bg-gray-50 flex items-center justify-center">
          <img src={value} alt="Preview" className="max-w-full max-h-full object-contain" />
          <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              type="button"
              onClick={() => onChange('')}
              className="px-3 py-1 bg-red-600 text-white text-sm font-medium rounded hover:bg-red-700 transition-colors shadow-sm"
            >
              Remove Image
            </button>
          </div>
        </div>
      ) : (
        <div 
          className={`w-full border-2 border-dashed rounded-lg p-6 flex flex-col items-center justify-center transition-colors
            ${uploading ? 'bg-gray-50 border-gray-300' : 'bg-gray-50 hover:bg-gray-100 border-gray-300'}`}
        >
          {uploading ? (
            <div className="flex flex-col items-center justify-center space-y-2">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              <p className="text-sm text-gray-500">Uploading...</p>
            </div>
          ) : (
            <>
              <svg className="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <p className="text-sm text-gray-600 font-medium mb-1">Click to upload an image</p>
              <p className="text-xs text-gray-500">PNG, JPG, WebP up to 10MB</p>
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="mt-4 px-4 py-2 bg-white border border-gray-300 rounded shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all"
              >
                Select File
              </button>
            </>
          )}
        </div>
      )}

      {/* Hidden file input */}
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        accept="image/*"
        className="hidden"
      />
      
      {/* Hidden text input for form compatibility/manual entry fallback */}
      <div className="flex gap-2 items-center mt-2">
        <input
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder || "Or paste image URL here"}
          required={required && !value}
          className="flex-1 block w-full text-sm border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 px-3 py-2 border"
        />
      </div>

      {error && <p className="text-sm text-red-600 mt-1">{error}</p>}
    </div>
  );
}
