import type { NextConfig } from "next";

const backendUrl = process.env.NODE_ENV === 'production' 
  ? 'http://cms-backend:8081' 
  : 'http://localhost:8081';

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'images.unsplash.com',
      },
      {
        protocol: 'https',
        hostname: 'example.com',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
      },
    ],
  },
  output: 'standalone',
  async rewrites() {
    return [
      {
        source: '/uploads/:path*',
        destination: `${backendUrl}/uploads/:path*`,
      },
    ];
  },
};

export default nextConfig;
