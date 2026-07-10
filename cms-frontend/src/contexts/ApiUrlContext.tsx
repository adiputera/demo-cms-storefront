'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';

interface ApiUrlContextType {
  baseUrl: string;
}

const ApiUrlContext = createContext<ApiUrlContextType>({
  baseUrl: 'http://localhost:8081/api/cms', // Default fallback
});

export function ApiUrlProvider({ children }: { children: React.ReactNode }) {
  const [baseUrl, setBaseUrl] = useState('http://localhost:8081/api/cms');

  useEffect(() => {
    // This runs ONLY in browser, preventing bundler optimization
    if (typeof window !== 'undefined') {
      const hostname = window.location.hostname;
      const protocol = window.location.protocol;
      setBaseUrl(`${protocol}//${hostname}:8081/api/cms`);
    }
  }, []);

  return (
    <ApiUrlContext.Provider value={{ baseUrl }}>
      {children}
    </ApiUrlContext.Provider>
  );
}

export function useApiUrl() {
  return useContext(ApiUrlContext);
}
