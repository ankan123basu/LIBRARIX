import React from 'react';

export interface StatusBadgeProps {
  status: string;
  className?: string;
}

export default function StatusBadge({ status, className = '' }: StatusBadgeProps) {
  const normalized = status.toUpperCase();

  let bgClass = 'bg-paper-2 text-ink';
  let label = status;

  if (normalized === 'AVAILABLE') {
    bgClass = 'bg-highlight text-ink';
    label = 'AVAILABLE NOW';
  } else if (normalized === 'BORROWED' || normalized === 'ACTIVE') {
    bgClass = 'bg-mint text-ink';
    label = 'CHECKED OUT';
  } else if (normalized === 'QUEUED' || normalized === 'WAITING') {
    bgClass = 'bg-blush text-ink';
    label = 'WAITLIST QUEUED';
  } else if (normalized === 'OVERDUE' || normalized === 'UNPAID') {
    bgClass = 'bg-stamp text-paper';
    label = 'OVERDUE NOTICE';
  } else if (normalized === 'RETURNED' || normalized === 'PAID' || normalized === 'WAIVED') {
    bgClass = 'bg-sky text-ink';
    label = status;
  }

  return (
    <span
      className={`inline-flex items-center px-3 py-1 rounded-full border-3 border-ink font-mono text-[11px] font-extrabold uppercase tracking-wide ${bgClass} ${className}`}
    >
      {label}
    </span>
  );
}
