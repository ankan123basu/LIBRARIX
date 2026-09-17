'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { BookOpen, DollarSign, RotateCcw, PlusCircle, ShieldCheck, CheckCircle2 } from 'lucide-react';
import { LoanDTO, FineDTO, User as UserType } from '@/lib/types';
import { fetchWithAuth, getStoredUser } from '@/lib/api';
import BrutalCard from '@/components/ui/BrutalCard';
import BrutalButton from '@/components/ui/BrutalButton';
import StampSeal from '@/components/ui/StampSeal';
import NoticeSlip from '@/components/ui/NoticeSlip';

export default function DashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<UserType | null>(null);

  // Member state
  const [myLoans, setMyLoans] = useState<LoanDTO[]>([]);
  const [myFines, setMyFines] = useState<FineDTO[]>([]);

  // Staff state
  const [allLoans, setAllLoans] = useState<LoanDTO[]>([]);
  const [allFines, setAllFines] = useState<FineDTO[]>([]);

  // Form state
  const [showAddModal, setShowAddModal] = useState(false);
  const [title, setTitle] = useState('');
  const [barcode, setBarcode] = useState('');
  const [authorOrBrand, setAuthorOrBrand] = useState('');
  const [type, setType] = useState('BOOK');
  const [totalQuantity, setTotalQuantity] = useState(1);
  const [location, setLocation] = useState('');
  const [tags, setTags] = useState('');

  const [loading, setLoading] = useState(true);
  const [statusMsg, setStatusMsg] = useState<string | null>(null);

  const loadDashboardData = async () => {
    const currentUser = getStoredUser();
    if (!currentUser) {
      router.push('/login');
      return;
    }
    setUser(currentUser);
    setLoading(true);

    try {
      const loans = await fetchWithAuth('/loans/mine');
      setMyLoans(loans);

      const fines = await fetchWithAuth('/fines/mine');
      setMyFines(fines);

      if (currentUser.role === 'ROLE_LIBRARIAN' || currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ROLE_CURATOR') {
        const adminLoans = await fetchWithAuth('/loans/all');
        setAllLoans(adminLoans);

        const adminFines = await fetchWithAuth('/fines/all');
        setAllFines(adminFines);
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const handleReturnItem = async (loanId: string) => {
    try {
      await fetchWithAuth(`/loans/return/${loanId}`, { method: 'POST' });
      setStatusMsg('Item returned successfully! Priority queue fulfillment evaluated.');
      loadDashboardData();
    } catch (err: any) {
      setStatusMsg(`Error: ${err.message}`);
    }
  };

  const handleWaiveFine = async (fineId: string) => {
    try {
      await fetchWithAuth(`/fines/${fineId}/waive`, { method: 'POST' });
      setStatusMsg('Fine successfully waived!');
      loadDashboardData();
    } catch (err: any) {
      setStatusMsg(`Error: ${err.message}`);
    }
  };

  const handleCreateResource = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await fetchWithAuth('/resources', {
        method: 'POST',
        body: JSON.stringify({
          title,
          barcode,
          authorOrBrand,
          type,
          totalQuantity: Number(totalQuantity),
          location,
          tags: tags.split(',').map((t: string) => t.trim()).filter(Boolean),
        }),
      });
      setShowAddModal(false);
      setStatusMsg('New resource asset registered in campus pool!');
      loadDashboardData();
    } catch (err: any) {
      setStatusMsg(`Error adding asset: ${err.message}`);
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto py-12 text-center">
        <div className="border-3 border-ink bg-paper-2 p-12 shadow-brutal animate-pulse">
          <p className="font-mono text-sm font-bold text-ink">LOADING MEMBER DASHBOARD & CIRCULATION RECORDS...</p>
        </div>
      </div>
    );
  }

  const isStaff = user?.role === 'ROLE_LIBRARIAN' || user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_CURATOR';

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      {/* Header */}
      <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h1 className="font-display text-3xl font-black text-ink uppercase">
              {user?.fullName}'S CIRCULATION DESK
            </h1>
            <span className="font-mono text-xs font-bold px-3 py-0.5 border-2 border-ink bg-mint text-ink uppercase">
              {user?.role.replace('ROLE_', '')}
            </span>
          </div>
          <p className="font-mono text-xs text-ink/80">
            Track active loans, stamped due dates, and administrative fine notices
          </p>
        </div>

        {isStaff && (
          <BrutalButton variant="primary" size="md" onClick={() => setShowAddModal(true)}>
            <PlusCircle className="w-4 h-4" />
            <span>REGISTER ASSET</span>
          </BrutalButton>
        )}
      </div>

      {statusMsg && (
        <div className="p-4 border-3 border-ink bg-mint text-ink font-mono text-xs font-bold flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4" />
            <span>{statusMsg}</span>
          </div>
          <button onClick={() => setStatusMsg(null)} className="font-bold underline">DISMISS</button>
        </div>
      )}

      {/* Member Section: Active Loans & Overdue Fines */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Active Loans */}
        <div className="space-y-4">
          <div className="flex items-center gap-2 border-b-3 border-ink pb-2">
            <BookOpen className="w-5 h-5 text-ink" />
            <h3 className="font-display text-xl font-black text-ink uppercase">
              MY ACTIVE LOANS ({myLoans.length})
            </h3>
          </div>

          {myLoans.length > 0 ? (
            <div className="space-y-4">
              {myLoans.map((loan: LoanDTO) => (
                <div key={loan.id} className="border-3 border-ink bg-paper-2 p-5 shadow-brutal flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 relative overflow-hidden">
                  <div className="space-y-1">
                    <span className="font-mono text-[10px] font-bold bg-ink text-paper px-2 py-0.5 border border-ink">
                      BARCODE #{loan.resourceBarcode}
                    </span>
                    <h4 className="font-display text-lg font-bold text-ink leading-tight">
                      {loan.resourceTitle}
                    </h4>
                    <p className="font-mono text-xs text-ink/70">
                      Borrowed: {new Date(loan.borrowDate).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="flex flex-col items-end gap-2 shrink-0">
                    <StampSeal
                      label="DATE DUE"
                      sublabel={new Date(loan.dueDate).toLocaleDateString()}
                      color={new Date(loan.dueDate) < new Date() ? 'stamp' : 'ink'}
                      rotate="-4deg"
                    />

                    {loan.status === 'ACTIVE' && (
                      <BrutalButton variant="mint" size="sm" onClick={() => handleReturnItem(loan.id)}>
                        <RotateCcw className="w-3.5 h-3.5" />
                        <span>RETURN ITEM</span>
                      </BrutalButton>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="border-3 border-ink bg-paper-2 p-8 text-center font-mono text-xs font-bold text-ink">
              NO ACTIVE CIRCULATION LOANS BORROWED.
            </div>
          )}
        </div>

        {/* Fines on Perforated Notice Slips */}
        <div className="space-y-4">
          <div className="flex items-center gap-2 border-b-3 border-ink pb-2">
            <DollarSign className="w-5 h-5 text-stamp" />
            <h3 className="font-display text-xl font-black text-ink uppercase">
              OVERDUE FINE SLIPS ({myFines.length})
            </h3>
          </div>

          {myFines.length > 0 ? (
            <div className="space-y-4">
              {myFines.map((fine: FineDTO) => (
                <NoticeSlip
                  key={fine.id}
                  title={fine.resourceTitle}
                  amount={fine.amount}
                  reason={fine.reason}
                  createdAt={fine.createdAt}
                  status={fine.status}
                  onWaive={() => handleWaiveFine(fine.id)}
                />
              ))}
            </div>
          ) : (
            <div className="border-3 border-ink bg-paper-2 p-8 text-center font-mono text-xs font-bold text-ink">
              NO OUTSTANDING OVERDUE FINE NOTICES.
            </div>
          )}
        </div>
      </div>

      {/* Staff Management Section */}
      {isStaff && (
        <div className="space-y-6 pt-6 border-t-3 border-ink">
          <div className="flex items-center gap-2 border-b-3 border-ink pb-2">
            <ShieldCheck className="w-6 h-6 text-stamp" />
            <h2 className="font-display text-2xl font-black text-ink uppercase">
              LIBRARIAN STAFF CIRCULATION CONSOLE
            </h2>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Campus Active Loans Table */}
            <BrutalCard color="lilac" interactive={false} className="space-y-4">
              <h3 className="font-display text-lg font-black text-ink uppercase">
                CAMPUS ACTIVE LOANS ({allLoans.length})
              </h3>
              <div className="overflow-x-auto">
                <table className="w-full text-left font-mono text-xs border-3 border-ink bg-paper-2">
                  <thead className="bg-ink text-paper uppercase">
                    <tr>
                      <th className="p-2.5 border-b-3 border-ink">ASSET</th>
                      <th className="p-2.5 border-b-3 border-ink">BORROWER</th>
                      <th className="p-2.5 border-b-3 border-ink">ACTION</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y-2 divide-ink">
                    {allLoans.map((l: LoanDTO) => (
                      <tr key={l.id} className="hover:bg-highlight/30">
                        <td className="p-2.5 font-bold text-ink">{l.resourceTitle}</td>
                        <td className="p-2.5 text-ink">{l.userFullName}</td>
                        <td className="p-2.5">
                          <BrutalButton variant="destructive" size="sm" onClick={() => handleReturnItem(l.id)}>
                            FORCE RETURN
                          </BrutalButton>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </BrutalCard>

            {/* Campus Fine Waivers Table */}
            <BrutalCard color="lilac" interactive={false} className="space-y-4">
              <h3 className="font-display text-lg font-black text-ink uppercase">
                CAMPUS FINE WAIVERS ({allFines.length})
              </h3>
              <div className="overflow-x-auto">
                <table className="w-full text-left font-mono text-xs border-3 border-ink bg-paper-2">
                  <thead className="bg-ink text-paper uppercase">
                    <tr>
                      <th className="p-2.5 border-b-3 border-ink">USER</th>
                      <th className="p-2.5 border-b-3 border-ink">AMOUNT</th>
                      <th className="p-2.5 border-b-3 border-ink">WAIVER</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y-2 divide-ink">
                    {allFines.map((f: FineDTO) => (
                      <tr key={f.id} className="hover:bg-highlight/30">
                        <td className="p-2.5 font-bold text-ink">{f.userFullName}</td>
                        <td className="p-2.5 font-bold text-stamp">${f.amount.toFixed(2)}</td>
                        <td className="p-2.5">
                          {f.status === 'PENDING' ? (
                            <BrutalButton variant="mint" size="sm" onClick={() => handleWaiveFine(f.id)}>
                              APPROVE WAIVER
                            </BrutalButton>
                          ) : (
                            <span className="font-mono text-xs font-bold text-ink uppercase">
                              {f.status}
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </BrutalCard>
          </div>
        </div>
      )}

      {/* Add Resource Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/60 backdrop-blur-xs p-4">
          <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal max-w-md w-full space-y-4">
            <h3 className="font-display text-xl font-black text-ink uppercase">
              REGISTER NEW CIRCULATION ASSET
            </h3>
            <form onSubmit={handleCreateResource} className="space-y-3 font-mono text-xs">
              <input
                type="text"
                placeholder="Barcode (e.g. HW-ESP32-01)"
                required
                value={barcode}
                onChange={(e) => setBarcode(e.target.value)}
                className="w-full bg-paper border-3 border-ink px-3 py-2 text-ink font-bold focus:outline-none"
              />
              <input
                type="text"
                placeholder="Asset Title"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="w-full bg-paper border-3 border-ink px-3 py-2 text-ink font-bold focus:outline-none"
              />
              <input
                type="text"
                placeholder="Author / Manufacturer Brand"
                required
                value={authorOrBrand}
                onChange={(e) => setAuthorOrBrand(e.target.value)}
                className="w-full bg-paper border-3 border-ink px-3 py-2 text-ink font-bold focus:outline-none"
              />
              <select
                value={type}
                onChange={(e) => setType(e.target.value)}
                className="w-full bg-paper border-3 border-ink px-3 py-2 text-ink font-bold focus:outline-none"
              >
                <option value="BOOK">Academic Book / Textbook</option>
              </select>
              <input
                type="number"
                min="1"
                placeholder="Total Quantity"
                required
                value={totalQuantity}
                onChange={(e) => setTotalQuantity(Number(e.target.value))}
                className="w-full bg-paper border-3 border-ink px-3 py-2 text-ink font-bold focus:outline-none"
              />
              <input
                type="text"
                placeholder="Location (e.g. Rack B2)"
                required
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                className="w-full bg-paper border-3 border-ink px-3 py-2 text-ink font-bold focus:outline-none"
              />
              <input
                type="text"
                placeholder="Comma separated tags (e.g. IoT, Sensors)"
                value={tags}
                onChange={(e) => setTags(e.target.value)}
                className="w-full bg-paper border-3 border-ink px-3 py-2 text-ink font-bold focus:outline-none"
              />

              <div className="flex items-center gap-3 pt-2">
                <BrutalButton type="button" variant="ghost" className="w-1/2" onClick={() => setShowAddModal(false)}>
                  CANCEL
                </BrutalButton>
                <BrutalButton type="submit" variant="primary" className="w-1/2">
                  SAVE ASSET
                </BrutalButton>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
