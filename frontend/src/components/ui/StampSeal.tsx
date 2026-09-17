import React from 'react';

export interface StampSealProps {
  label: string;
  sublabel?: string;
  color?: 'stamp' | 'mint' | 'ink';
  rotate?: string;
  className?: string;
}

export default function StampSeal({
  label,
  sublabel,
  color = 'stamp',
  rotate = '-8deg',
  className = '',
}: StampSealProps) {
  const colorMap = {
    stamp: 'border-stamp text-stamp',
    mint: 'border-mint text-mint',
    ink: 'border-ink text-ink',
  };

  return (
    <div
      style={{ transform: `rotate(${rotate})` }}
      className={`inline-block border-4 border-double px-4 py-2 font-mono font-black uppercase text-center select-none mix-blend-multiply ${colorMap[color]} ${className}`}
    >
      <span className="block text-sm sm:text-base tracking-widest leading-none">
        {label}
      </span>
      {sublabel && (
        <span className="block text-[10px] tracking-wider mt-1 opacity-90">
          {sublabel}
        </span>
      )}
    </div>
  );
}
