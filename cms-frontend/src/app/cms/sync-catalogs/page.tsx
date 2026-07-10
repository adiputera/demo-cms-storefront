import { cmsApiClient } from '@/lib/cms-api-client';
import SyncButton from '../components/SyncButton';

export default async function SyncCatalogsPage() {
  let catalogIds: string[] = [];
  let error = null;

  try {
    // Fetch all catalog entities using the existing table registry API
    const response = await cmsApiClient.searchData('catalog', []);
    
    if (response.success && response.data) {
      // Extract unique catalogIds and sort alphabetically
      const uniqueCatalogIds = new Set<string>();
      response.data.forEach((row: any) => {
        if (row.values && row.values.catalogId) {
          uniqueCatalogIds.add(row.values.catalogId);
        }
      });
      catalogIds = Array.from(uniqueCatalogIds).sort();
    }
  } catch (err: any) {
    error = err.message;
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Sync Catalogs</h1>
        <p className="text-gray-600 mt-2">Sync staged content to online for each catalog</p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          Error loading catalogs: {error}
        </div>
      )}

      {!error && catalogIds.length === 0 && (
        <div className="bg-yellow-50 border border-yellow-200 text-yellow-700 px-4 py-3 rounded-lg">
          No catalogs found in the system.
        </div>
      )}

      {!error && catalogIds.length > 0 && (
        <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Catalog ID
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {catalogIds.map((catalogId) => (
                <tr key={catalogId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{catalogId}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <SyncButton catalogId={catalogId} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
