'use client';

import { useEffect, useState } from 'react';

export default function StorefrontHeaderLink() {
  const [href, setHref] = useState('http://localhost:3000');

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setHref(`${window.location.protocol}//${window.location.hostname}:3000`);
    }
  }, []);

  return (
    <a
      href={href}
      className="text-sm font-semibold text-blue-600 hover:text-blue-800 transition-colors font-sans"
      target="_blank"
      rel="noopener noreferrer"
    >
      View Storefront
    </a>
  );
}
