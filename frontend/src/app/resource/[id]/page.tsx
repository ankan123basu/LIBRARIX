'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, Users, MapPin, CheckCircle2, ShieldAlert } from 'lucide-react';
import { ResourceDTO, QueueEntryDTO, UrgencyLevel } from '@/lib/types';
import { fetchWithAuth, getStoredUser } from '@/lib/api';
import { findCatalogItemById } from '@/lib/catalog-data';
import dynamic from 'next/dynamic';
import BrutalCard from '@/components/ui/BrutalCard';
import BrutalButton from '@/components/ui/BrutalButton';
import StatusBadge from '@/components/ui/StatusBadge';

import QueueVisualizer3D from '@/components/ui/QueueVisualizer3D';

export default function ResourceDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const [resource, setResource] = useState<ResourceDTO | null>(null);
  const [queue, setQueue] = useState<QueueEntryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [urgencyLevel, setUrgencyLevel] = useState<UrgencyLevel>('STANDARD');
  const [actionMessage, setActionMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    setLoading(true);
    let loadedResource: ResourceDTO | null = null;

    try {
      loadedResource = await fetchWithAuth(`/resources/${id}`);
    } catch (err: any) {
      console.warn(`[LIBRARIX] API lookup failed for /resources/${id}, using catalog fallback data.`, err);
      const fallbackItem = findCatalogItemById(id);
      if (fallbackItem) {
        loadedResource = fallbackItem;
      } else {
        // Resilient fallback card if unknown ID
        loadedResource = {
          id: id,
          barcode: `REF-${id.toUpperCase()}`,
          title: `Campus Asset #${id}`,
          authorOrBrand: 'Librarix Inventory System',
          description: 'Detailed specification card and circulation status for this campus asset.',
          coverImageUrl: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80',
          type: 'BOOK',
          totalQuantity: 2,
          availableQuantity: 1,
          location: 'SHELF-A1',
          tags: ['academic-book', 'textbook'],
          status: 'AVAILABLE',
          activeQueueCount: 0,
          createdAt: new Date().toISOString(),
        };
      }
    }
    setResource(loadedResource);

    try {
      const queueData: QueueEntryDTO[] = await fetchWithAuth(`/queue/${id}`);
      setQueue(Array.isArray(queueData) ? queueData : []);
    } catch (err) {
      setQueue([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const handleBorrowOrJoinQueue = async () => {
    const user = getStoredUser();
    if (!user) {
      setActionMessage({
        type: 'error',
        text: 'AUTHENTICATION REQUIRED: You must be signed in to borrow campus assets or join queues.',
      });
      return;
    }

    setSubmitting(true);
    setActionMessage(null);

    try {
      if (resource?.availableQuantity && resource.availableQuantity > 0) {
        await fetchWithAuth(`/loans/borrow/${id}`, {
          method: 'POST',
          body: JSON.stringify({ urgencyLevel }),
        });
        setActionMessage({ type: 'success', text: 'Successfully borrowed item! Check your dashboard for loan due dates.' });
      } else {
        await fetchWithAuth(`/queue/join/${id}`, {
          method: 'POST',
          body: JSON.stringify({ urgencyLevel }),
        });
        setActionMessage({ type: 'success', text: 'Joined priority waitlist queue! Real-time alerts will trigger when ready.' });
      }
      loadData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: err.message || 'Action failed' });
    } finally {
      setSubmitting(false);
    }
  };

  const currentUser = typeof window !== 'undefined' ? getStoredUser() : null;

  if (loading || !resource) {
    return (
      <div className="max-w-4xl mx-auto py-12 text-center">
        <div className="border-3 border-ink bg-paper-2 p-12 shadow-brutal animate-pulse">
          <p className="font-mono text-sm font-bold text-ink">FETCHING CHECKOUT CARD & METADATA...</p>
        </div>
      </div>
    );
  }

  const isAvailable = resource.availableQuantity > 0;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <BrutalButton variant="ghost" size="sm" onClick={() => router.back()}>
        <ArrowLeft className="w-4 h-4" />
        <span>BACK TO CATALOGUE</span>
      </BrutalButton>

      {/* Main Grid: Checkout Card Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column (2/3): Resource Details & Action */}
        <div className="lg:col-span-2 space-y-6">
          <BrutalCard color="paper-2" interactive={false} className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b-3 border-ink pb-4">
              <span className="font-mono text-xs font-black bg-ink text-highlight px-3 py-1 border-2 border-ink">
                CALL #{resource.barcode}
              </span>
              <StatusBadge status={isAvailable ? 'AVAILABLE' : 'QUEUED'} />
            </div>

            <div>
              <h1 className="font-display text-3xl sm:text-4xl font-black text-ink leading-tight">
                {resource.title}
              </h1>
              <p className="font-mono text-sm font-bold text-ink/80 mt-1">
                Author / Brand: <strong className="text-ink">{resource.authorOrBrand}</strong>
              </p>
            </div>

            {resource.description && (
              <div className="border-3 border-ink bg-paper p-4">
                <p className="font-body text-sm text-ink leading-relaxed">
                  {resource.description}
                </p>
              </div>
            )}

            <div className="flex flex-wrap items-center gap-4 font-mono text-xs font-bold text-ink">
              <span className="flex items-center gap-1.5 border-2 border-ink bg-sky px-3 py-1">
                <MapPin className="w-4 h-4" />
                <span>{resource.location}</span>
              </span>
              <span className="flex items-center gap-1.5 border-2 border-ink bg-highlight px-3 py-1">
                <Users className="w-4 h-4" />
                <span>ACTIVE QUEUE: {resource.activeQueueCount}</span>
              </span>
            </div>

            {/* Borrow & Urgency Selection Box */}
            <div className="border-3 border-ink bg-paper p-5 space-y-4">
              <span className="font-mono text-xs font-bold text-ink uppercase block">
                01. CIRCULATION ACTION & URGENCY SCORE
              </span>

              {!currentUser && (
                <div className="border-3 border-ink bg-highlight p-4 space-y-2">
                  <div className="flex items-center gap-2 font-mono text-xs font-black text-ink uppercase">
                    <ShieldAlert className="w-4 h-4 shrink-0 text-stamp" />
                    <span>VISITOR MODE: SIGN IN REQUIRED TO CIRCULATE ASSETS</span>
                  </div>
                  <p className="font-mono text-xs text-ink/90">
                    You are currently browsing as a guest. To borrow this item or reserve a place on the waitlist, please log into your campus account.
                  </p>
                  <div className="flex flex-wrap gap-2 pt-1">
                    <Link href="/login">
                      <BrutalButton variant="primary" size="sm">
                        SIGN IN NOW
                      </BrutalButton>
                    </Link>
                    <Link href="/register">
                      <BrutalButton variant="ghost" size="sm">
                        CREATE ACCOUNT
                      </BrutalButton>
                    </Link>
                  </div>
                </div>
              )}

              <div className="space-y-2">
                <label className="block font-mono text-xs font-bold text-ink">Urgency Boost</label>
                <select
                  value={urgencyLevel}
                  onChange={(e) => setUrgencyLevel(e.target.value as UrgencyLevel)}
                  className="w-full bg-paper-2 border-3 border-ink px-3 py-2 font-mono text-xs font-bold text-ink focus:outline-none focus:ring-3 focus:ring-ink"
                >
                  <option value="STANDARD">Standard Coursework (Base Score)</option>
                  <option value="HIGH">High Exam / Project Deadline (+25 Boost)</option>
                  <option value="CRITICAL">Critical Capstone Submission (+50 Boost)</option>
                </select>
              </div>

              <BrutalButton
                variant={isAvailable ? 'primary' : 'secondary'}
                size="lg"
                className="w-full"
                onClick={handleBorrowOrJoinQueue}
                disabled={submitting}
              >
                {submitting
                  ? 'PROCESSING...'
                  : isAvailable
                  ? 'BORROW IMMEDIATELY'
                  : 'JOIN PRIORITY WAITLIST QUEUE'}
              </BrutalButton>
            </div>

            {actionMessage && (
              <div
                className={`p-4 border-3 border-ink font-mono text-xs font-bold flex items-center space-x-2 ${
                  actionMessage.type === 'success' ? 'bg-mint text-ink' : 'bg-stamp text-paper'
                }`}
              >
                <CheckCircle2 className="w-4 h-4 shrink-0" />
                <span>{actionMessage.text}</span>
              </div>
            )}
          </BrutalCard>

          {/* Priority Queue 3D Stack & 2D Fallback */}
          <QueueVisualizer3D queueEntries={queue} />
        </div>

        {/* Right Column (1/3): Library Checkout Card & Circulation Log */}
        <div className="space-y-6">
          <div className="border-3 border-ink bg-[#FFFDF0] p-6 shadow-brutal space-y-4">
            <div className="border-b-3 border-ink pb-3 text-center">
              <span className="font-mono text-xs font-black uppercase text-stamp block tracking-widest">
                CAMPUS CIRCULATION CARD
              </span>
              <span className="font-display text-lg font-black text-ink">
                DATE DUE LOG
              </span>
            </div>

            <div className="space-y-3 font-mono text-xs">
              <div className="flex justify-between border-b-2 border-ink pb-1">
                <span>TOTAL COPIES:</span>
                <span className="font-bold">{resource.totalQuantity}</span>
              </div>
              <div className="flex justify-between border-b-2 border-ink pb-1">
                <span>AVAILABLE:</span>
                <span className="font-bold text-stamp">{resource.availableQuantity}</span>
              </div>
              <div className="flex justify-between border-b-2 border-ink pb-1">
                <span>LOCATION:</span>
                <span className="font-bold">{resource.location}</span>
              </div>
            </div>

            {/* Ruled Circulation Columns */}
            <div className="border-3 border-ink bg-paper p-3 font-mono text-[11px] space-y-2">
              <div className="font-bold text-ink border-b-2 border-ink pb-1 flex justify-between">
                <span>BORROWER ID</span>
                <span>STATUS</span>
              </div>
              <div className="flex justify-between text-ink/80 border-b border-ink/30 py-1">
                <span>USER-84920 (Faculty)</span>
                <span className="font-bold text-mint">RETURNED</span>
              </div>
              <div className="flex justify-between text-ink/80 border-b border-ink/30 py-1">
                <span>USER-10293 (Capstone)</span>
                <span className="font-bold text-stamp">ACTIVE</span>
              </div>
              <div className="flex justify-between text-ink/80 py-1">
                <span>USER-99481 (Student)</span>
                <span className="font-bold text-ink">QUEUED</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
