import React from 'react';

export interface StatBlockProps {
  value: string | number;
  label: string;
  color?: 'highlight' | 'mint' | 'sky' | 'blush' | 'lilac' | 'paper-2';
  className?: string;
}

export default function StatBlock({
  value,
  label,
  color = 'paper-2',
  className = '',
}: StatBlockProps) {
  const colorMap = {
    highlight: 'bg-highlight',
    mint: 'bg-mint',
    sky: 'bg-sky',
    blush: 'bg-blush',
    lilac: 'bg-lilac',
    'paper-2': 'bg-paper-2',
  };

  return (
    <div className={`border-3 border-ink shadow-brutal p-4 ${colorMap[color]} ${className}`}>
      <span className="block font-display text-4xl sm:text-5xl font-black text-ink leading-none tracking-tight">
        {value}
      </span>
      <span className="block font-mono text-xs font-bold text-ink uppercase tracking-wider mt-2">
        {label}
      </span>
    </div>
  );
}
