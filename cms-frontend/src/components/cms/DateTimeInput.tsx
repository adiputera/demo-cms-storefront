'use client';

import React from 'react';

interface DateTimeInputProps {
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  placeholder?: string;
  disabled?: boolean;
}

export default function DateTimeInput({ 
  value, 
  onChange, 
  required, 
  placeholder,
  disabled 
}: DateTimeInputProps) {
  // Convert ISO datetime with seconds to datetime-local format (no seconds)
  const toDateTimeLocalValue = (isoString: string) => {
    if (!isoString) return '';
    // If format is YYYY-MM-DDTHH:mm:ss, strip seconds
    if (isoString.length > 16) {
      return isoString.substring(0, 16);
    }
    return isoString;
  };

  // Ensure we send back proper ISO format
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const localValue = e.target.value; // YYYY-MM-DDTHH:mm
    if (!localValue) {
      onChange('');
      return;
    }
    // Add seconds if not present
    const isoValue = localValue.length === 16 ? localValue + ':00' : localValue;
    onChange(isoValue);
  };

  return (
    <input
      type="datetime-local"
      value={toDateTimeLocalValue(value)}
      onChange={handleChange}
      required={required}
      placeholder={placeholder}
      disabled={disabled}
      className="bg-white border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none disabled:bg-gray-50 disabled:text-gray-500 font-sans"
    />
  );
}
