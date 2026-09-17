import React from 'react';

export interface StickerProps {
  color?: 'highlight' | 'mint' | 'blush' | 'sky' | 'lilac' | 'stamp';
  rotate?: '-6deg' | '-4deg' | '-2deg' | '2deg' | '4deg' | '6deg';
  children: React.ReactNode;
  className?: string;
}

export default function Sticker({
  color = 'highlight',
  rotate = '4deg',
  children,
  className = '',
}: StickerProps) {
  const colorMap = {
    highlight: 'bg-highlight text-ink',
    mint: 'bg-mint text-ink',
    blush: 'bg-blush text-ink',
    sky: 'bg-sky text-ink',
    lilac: 'bg-lilac text-ink',
    stamp: 'bg-stamp text-paper',
  };

  const rotateStyle = { transform: `rotate(${rotate})` };

  return (
    <div
      style={rotateStyle}
      className={`inline-flex items-center px-3 py-1 border-3 border-ink shadow-brutal-sm font-mono text-xs font-bold ${colorMap[color]} ${className}`}
    >
      {children}
    </div>
  );
}
