import React from 'react';
import Image from 'next/image';

interface LogoProps {
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'dark';
  showTagline?: boolean;
  className?: string;
}

export default function Logo({
  size = 'md',
  variant = 'default',
  showTagline = true,
  className = '',
}: LogoProps) {
  const sizeMap = {
    sm: { mark: 28, text: 'text-lg', tagline: 'text-[9px]' },
    md: { mark: 36, text: 'text-2xl', tagline: 'text-[10px]' },
    lg: { mark: 48, text: 'text-4xl', tagline: 'text-xs' },
  };

  const current = sizeMap[size];

  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <div className="relative shrink-0 flex items-center justify-center">
        <Image
          src="/brand/mark.svg"
          alt="LIBRARIX Logo Mark"
          width={current.mark}
          height={current.mark}
          priority
          className="object-contain"
        />
      </div>

      <div className="flex flex-col leading-none">
        <span className={`font-black tracking-tight font-display ${current.text} ${
          variant === 'dark' ? 'text-white' : 'text-[#0B0B0B]'
        }`}>
          LIBRARIX
        </span>
        {showTagline && (
          <span className={`font-mono font-medium tracking-wide uppercase mt-0.5 ${current.tagline} ${
            variant === 'dark' ? 'text-[#FFE14D]' : 'text-[#0B0B0B]/80'
          }`}>
            Smart Campus Library
          </span>
        )}
      </div>
    </div>
  );
}
