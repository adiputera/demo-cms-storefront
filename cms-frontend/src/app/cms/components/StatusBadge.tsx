export default function StatusBadge({ status }: { status: string }) {
  if (status === 'SYNCED') {
    return <span className="px-2 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full">🟢 Synced</span>;
  }
  if (status === 'OUT_OF_SYNC') {
    return <span className="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs font-semibold rounded-full">🟡 Out of Sync</span>;
  }
  if (status === 'NOT_SYNCED') {
    return <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs font-semibold rounded-full">⚪ Not Synced</span>;
  }
  return <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs font-semibold rounded-full">⚪ Unknown</span>;
}
