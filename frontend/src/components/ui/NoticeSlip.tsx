import React from 'react';

export interface NoticeSlipProps {
  title: string;
  amount: number;
  reason: string;
  createdAt: string;
  status: string;
  onWaive?: () => void;
  className?: string;
}

export default function NoticeSlip({
  title,
  amount,
  reason,
  createdAt,
  status,
  onWaive,
  className = '',
}: NoticeSlipProps) {
  return (
    <div className={`border-3 border-ink bg-paper-2 shadow-brutal p-6 relative ${className}`}>
      {/* Perforated Top Tear Edge */}
      <div className="absolute -top-1 left-0 right-0 border-b-2 border-dashed border-ink" />

      <div className="flex items-start justify-between border-b-3 border-ink pb-4 mb-4">
        <div>
          <span className="font-mono text-xs font-bold text-stamp uppercase tracking-widest block mb-1">
            ⚠️ OFFICIAL OVERDUE CIRCULATION NOTICE
          </span>
          <h4 className="font-display text-lg font-bold text-ink">{title}</h4>
        </div>
        <div className="text-right">
          <span className="font-display text-2xl font-black text-stamp block">
            ${amount.toFixed(2)}
          </span>
          <span className="font-mono text-[10px] text-ink uppercase">Fine Due</span>
        </div>
      </div>

      <div className="space-y-2 font-mono text-xs text-ink mb-6">
        <p><strong>REASON:</strong> {reason}</p>
        <p><strong>ISSUED:</strong> {new Date(createdAt).toLocaleDateString()}</p>
        <p><strong>STATUS:</strong> <span className="underline font-bold uppercase">{status}</span></p>
      </div>

      {onWaive && (status === 'PENDING' || status === 'UNPAID') && (
        <button
          onClick={onWaive}
          className="w-full py-2 bg-blush text-ink border-3 border-ink shadow-brutal-sm font-mono text-xs font-bold hover:bg-[#F294BE] active:translate-x-[3px] active:translate-y-[3px] active:shadow-none"
        >
          REQUEST ADMINISTRATIVE WAIVER
        </button>
      )}

      {/* Perforated Bottom Tear Edge */}
      <div className="absolute -bottom-1 left-0 right-0 border-b-2 border-dashed border-ink" />
    </div>
  );
}
