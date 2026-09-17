'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { ArrowRight, BookOpen, Cpu, Wrench, DoorOpen } from 'lucide-react';
import { ResourceDTO } from '@/lib/types';
import StatusBadge from '@/components/ui/StatusBadge';
import BrutalButton from '@/components/ui/BrutalButton';

interface ResourceCardProps {
  resource: ResourceDTO;
}

export default function ResourceCard({ resource }: ResourceCardProps) {
  const isAvailable = resource.availableQuantity > 0;
  const [imgError, setImgError] = useState(false);

  const typeColorMap = {
    BOOK: 'bg-highlight',
    HARDWARE: 'bg-sky',
    LAB_KIT: 'bg-mint',
    SEMINAR_ROOM: 'bg-blush',
  };

  const typeIconMap = {
    BOOK: <BookOpen className="w-4 h-4 text-ink" />,
    HARDWARE: <Cpu className="w-4 h-4 text-ink" />,
    LAB_KIT: <Wrench className="w-4 h-4 text-ink" />,
    SEMINAR_ROOM: <DoorOpen className="w-4 h-4 text-ink" />,
  };

  return (
    <div className="border-3 border-ink bg-paper-2 shadow-brutal p-5 flex flex-col justify-between transition-all duration-150 hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-brutal-hover">
      <div>
        {/* Header Bar with Call Number Spine Label */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className="font-mono text-xs font-black bg-ink text-highlight px-2.5 py-1 border-2 border-ink">
            CALL #{resource.barcode}
          </span>
          <StatusBadge status={isAvailable ? 'AVAILABLE' : 'QUEUED'} />
        </div>

        {/* Real Cover Image or Styled Neobrutalist Graphic Banner */}
        {resource.coverImageUrl && !imgError ? (
          <div className="w-full h-48 border-3 border-ink overflow-hidden mb-3 bg-paper relative shadow-brutal-sm">
            <img
              src={resource.coverImageUrl}
              alt={resource.title}
              className="w-full h-full object-cover"
              onError={() => setImgError(true)}
            />
          </div>
        ) : (
          <div className={`w-full h-40 border-3 border-ink ${typeColorMap[resource.type] || 'bg-sky'} mb-3 p-4 flex flex-col justify-between relative shadow-brutal-sm overflow-hidden`}>
            <div className="flex items-center justify-between">
              <span className="font-mono text-xs font-black bg-ink text-paper px-2 py-0.5 uppercase flex items-center gap-1.5">
                {typeIconMap[resource.type] || <BookOpen className="w-4 h-4" />}
                <span>{resource.type}</span>
              </span>
              <span className="font-mono text-[10px] font-bold text-ink bg-paper px-1.5 py-0.5 border border-ink">
                {resource.location}
              </span>
            </div>

            <div>
              <p className="font-display text-base font-black text-ink line-clamp-2 leading-snug uppercase">
                {resource.title}
              </p>
              <p className="font-mono text-[10px] font-bold text-ink/80 mt-1">
                {resource.authorOrBrand}
              </p>
            </div>
          </div>
        )}

        <h3 className="font-display text-lg font-black text-ink leading-tight line-clamp-1 mt-1">
          {resource.title}
        </h3>
        <p className="font-mono text-xs font-bold text-ink/70 mt-0.5 line-clamp-1">
          {resource.authorOrBrand}
        </p>

        {resource.description && (
          <p className="font-body text-xs text-ink mt-2 line-clamp-2 leading-relaxed">
            {resource.description}
          </p>
        )}

        {/* Tags */}
        <div className="mt-3 flex flex-wrap gap-1.5">
          {resource.tags?.map((tag) => (
            <span
              key={tag}
              className="font-mono text-[10px] font-bold bg-paper border-2 border-ink px-2 py-0.5"
            >
              #{tag}
            </span>
          ))}
        </div>
      </div>

      {/* Footer Info & Action */}
      <div className="mt-5 pt-3 border-t-3 border-ink flex items-center justify-between gap-2">
        <div className="font-mono text-xs font-bold text-ink">
          QUEUED: <span className="bg-highlight px-1.5 py-0.5 border border-ink">{resource.activeQueueCount}</span>
        </div>

        <Link href={`/resource/${resource.id}`}>
          <BrutalButton variant={isAvailable ? 'primary' : 'secondary'} size="sm">
            <span>{isAvailable ? 'BORROW' : 'JOIN QUEUE'}</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </BrutalButton>
        </Link>
      </div>
    </div>
  );
}
