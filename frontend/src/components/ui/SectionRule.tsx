import React from 'react';

export interface SectionRuleProps {
  title: string;
  number?: string;
  className?: string;
}

export default function SectionRule({ title, number, className = '' }: SectionRuleProps) {
  return (
    <div className={`relative flex items-center my-8 ${className}`}>
      <div className="grow border-t-3 border-ink" />
      <div className="shrink-0 mx-4 border-3 border-ink bg-paper px-4 py-1.5 shadow-brutal-sm flex items-center gap-2">
        {number && (
          <span className="font-mono text-xs font-black bg-ink text-paper px-2 py-0.5">
            {number}
          </span>
        )}
        <h2 className="font-display text-lg font-black text-ink uppercase tracking-tight">
          {title}
        </h2>
      </div>
      <div className="grow border-t-3 border-ink" />
    </div>
  );
}
