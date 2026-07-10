'use client';

import React, { useEffect, useState } from 'react';

interface StorefrontLinkProps {
  slug: string;
  className?: string;
  children: React.ReactNode;
}

export default function StorefrontLink({ slug, className, children }: StorefrontLinkProps) {
  const [href, setHref] = useState(`http://localhost:3000${slug}`);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setHref(`${window.location.protocol}//${window.location.hostname}:3000${slug}`);
    }
  }, [slug]);

  return (
    <a href={href} target="_blank" rel="noopener noreferrer" className={className}>
      {children}
    </a>
  );
}
