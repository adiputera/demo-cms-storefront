'use client';

import React from 'react';

interface DateInputProps {
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  placeholder?: string;
  disabled?: boolean;
}

export default function DateInput({ 
  value, 
  onChange, 
  required, 
  placeholder,
  disabled 
}: DateInputProps) {
  return (
    <input
      type="date"
      value={value || ''}
      onChange={(e) => onChange(e.target.value)}
      required={required}
      placeholder={placeholder}
      disabled={disabled}
      className="bg-white border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none disabled:bg-gray-50 disabled:text-gray-500 font-sans"
    />
  );
}
