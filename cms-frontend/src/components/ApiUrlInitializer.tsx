'use client';

import { useEffect } from 'react';
import { setRuntimeBaseUrl } from '@/lib/cms-api-client';

export default function ApiUrlInitializer() {
  useEffect(() => {
    // Set the runtime base URL using window.location
    // This runs only in browser, preventing bundler optimization
    const protocol = window.location.protocol;
    const hostname = window.location.hostname;
    const url = `${protocol}//${hostname}:8081/api/cms`;
    setRuntimeBaseUrl(url);
  }, []);

  return null; // This component doesn't render anything
}
