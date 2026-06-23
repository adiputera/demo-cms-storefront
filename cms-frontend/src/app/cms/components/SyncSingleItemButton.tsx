'use client';

import { useState } from 'react';
import { cmsApiClient } from '@/lib/cms-api-client';
import { useRouter } from 'next/navigation';

interface SyncSingleItemButtonProps {
  entityType: string;
  itemId: number;
  className?: string;
}

export default function SyncSingleItemButton({ entityType, itemId, className }: SyncSingleItemButtonProps) {
  const [isSyncing, setIsSyncing] = useState(false);
  const router = useRouter();

  const handleSync = async () => {
    setIsSyncing(true);
    try {
      await cmsApiClient.syncItem(entityType, itemId);
      alert(`Successfully synced ${entityType} to ONLINE!`);
      router.refresh();
    } catch (error: any) {
      alert(error.message || `Failed to sync ${entityType}`);
    } finally {
      setIsSyncing(false);
    }
  };

  return (
    <button
      onClick={handleSync}
      disabled={isSyncing}
      className={className || `text-green-600 hover:text-green-800 font-medium ${isSyncing ? 'opacity-50 cursor-not-allowed' : ''}`}
    >
      {isSyncing ? 'Syncing...' : 'Sync Item'}
    </button>
  );
}
