import React from 'react';

export interface BrutalCardProps extends React.HTMLAttributes<HTMLDivElement> {
  color?: 'paper' | 'paper-2' | 'highlight' | 'mint' | 'sky' | 'blush' | 'lilac' | 'stamp';
  interactive?: boolean;
  children: React.ReactNode;
}

export default function BrutalCard({
  color = 'paper-2',
  interactive = true,
  className = '',
  children,
  ...props
}: BrutalCardProps) {
  const colorMap = {
    paper: 'bg-paper',
    'paper-2': 'bg-paper-2',
    highlight: 'bg-highlight',
    mint: 'bg-mint',
    sky: 'bg-sky',
    blush: 'bg-blush',
    lilac: 'bg-lilac',
    stamp: 'bg-stamp text-paper',
  };

  const interactiveStyles = interactive
    ? 'transition-all duration-150 hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-brutal-hover'
    : '';

  return (
    <div
      className={`border-3 border-ink shadow-brutal p-5 ${colorMap[color]} ${interactiveStyles} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
