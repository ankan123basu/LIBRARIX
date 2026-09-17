import React from 'react';

export interface BrutalButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'destructive' | 'mint' | 'sky';
  size?: 'sm' | 'md' | 'lg';
  children: React.ReactNode;
}

export default function BrutalButton({
  variant = 'primary',
  size = 'md',
  className = '',
  children,
  ...props
}: BrutalButtonProps) {
  const variantMap = {
    primary: 'bg-highlight text-ink hover:bg-[#FADB30]',
    secondary: 'bg-blush text-ink hover:bg-[#F294BE]',
    ghost: 'bg-paper-2 text-ink hover:bg-paper',
    destructive: 'bg-stamp text-paper hover:bg-[#C92C21]',
    mint: 'bg-mint text-ink hover:bg-[#6CD897]',
    sky: 'bg-sky text-ink hover:bg-[#8BBAEE]',
  };

  const sizeMap = {
    sm: 'px-3 py-1.5 text-xs font-mono font-bold',
    md: 'px-5 py-2.5 text-sm font-body font-bold',
    lg: 'px-7 py-3.5 text-base font-display font-bold uppercase tracking-tight',
  };

  return (
    <button
      className={`border-3 border-ink shadow-brutal active:translate-x-[6px] active:translate-y-[6px] active:shadow-none focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-ink focus-visible:ring-offset-2 transition-all duration-100 inline-flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed disabled:active:translate-x-0 disabled:active:translate-y-0 disabled:active:shadow-brutal ${variantMap[variant]} ${sizeMap[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
