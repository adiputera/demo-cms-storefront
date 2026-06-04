import Link from 'next/link';

export default function CMSLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Link href="/cms" className="text-2xl font-bold text-gray-900">
              CMS Admin
            </Link>
            <nav className="flex gap-6">
              <Link href="/" className="text-blue-600 hover:text-blue-800">
                View Storefront
              </Link>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="container mx-auto px-4 py-8">
        <div className="flex gap-8">
          {/* Sidebar */}
          <aside className="w-64 flex-shrink-0">
            <nav className="bg-white rounded-lg shadow p-4">
              <ul className="space-y-2">
                <li>
                  <Link
                    href="/cms"
                    className="block px-4 py-2 rounded hover:bg-gray-100 text-gray-700 hover:text-gray-900"
                  >
                    Dashboard
                  </Link>
                </li>
                <li>
                  <Link
                    href="/cms/pages"
                    className="block px-4 py-2 rounded hover:bg-gray-100 text-gray-700 hover:text-gray-900"
                  >
                    Pages
                  </Link>
                </li>
                <li>
                  <Link
                    href="/cms/products"
                    className="block px-4 py-2 rounded hover:bg-gray-100 text-gray-700 hover:text-gray-900"
                  >
                    Products
                  </Link>
                </li>
              </ul>
            </nav>
          </aside>

          {/* Content Area */}
          <main className="flex-1">
            <div className="bg-white rounded-lg shadow p-6">
              {children}
            </div>
          </main>
        </div>
      </div>
    </div>
  );
}
