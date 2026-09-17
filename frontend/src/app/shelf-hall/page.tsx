'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Layers, RefreshCw, Sparkles, BookOpen, CheckCircle2, X, List, Box } from 'lucide-react';
import { ShelfPositionDTO, UrgencyLevel } from '@/lib/types';
import { fetchWithAuth, getStoredUser } from '@/lib/api';
import dynamic from 'next/dynamic';
import BrutalButton from '@/components/ui/BrutalButton';
import BrutalCard from '@/components/ui/BrutalCard';
import StatusBadge from '@/components/ui/StatusBadge';
import Sticker from '@/components/ui/Sticker';

import Shelf3DScene from '@/components/Shelf3DScene';

const FALLBACK_SHELF_DATA: ShelfPositionDTO[] = [
  { resourceId: 'res-1', barcode: '005.133 LIB', title: 'Designing Data-Intensive Applications', authorOrBrand: 'Martin Kleppmann', type: 'BOOK', status: 'AVAILABLE', clusterId: 1, aisleId: 'AISLE-A1', shelfLevel: 2, posX: -1.2, posY: 1.64, posZ: 0, spineColorHex: '#FFE14D', isEyeLevel: true },
  { resourceId: 'res-2', barcode: '005.741 MON', title: 'MongoDB: The Definitive Guide', authorOrBrand: 'Shannon Bradshaw', type: 'BOOK', status: 'BORROWED', clusterId: 1, aisleId: 'AISLE-A1', shelfLevel: 2, posX: -0.82, posY: 1.64, posZ: 0, spineColorHex: '#7DE8A8', isEyeLevel: true },
  { resourceId: 'res-3', barcode: '005.276 SPR', title: 'Spring Boot 3 in Action', authorOrBrand: 'Craig Walls', type: 'BOOK', status: 'RESERVED', clusterId: 1, aisleId: 'AISLE-A1', shelfLevel: 2, posX: -0.44, posY: 1.64, posZ: 0, spineColorHex: '#FFA8CE', isEyeLevel: true },
  { resourceId: 'res-4', barcode: '004.620 STO', title: 'STOMP & WebSocket Architectures', authorOrBrand: 'Librarix Core Team', type: 'BOOK', status: 'AVAILABLE', clusterId: 1, aisleId: 'AISLE-A1', shelfLevel: 2, posX: -0.06, posY: 1.64, posZ: 0, spineColorHex: '#FFE14D', isEyeLevel: true },
  { resourceId: 'res-5', barcode: '005.117 CLN', title: 'Clean Code Handbook', authorOrBrand: 'Robert C. Martin', type: 'BOOK', status: 'AVAILABLE', clusterId: 1, aisleId: 'AISLE-A1', shelfLevel: 2, posX: 0.32, posY: 1.64, posZ: 0, spineColorHex: '#FFE14D', isEyeLevel: true },
  { resourceId: 'res-6', barcode: '005.300 SYS', title: 'System Design Interview Guide', authorOrBrand: 'Alex Xu', type: 'BOOK', status: 'AVAILABLE', clusterId: 1, aisleId: 'AISLE-A1', shelfLevel: 2, posX: 0.70, posY: 1.64, posZ: 0, spineColorHex: '#A4D7E1', isEyeLevel: true },

  { resourceId: 'res-7', barcode: '005.100 ALG', title: 'Introduction to Algorithms (CLRS)', authorOrBrand: 'Cormen & Leiserson', type: 'BOOK', status: 'AVAILABLE', clusterId: 2, aisleId: 'AISLE-A1', shelfLevel: 1, posX: -1.2, posY: 0.54, posZ: 0, spineColorHex: '#FFE14D', isEyeLevel: false },
  { resourceId: 'res-8', barcode: '004.600 NET', title: 'Computer Networking Top-Down', authorOrBrand: 'James Kurose', type: 'BOOK', status: 'AVAILABLE', clusterId: 2, aisleId: 'AISLE-A1', shelfLevel: 1, posX: -0.82, posY: 0.54, posZ: 0, spineColorHex: '#7DE8A8', isEyeLevel: false },
  { resourceId: 'res-9', barcode: '006.300 ART', title: 'Artificial Intelligence (Russell)', authorOrBrand: 'Russell & Norvig', type: 'BOOK', status: 'BORROWED', clusterId: 2, aisleId: 'AISLE-A1', shelfLevel: 1, posX: -0.44, posY: 0.54, posZ: 0, spineColorHex: '#FFA8CE', isEyeLevel: false },
  { resourceId: 'res-10', barcode: '006.310 DEE', title: 'Deep Learning Theory & Models', authorOrBrand: 'Ian Goodfellow', type: 'BOOK', status: 'AVAILABLE', clusterId: 2, aisleId: 'AISLE-A1', shelfLevel: 1, posX: -0.06, posY: 0.54, posZ: 0, spineColorHex: '#A4D7E1', isEyeLevel: false },

  { resourceId: 'res-11', barcode: '005.131 SICP', title: 'Structure and Interpretation of Computer Programs', authorOrBrand: 'Harold Abelson & Gerald Jay Sussman', type: 'BOOK', status: 'AVAILABLE', clusterId: 3, aisleId: 'AISLE-B1', shelfLevel: 3, posX: -1.2, posY: 2.54, posZ: 0, spineColorHex: '#A4D7E1', isEyeLevel: false },
  { resourceId: 'res-12', barcode: '005.453 CMP', title: 'Compilers: Principles, Techniques, and Tools (Dragon Book)', authorOrBrand: 'Alfred V. Aho & Jeffrey D. Ullman', type: 'BOOK', status: 'BORROWED', clusterId: 3, aisleId: 'AISLE-B1', shelfLevel: 3, posX: -0.82, posY: 2.54, posZ: 0, spineColorHex: '#7DE8A8', isEyeLevel: false },
  { resourceId: 'res-13', barcode: '005.430 OPS', title: 'Operating System Concepts (Dinosaur Book)', authorOrBrand: 'Abraham Silberschatz & Peter B. Galvin', type: 'BOOK', status: 'AVAILABLE', clusterId: 3, aisleId: 'AISLE-B1', shelfLevel: 3, posX: -0.44, posY: 2.54, posZ: 0, spineColorHex: '#FFE14D', isEyeLevel: false },
  { resourceId: 'res-14', barcode: '005.740 DBS', title: 'Database System Concepts (7th Edition)', authorOrBrand: 'Abraham Silberschatz & Henry F. Korth', type: 'BOOK', status: 'AVAILABLE', clusterId: 3, aisleId: 'AISLE-B1', shelfLevel: 3, posX: -0.06, posY: 2.54, posZ: 0, spineColorHex: '#7DE8A8', isEyeLevel: false },
  { resourceId: 'res-15', barcode: '510.285 CON', title: 'Concrete Mathematics: A Foundation for CS', authorOrBrand: 'Ronald L. Graham & Donald E. Knuth', type: 'BOOK', status: 'AVAILABLE', clusterId: 3, aisleId: 'AISLE-B1', shelfLevel: 3, posX: 0.32, posY: 2.54, posZ: 0, spineColorHex: '#A4D7E1', isEyeLevel: false },
  { resourceId: 'res-16', barcode: '006.310 PRM', title: 'Pattern Recognition and Machine Learning', authorOrBrand: 'Christopher M. Bishop', type: 'BOOK', status: 'AVAILABLE', clusterId: 3, aisleId: 'AISLE-B1', shelfLevel: 3, posX: 0.70, posY: 2.54, posZ: 0, spineColorHex: '#FFA8CE', isEyeLevel: false },
];

export default function ShelfHallPage() {
  const [layout, setLayout] = useState<ShelfPositionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedBook, setSelectedBook] = useState<ShelfPositionDTO | null>(null);
  const [viewMode, setViewMode] = useState<'3d' | '2d'>('3d');
  const [urgencyLevel, setUrgencyLevel] = useState<UrgencyLevel>('STANDARD');
  const [actionMsg, setActionMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    // Auto fallback to 2D list view if prefers-reduced-motion is enabled
    if (typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      setViewMode('2d');
    }
  }, []);

  const fetch3DLayout = async () => {
    setLoading(true);
    try {
      const data = await fetchWithAuth('/shelf/layout');
      const items = Array.isArray(data) ? data : [];
      setLayout(items.length > 0 ? items : FALLBACK_SHELF_DATA);
    } catch (err) {
      // If unauthenticated or backend fails, supply rich fallback 3D layout data
      setLayout(FALLBACK_SHELF_DATA);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetch3DLayout();
  }, []);

  const handleBorrowOrJoinQueue = async () => {
    if (!selectedBook) return;
    const user = getStoredUser();
    if (!user) {
      window.location.href = '/login';
      return;
    }

    setSubmitting(true);
    setActionMsg(null);

    try {
      if (selectedBook.status === 'AVAILABLE') {
        await fetchWithAuth(`/loans/borrow/${selectedBook.resourceId}`, {
          method: 'POST',
          body: JSON.stringify({ urgencyLevel }),
        });
        setActionMsg({ type: 'success', text: `Successfully borrowed '${selectedBook.title}'!` });
      } else {
        await fetchWithAuth(`/queue/join/${selectedBook.resourceId}`, {
          method: 'POST',
          body: JSON.stringify({ urgencyLevel }),
        });
        setActionMsg({ type: 'success', text: `Joined priority waitlist queue for '${selectedBook.title}'!` });
      }
      fetch3DLayout();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Action failed' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <h1 className="font-display text-3xl font-black text-ink uppercase">
              3D IMMERSIVE SHELF HALL
            </h1>
            <Sticker color="lilac" rotate="-2deg">
              3D SLOTTER
            </Sticker>
          </div>
          <p className="font-mono text-xs text-ink/80">
            Interactive 3D visual shelf layout organized by popularity and shelf placement
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* View Mode Switcher */}
          <button
            onClick={() => setViewMode(viewMode === '3d' ? '2d' : '3d')}
            className="px-3.5 py-2 border-3 border-ink bg-highlight font-mono text-xs font-bold flex items-center space-x-1.5 shadow-brutal-sm hover:bg-[#FADB30] active:translate-x-[2px] active:translate-y-[2px]"
          >
            {viewMode === '3d' ? <List className="w-4 h-4" /> : <Box className="w-4 h-4" />}
            <span>{viewMode === '3d' ? 'SWITCH TO LIST VIEW' : 'SWITCH TO 3D HALL'}</span>
          </button>

          <Link href="/catalog">
            <BrutalButton variant="ghost" size="sm">
              <BookOpen className="w-4 h-4" />
              <span>2D CATALOGUE</span>
            </BrutalButton>
          </Link>

          <button
            onClick={fetch3DLayout}
            className="p-2.5 border-3 border-ink bg-paper-2 shadow-brutal-sm hover:bg-paper"
            title="Refresh Layout"
          >
            <RefreshCw className={`w-4 h-4 text-ink ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Main Content: 3D Scene vs 2D Fallback List View */}
      {loading ? (
        <div className="w-full h-[650px] border-3 border-ink bg-paper-2 animate-pulse flex items-center justify-center">
          <p className="font-mono text-xs font-bold text-ink">COMPUTING 3D AISLE COORDINATES & GRAPH CLUSTERS...</p>
        </div>
      ) : viewMode === '3d' ? (
        <Shelf3DScene layout={layout} onSelectBook={(book) => setSelectedBook(book)} />
      ) : (
        /* 2D List Fallback View */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {layout.map((book) => (
            <BrutalCard
              key={book.resourceId}
              color="paper-2"
              onClick={() => setSelectedBook(book)}
              className="cursor-pointer space-y-3"
            >
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs font-black bg-ink text-highlight px-2 py-0.5 border border-ink">
                  {book.aisleId} • LEVEL {book.shelfLevel}
                </span>
                <StatusBadge status={book.status} />
              </div>
              <h4 className="font-display text-lg font-black text-ink line-clamp-1">{book.title}</h4>
              <p className="font-mono text-xs text-ink/70">{book.authorOrBrand}</p>
              {book.isEyeLevel && (
                <div className="font-mono text-[10px] bg-highlight border-2 border-ink p-1.5 font-bold">
                  🌟 EYE-LEVEL PLACEMENT (TOP 30% POPULARITY)
                </div>
              )}
              <BrutalButton variant="primary" size="sm" className="w-full mt-2">
                SELECT ITEM
              </BrutalButton>
            </BrutalCard>
          ))}
        </div>
      )}

      {/* 2D Drawer Modal for Item Checkout */}
      {selectedBook && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/60 backdrop-blur-xs p-4">
          <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal max-w-md w-full space-y-4 relative">
            <button
              onClick={() => { setSelectedBook(null); setActionMsg(null); }}
              className="absolute top-4 right-4 p-1 border-2 border-ink bg-paper hover:bg-blush"
            >
              <X className="w-5 h-5 text-ink" />
            </button>

            <div className="flex items-center gap-2">
              <StatusBadge status={selectedBook.status} />
              <span className="font-mono text-xs font-bold bg-ink text-paper px-2 py-0.5">
                {selectedBook.aisleId} • LEVEL {selectedBook.shelfLevel}
              </span>
            </div>

            <h3 className="font-display text-2xl font-black text-ink leading-tight">{selectedBook.title}</h3>
            <p className="font-mono text-xs font-bold text-ink/80">{selectedBook.authorOrBrand} • BARCODE #{selectedBook.barcode}</p>

            {selectedBook.isEyeLevel && (
              <p className="font-mono text-xs bg-highlight p-3 border-3 border-ink font-bold">
                🌟 <strong>Eye-Level Position:</strong> Ranks in top 30% campus popularity demand!
              </p>
            )}

            <div className="space-y-2 pt-2 font-mono text-xs">
              <label className="block font-bold text-ink uppercase">Urgency Score Level</label>
              <select
                value={urgencyLevel}
                onChange={(e) => setUrgencyLevel(e.target.value as UrgencyLevel)}
                className="w-full bg-paper border-3 border-ink px-3 py-2 font-bold text-ink"
              >
                <option value="STANDARD">Standard Coursework</option>
                <option value="HIGH">High Exam / Project Deadline</option>
                <option value="CRITICAL">Critical Capstone Submission</option>
              </select>
            </div>

            <BrutalButton
              variant={selectedBook.status === 'AVAILABLE' ? 'primary' : 'secondary'}
              size="lg"
              className="w-full"
              onClick={handleBorrowOrJoinQueue}
              disabled={submitting}
            >
              {submitting ? 'PROCESSING...' : selectedBook.status === 'AVAILABLE' ? 'BORROW INSTANTLY' : 'JOIN PRIORITY QUEUE'}
            </BrutalButton>

            {actionMsg && (
              <div className={`p-3 border-3 border-ink font-mono text-xs font-bold flex items-center gap-2 ${
                actionMsg.type === 'success' ? 'bg-mint text-ink' : 'bg-stamp text-paper'
              }`}>
                <CheckCircle2 className="w-4 h-4 shrink-0" />
                <span>{actionMsg.text}</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
