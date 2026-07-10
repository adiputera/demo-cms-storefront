'use client';

import React, { useState, useRef, KeyboardEvent } from 'react';

interface ArrayStringInputProps {
  value: string[];
  onChange: (value: string[]) => void;
  required?: boolean;
  placeholder?: string;
  disabled?: boolean;
}

export default function ArrayStringInput({ 
  value = [], 
  onChange, 
  required, 
  placeholder = 'Type and press Enter to add...',
  disabled 
}: ArrayStringInputProps) {
  const [inputValue, setInputValue] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  const handleAddItem = () => {
    const trimmed = inputValue.trim();
    if (trimmed && !value.includes(trimmed)) {
      onChange([...value, trimmed]);
      setInputValue('');
    }
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddItem();
    }
  };

  const handleRemoveItem = (indexToRemove: number) => {
    onChange(value.filter((_, index) => index !== indexToRemove));
  };

  return (
    <div className="space-y-2">
      {/* Display chips */}
      <div className="flex flex-wrap gap-2 min-h-[42px] p-2 bg-gray-50 border border-gray-300 rounded-lg">
        {value.length > 0 ? (
          value.map((item, index) => (
            <span
              key={index}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-700 font-sans text-xs font-semibold rounded-full border border-blue-200"
            >
              {item}
              {!disabled && (
                <button
                  type="button"
                  onClick={() => handleRemoveItem(index)}
                  className="text-blue-500 hover:text-blue-700 font-bold focus:outline-none"
                  aria-label={`Remove ${item}`}
                >
                  &times;
                </button>
              )}
            </span>
          ))
        ) : (
          <span className="text-gray-400 text-sm py-1 px-2 font-sans self-center">
            {required ? 'At least one item required' : 'No items added'}
          </span>
        )}
      </div>

      {/* Input field */}
      {!disabled && (
        <div className="flex gap-2">
          <input
            ref={inputRef}
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            className="flex-1 bg-white border border-gray-300 rounded-lg text-sm px-3.5 py-2.5 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none font-sans"
          />
          <button
            type="button"
            onClick={handleAddItem}
            disabled={!inputValue.trim()}
            className="px-4 py-2 border border-gray-300 rounded-lg bg-white text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Add
          </button>
        </div>
      )}
    </div>
  );
}
