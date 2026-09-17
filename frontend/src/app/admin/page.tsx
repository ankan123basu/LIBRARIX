'use client';

import React, { useEffect, useState } from 'react';
import dynamic from 'next/dynamic';
import { useRouter } from 'next/navigation';
import {
  ShieldCheck,
  Plus,
  Trash2,
  Edit,
  RefreshCw,
  BookOpen,
  DollarSign,
  Layers,
  Sparkles,
  Search,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';
import {
  ResourceDTO,
  LoanDTO,
  FineDTO,
  ShelfPositionDTO,
  ResourceType,
} from '@/lib/types';
import { fetchWithAuth, getStoredUser } from '@/lib/api';
import BrutalCard from '@/components/ui/BrutalCard';
import BrutalButton from '@/components/ui/BrutalButton';
import StatBlock from '@/components/ui/StatBlock';
import Sticker from '@/components/ui/Sticker';
import StatusBadge from '@/components/ui/StatusBadge';

const AdminHeatmap3D = dynamic(() => import('@/components/ui/AdminHeatmap3D'), {
  ssr: false,
});

export default function AdminPage() {
  const router = useRouter();
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [activeTab, setActiveTab] = useState<'catalog' | 'loans' | 'fines' | 'optimizer'>('catalog');

  // Data states
  const [resources, setResources] = useState<ResourceDTO[]>([]);
  const [loans, setLoans] = useState<LoanDTO[]>([]);
  const [fines, setFines] = useState<FineDTO[]>([]);
  const [shelfLayout, setShelfLayout] = useState<ShelfPositionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionMessage, setActionMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Form states
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingResource, setEditingResource] = useState<ResourceDTO | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Form fields
  const [formData, setFormData] = useState({
    title: '',
    authorOrBrand: '',
    description: '',
    barcode: '',
    type: 'BOOK' as ResourceType,
    totalQuantity: 1,
    location: 'SHELF-A1',
    tags: 'programming, cs',
  });

  const loadAllAdminData = async () => {
    setLoading(true);
    const user = getStoredUser();
    if (!user) {
      router.push('/login');
      return;
    }
    setCurrentUser(user);

    if (user.role !== 'ROLE_ADMIN' && user.role !== 'ROLE_LIBRARIAN' && user.role !== 'ROLE_CURATOR') {
      return;
    }

    try {
      // Load resources catalog
      const resData = await fetchWithAuth('/resources?size=50');
      setResources(resData.content || resData);

      // Load 3D layout
      const layoutData = await fetchWithAuth('/shelf/layout');
      setShelfLayout(layoutData);

      // Load active loans (Librarian/Admin only)
      if (user.role === 'ROLE_LIBRARIAN' || user.role === 'ROLE_ADMIN') {
        const loansData = await fetchWithAuth('/loans/all');
        setLoans(loansData);

        const finesData = await fetchWithAuth('/fines/all');
        setFines(finesData);
      }
    } catch (err: any) {
      console.error('Error loading admin data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllAdminData();
  }, []);

  // Handlers
  const handleSaveResource = async (e: React.FormEvent) => {
    e.preventDefault();
    setActionMessage(null);
    try {
      const payload = {
        title: formData.title,
        authorOrBrand: formData.authorOrBrand,
        description: formData.description,
        barcode: formData.barcode || `BC-${Math.floor(100000 + Math.random() * 900000)}`,
        type: formData.type,
        totalQuantity: Number(formData.totalQuantity),
        availableQuantity: Number(formData.totalQuantity),
        location: formData.location,
        tags: formData.tags.split(',').map((t) => t.trim()),
      };

      if (editingResource) {
        await fetchWithAuth(`/resources/${editingResource.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload),
        });
        setActionMessage({ type: 'success', text: `Resource "${payload.title}" updated successfully.` });
      } else {
        await fetchWithAuth('/resources', {
          method: 'POST',
          body: JSON.stringify(payload),
        });
        setActionMessage({ type: 'success', text: `Resource "${payload.title}" created successfully.` });
      }

      setShowAddModal(false);
      setEditingResource(null);
      resetForm();
      loadAllAdminData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: err.message || 'Failed to save resource.' });
    }
  };

  const handleDeleteResource = async (id: string, title: string) => {
    if (!confirm(`Are you sure you want to delete "${title}"?`)) return;
    try {
      await fetchWithAuth(`/resources/${id}`, { method: 'DELETE' });
      setActionMessage({ type: 'success', text: `Resource "${title}" deleted.` });
      loadAllAdminData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: err.message || 'Failed to delete resource.' });
    }
  };

  const handleReturnLoan = async (loanId: string) => {
    try {
      await fetchWithAuth(`/loans/return/${loanId}`, { method: 'POST' });
      setActionMessage({ type: 'success', text: 'Loan marked as RETURNED.' });
      loadAllAdminData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: err.message || 'Failed to return loan.' });
    }
  };

  const handleWaiveFine = async (fineId: string) => {
    try {
      await fetchWithAuth(`/fines/${fineId}/waive`, { method: 'POST' });
      setActionMessage({ type: 'success', text: 'Fine administrative waiver granted.' });
      loadAllAdminData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: err.message || 'Failed to waive fine.' });
    }
  };

  const handleReoptimizeGraph = async () => {
    try {
      const res = await fetchWithAuth('/shelf/reoptimize', { method: 'POST' });
      setActionMessage({
        type: 'success',
        text: `Graph re-optimized! Cleared LRU cache. Total assets re-slotted: ${res.totalAssetsSlotting}`,
      });
      loadAllAdminData();
    } catch (err: any) {
      setActionMessage({ type: 'error', text: err.message || 'Failed to reoptimize graph.' });
    }
  };

  const resetForm = () => {
    setFormData({
      title: '',
      authorOrBrand: '',
      description: '',
      barcode: '',
      type: 'BOOK',
      totalQuantity: 1,
      location: 'SHELF-A1',
      tags: 'programming, cs',
    });
  };

  const openEditModal = (r: ResourceDTO) => {
    setEditingResource(r);
    setFormData({
      title: r.title,
      authorOrBrand: r.authorOrBrand,
      description: r.description || '',
      barcode: r.barcode,
      type: r.type,
      totalQuantity: r.totalQuantity,
      location: r.location,
      tags: r.tags ? r.tags.join(', ') : '',
    });
    setShowAddModal(true);
  };

  if (!currentUser || (currentUser.role !== 'ROLE_ADMIN' && currentUser.role !== 'ROLE_LIBRARIAN' && currentUser.role !== 'ROLE_CURATOR')) {
    return (
      <div className="max-w-3xl mx-auto py-16 text-center space-y-6">
        <div className="border-3 border-ink bg-stamp p-10 shadow-brutal text-paper space-y-4">
          <AlertTriangle className="w-12 h-12 mx-auto" />
          <h2 className="font-display text-3xl font-black uppercase">ACCESS RESTRICTED</h2>
          <p className="font-mono text-sm">
            Staff Administrative Console requires ROLE_CURATOR, ROLE_LIBRARIAN, or ROLE_ADMIN permissions.
          </p>
          <BrutalButton variant="secondary" onClick={() => router.push('/login')}>
            SIGN IN WITH STAFF ACCOUNT
          </BrutalButton>
        </div>
      </div>
    );
  }

  const isLibrarianOrAdmin = currentUser.role === 'ROLE_LIBRARIAN' || currentUser.role === 'ROLE_ADMIN';
  const isCuratorOrAdmin = currentUser.role === 'ROLE_CURATOR' || currentUser.role === 'ROLE_ADMIN';

  const filteredResources = resources.filter(
    (r) =>
      r.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.authorOrBrand.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.barcode.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-8 py-6">
      {/* Lilac Staff Header Banner */}
      <div className="border-3 border-ink bg-lilac p-6 sm:p-10 shadow-brutal-lg relative overflow-hidden space-y-4">
        <div className="absolute top-4 right-4">
          <Sticker color="highlight" rotate="4deg">
            🛡️ ROLE: {currentUser.role.replace('ROLE_', '')}
          </Sticker>
        </div>

        <div className="inline-flex items-center gap-2 border-3 border-ink bg-paper px-3 py-1 font-mono text-xs font-bold uppercase shadow-brutal-sm">
          <ShieldCheck className="w-4 h-4 text-stamp" />
          <span>STAFF CONSOLE & MANAGEMENT SYSTEM</span>
        </div>

        <h1 className="font-display text-4xl sm:text-5xl font-black text-ink tracking-tight uppercase">
          STAFF ADMINISTRATION & CIRCULATION CONTROL
        </h1>
        <p className="font-mono text-sm text-ink/80 max-w-2xl font-bold">
          Manage resources catalog, enforce fine waivers, view live active loans, and trigger graph slotting optimizer.
        </p>
      </div>

      {/* Global Action Message */}
      {actionMessage && (
        <div
          className={`p-4 border-3 border-ink font-mono text-xs font-bold flex items-center justify-between shadow-brutal-sm ${
            actionMessage.type === 'success' ? 'bg-mint text-ink' : 'bg-stamp text-paper'
          }`}
        >
          <div className="flex items-center space-x-2">
            <CheckCircle2 className="w-5 h-5 shrink-0" />
            <span>{actionMessage.text}</span>
          </div>
          <button onClick={() => setActionMessage(null)} className="font-black text-sm">
            ✕
          </button>
        </div>
      )}

      {/* Admin Stat Blocks */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <StatBlock
          label="CATALOG ASSETS"
          value={resources.length}
          color="paper-2"
        />
        <StatBlock
          label="ACTIVE LOANS"
          value={loans.filter((l) => l.status === 'ACTIVE').length}
          color="sky"
        />
        <StatBlock
          label="PENDING FINES"
          value={`$${fines.filter((f) => f.status === 'PENDING').reduce((a, b) => a + b.amount, 0).toFixed(2)}`}
          color="blush"
        />
        <StatBlock
          label="3D GRAPH CLUSTERS"
          value={shelfLayout.length > 0 ? new Set(shelfLayout.map((s) => s.clusterId)).size : 1}
          color="mint"
        />
      </div>

      {/* 3D Occupancy Heatmap Section */}
      <AdminHeatmap3D shelfLayout={shelfLayout} />

      {/* Navigation Tabs */}
      <div className="flex flex-wrap items-center gap-2 border-b-3 border-ink pb-3 font-mono text-xs font-bold">
        <button
          onClick={() => setActiveTab('catalog')}
          className={`px-4 py-2 border-3 border-ink transition-all flex items-center gap-2 ${
            activeTab === 'catalog'
              ? 'bg-highlight shadow-brutal translate-x-[-2px] translate-y-[-2px]'
              : 'bg-paper-2 hover:bg-paper'
          }`}
        >
          <BookOpen className="w-4 h-4" />
          <span>RESOURCE CATALOG ({resources.length})</span>
        </button>

        {isLibrarianOrAdmin && (
          <button
            onClick={() => setActiveTab('loans')}
            className={`px-4 py-2 border-3 border-ink transition-all flex items-center gap-2 ${
              activeTab === 'loans'
                ? 'bg-mint shadow-brutal translate-x-[-2px] translate-y-[-2px]'
                : 'bg-paper-2 hover:bg-paper'
            }`}
          >
            <Layers className="w-4 h-4" />
            <span>ACTIVE LOANS ({loans.length})</span>
          </button>
        )}

        {isLibrarianOrAdmin && (
          <button
            onClick={() => setActiveTab('fines')}
            className={`px-4 py-2 border-3 border-ink transition-all flex items-center gap-2 ${
              activeTab === 'fines'
                ? 'bg-blush shadow-brutal translate-x-[-2px] translate-y-[-2px]'
                : 'bg-paper-2 hover:bg-paper'
            }`}
          >
            <DollarSign className="w-4 h-4" />
            <span>FINE NOTICES ({fines.length})</span>
          </button>
        )}

        {isCuratorOrAdmin && (
          <button
            onClick={() => setActiveTab('optimizer')}
            className={`px-4 py-2 border-3 border-ink transition-all flex items-center gap-2 ${
              activeTab === 'optimizer'
                ? 'bg-lilac shadow-brutal translate-x-[-2px] translate-y-[-2px]'
                : 'bg-paper-2 hover:bg-paper'
            }`}
          >
            <RefreshCw className="w-4 h-4" />
            <span>SHELF OPTIMIZER</span>
          </button>
        )}
      </div>

      {/* Tab 1: Resource Catalog Management */}
      {activeTab === 'catalog' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-paper-2 border-3 border-ink p-4 shadow-brutal-sm">
            <div className="relative w-full sm:w-80">
              <Search className="w-4 h-4 absolute left-3 top-3 text-ink/70" />
              <input
                type="text"
                placeholder="Search title, author, barcode..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full bg-paper border-2 border-ink pl-9 pr-3 py-1.5 font-mono text-xs font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
              />
            </div>

            {isCuratorOrAdmin && (
              <BrutalButton
                variant="primary"
                size="sm"
                onClick={() => {
                  resetForm();
                  setEditingResource(null);
                  setShowAddModal(true);
                }}
              >
                <Plus className="w-4 h-4" />
                <span>ADD NEW RESOURCE</span>
              </BrutalButton>
            )}
          </div>

          <div className="overflow-x-auto border-3 border-ink bg-paper shadow-brutal">
            <table className="w-full text-left font-mono text-xs border-collapse">
              <thead className="bg-ink text-paper uppercase border-b-3 border-ink">
                <tr>
                  <th className="p-3 border-r-2 border-ink">BARCODE</th>
                  <th className="p-3 border-r-2 border-ink">TITLE & AUTHOR</th>
                  <th className="p-3 border-r-2 border-ink">TYPE</th>
                  <th className="p-3 border-r-2 border-ink">QTY (AVAIL / TOTAL)</th>
                  <th className="p-3 border-r-2 border-ink">LOCATION</th>
                  <th className="p-3">ACTIONS</th>
                </tr>
              </thead>
              <tbody className="divide-y-2 divide-ink bg-paper-2">
                {filteredResources.map((r) => (
                  <tr key={r.id} className="hover:bg-highlight/20 transition-colors">
                    <td className="p-3 font-bold border-r-2 border-ink text-ink">{r.barcode}</td>
                    <td className="p-3 border-r-2 border-ink">
                      <p className="font-bold text-ink text-sm">{r.title}</p>
                      <p className="text-[10px] text-ink/70">{r.authorOrBrand}</p>
                    </td>
                    <td className="p-3 border-r-2 border-ink">
                      <span className="bg-sky text-ink px-2 py-0.5 border border-ink font-bold text-[10px]">
                        {r.type}
                      </span>
                    </td>
                    <td className="p-3 border-r-2 border-ink font-bold">
                      <span className={r.availableQuantity > 0 ? 'text-mint font-black' : 'text-stamp'}>
                        {r.availableQuantity}
                      </span>{' '}
                      / {r.totalQuantity}
                    </td>
                    <td className="p-3 border-r-2 border-ink text-ink font-bold">{r.location}</td>
                    <td className="p-3 space-x-2">
                      {isCuratorOrAdmin && (
                        <button
                          onClick={() => openEditModal(r)}
                          className="p-1.5 border-2 border-ink bg-paper hover:bg-highlight font-bold text-ink"
                          title="Edit Resource"
                        >
                          <Edit className="w-3.5 h-3.5" />
                        </button>
                      )}
                      {isLibrarianOrAdmin && (
                        <button
                          onClick={() => handleDeleteResource(r.id, r.title)}
                          className="p-1.5 border-2 border-ink bg-stamp text-paper hover:bg-[#C92C21] font-bold"
                          title="Delete Resource (Librarian/Admin Only)"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 2: Active Loans Management */}
      {activeTab === 'loans' && isLibrarianOrAdmin && (
        <div className="space-y-4">
          <div className="overflow-x-auto border-3 border-ink bg-paper shadow-brutal">
            <table className="w-full text-left font-mono text-xs border-collapse">
              <thead className="bg-ink text-paper uppercase border-b-3 border-ink">
                <tr>
                  <th className="p-3 border-r-2 border-ink">LOAN ID</th>
                  <th className="p-3 border-r-2 border-ink">BORROWER</th>
                  <th className="p-3 border-r-2 border-ink">RESOURCE TITLE</th>
                  <th className="p-3 border-r-2 border-ink">DUE DATE</th>
                  <th className="p-3 border-r-2 border-ink">STATUS</th>
                  <th className="p-3">ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y-2 divide-ink bg-paper-2">
                {loans.map((loan) => (
                  <tr key={loan.id} className="hover:bg-highlight/20 transition-colors">
                    <td className="p-3 font-bold border-r-2 border-ink text-ink">{loan.id.slice(0, 8)}...</td>
                    <td className="p-3 border-r-2 border-ink font-bold text-ink">{loan.userFullName}</td>
                    <td className="p-3 border-r-2 border-ink text-ink">{loan.resourceTitle}</td>
                    <td className="p-3 border-r-2 border-ink font-bold text-stamp">{loan.dueDate}</td>
                    <td className="p-3 border-r-2 border-ink">
                      <StatusBadge status={loan.status} />
                    </td>
                    <td className="p-3">
                      {loan.status === 'ACTIVE' && (
                        <BrutalButton
                          variant="secondary"
                          size="sm"
                          onClick={() => handleReturnLoan(loan.id)}
                        >
                          MARK RETURNED
                        </BrutalButton>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: Fine Notices Management */}
      {activeTab === 'fines' && isLibrarianOrAdmin && (
        <div className="space-y-4">
          <div className="overflow-x-auto border-3 border-ink bg-paper shadow-brutal">
            <table className="w-full text-left font-mono text-xs border-collapse">
              <thead className="bg-ink text-paper uppercase border-b-3 border-ink">
                <tr>
                  <th className="p-3 border-r-2 border-ink">FINE ID</th>
                  <th className="p-3 border-r-2 border-ink">BORROWER</th>
                  <th className="p-3 border-r-2 border-ink">AMOUNT</th>
                  <th className="p-3 border-r-2 border-ink">REASON</th>
                  <th className="p-3 border-r-2 border-ink">STATUS</th>
                  <th className="p-3">ADMIN ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y-2 divide-ink bg-paper-2">
                {fines.map((fine) => (
                  <tr key={fine.id} className="hover:bg-highlight/20 transition-colors">
                    <td className="p-3 font-bold border-r-2 border-ink text-ink">{fine.id.slice(0, 8)}...</td>
                    <td className="p-3 border-r-2 border-ink font-bold text-ink">{fine.userFullName}</td>
                    <td className="p-3 border-r-2 border-ink font-bold text-stamp">${fine.amount.toFixed(2)}</td>
                    <td className="p-3 border-r-2 border-ink text-ink">{fine.reason}</td>
                    <td className="p-3 border-r-2 border-ink">
                      <StatusBadge status={fine.status} />
                    </td>
                    <td className="p-3">
                      {fine.status === 'PENDING' && (
                        <BrutalButton
                          variant="ghost"
                          size="sm"
                          onClick={() => handleWaiveFine(fine.id)}
                        >
                          WAIVE FINE
                        </BrutalButton>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 4: Shelf Optimizer */}
      {activeTab === 'optimizer' && isCuratorOrAdmin && (
        <div className="border-3 border-ink bg-paper-2 p-8 shadow-brutal space-y-6">
          <div className="border-b-3 border-ink pb-4">
            <span className="bg-ink text-paper font-mono text-[10px] font-bold px-2 py-0.5 uppercase">
              CO-BORROW GRAPH & LRU-K OPTIMIZER
            </span>
            <h3 className="font-display text-2xl font-black text-ink mt-2">
              RE-SLOT 3D LIBRARY SHELF POSITIONS
            </h3>
            <p className="font-mono text-xs text-ink/80 mt-1">
              Clears the LRU-K cache and runs the spectral co-borrowing graph cluster algorithm to place frequently co-borrowed items on eye-level shelves.
            </p>
          </div>

          <BrutalButton variant="primary" size="lg" onClick={handleReoptimizeGraph}>
            <RefreshCw className="w-5 h-5" />
            <span>TRIGGER GRAPH RE-OPTIMIZATION</span>
          </BrutalButton>
        </div>
      )}

      {/* Modal Form for Add/Edit Resource */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-ink/70 flex items-center justify-center p-4">
          <div className="bg-paper border-3 border-ink p-6 shadow-brutal-lg max-w-xl w-full space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b-3 border-ink pb-3">
              <h3 className="font-display text-xl font-black text-ink uppercase">
                {editingResource ? 'EDIT RESOURCE METADATA' : 'CREATE NEW CATALOG RESOURCE'}
              </h3>
              <button
                onClick={() => setShowAddModal(false)}
                className="font-mono font-bold text-lg p-1 border-2 border-ink bg-paper-2 hover:bg-stamp hover:text-paper"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSaveResource} className="space-y-4 font-mono text-xs">
              <div>
                <label className="block font-bold text-ink mb-1">RESOURCE TITLE</label>
                <input
                  type="text"
                  required
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  className="w-full bg-paper-2 border-2 border-ink px-3 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block font-bold text-ink mb-1">AUTHOR / BRAND</label>
                  <input
                    type="text"
                    required
                    value={formData.authorOrBrand}
                    onChange={(e) => setFormData({ ...formData, authorOrBrand: e.target.value })}
                    className="w-full bg-paper-2 border-2 border-ink px-3 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                  />
                </div>

                <div>
                  <label className="block font-bold text-ink mb-1">BARCODE</label>
                  <input
                    type="text"
                    value={formData.barcode}
                    onChange={(e) => setFormData({ ...formData, barcode: e.target.value })}
                    placeholder="Auto-generated if empty"
                    className="w-full bg-paper-2 border-2 border-ink px-3 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block font-bold text-ink mb-1">RESOURCE TYPE</label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value as ResourceType })}
                    className="w-full bg-paper-2 border-2 border-ink px-3 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                  >
                    <option value="BOOK">BOOK</option>
                  </select>
                </div>

                <div>
                  <label className="block font-bold text-ink mb-1">TOTAL QUANTITY</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={formData.totalQuantity}
                    onChange={(e) => setFormData({ ...formData, totalQuantity: Number(e.target.value) })}
                    className="w-full bg-paper-2 border-2 border-ink px-3 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                  />
                </div>

                <div>
                  <label className="block font-bold text-ink mb-1">SHELF LOCATION</label>
                  <input
                    type="text"
                    required
                    value={formData.location}
                    onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                    className="w-full bg-paper-2 border-2 border-ink px-3 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-ink mb-1">DESCRIPTION</label>
                <textarea
                  rows={3}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-paper-2 border-2 border-ink px-3 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                />
              </div>

              <div className="flex justify-end space-x-3 pt-3 border-t-2 border-ink">
                <BrutalButton variant="ghost" size="sm" type="button" onClick={() => setShowAddModal(false)}>
                  CANCEL
                </BrutalButton>
                <BrutalButton variant="primary" size="sm" type="submit">
                  SAVE RESOURCE
                </BrutalButton>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
