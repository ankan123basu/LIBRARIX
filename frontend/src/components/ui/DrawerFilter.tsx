import React from 'react';
import { X, Filter, Tag, BookOpen } from 'lucide-react';
import BrutalButton from './BrutalButton';

export interface DrawerFilterProps {
  isOpen: boolean;
  onClose: () => void;
  selectedType: string;
  onSelectType: (type: string) => void;
  selectedTag: string;
  onSelectTag: (tag: string) => void;
  availableTags: string[];
  onReset: () => void;
}

export default function DrawerFilter({
  isOpen,
  onClose,
  selectedType,
  onSelectType,
  selectedTag,
  onSelectTag,
  availableTags,
  onReset,
}: DrawerFilterProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-ink/50 backdrop-blur-xs animate-fade-in">
      <div className="w-full max-w-md bg-paper border-l-3 border-ink h-full shadow-brutal p-6 overflow-y-auto flex flex-col justify-between">
        <div className="space-y-6">
          {/* Header */}
          <div className="flex items-center justify-between border-b-3 border-ink pb-4">
            <div className="flex items-center space-x-2">
              <Filter className="w-5 h-5 text-ink" />
              <h3 className="font-display text-xl font-black text-ink uppercase">
                Card Catalogue Drawer
              </h3>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 border-3 border-ink bg-paper-2 hover:bg-blush shadow-brutal-sm"
            >
              <X className="w-5 h-5 text-ink" />
            </button>
          </div>

          {/* Resource Type Category Tab Dividers */}
          <div className="space-y-3">
            <span className="font-mono text-xs font-bold text-ink uppercase block">
              01. Resource Category
            </span>
            <div className="grid grid-cols-2 gap-2">
              {['ALL', 'BOOK'].map((type) => (
                <button
                  key={type}
                  onClick={() => onSelectType(type === 'ALL' ? '' : type)}
                  className={`p-2.5 text-xs font-mono font-bold border-3 border-ink text-left transition-all ${
                    (type === 'ALL' && !selectedType) || selectedType === type
                      ? 'bg-highlight shadow-brutal-sm translate-x-[-2px] translate-y-[-2px]'
                      : 'bg-paper-2 hover:bg-sky'
                  }`}
                >
                  {type === 'ALL' ? 'ALL BOOKS' : 'BOOKS'}
                </button>
              ))}
            </div>
          </div>

          {/* Tag Filter Tabs */}
          {availableTags.length > 0 && (
            <div className="space-y-3">
              <span className="font-mono text-xs font-bold text-ink uppercase block">
                02. Topic & Specialization Tags
              </span>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => onSelectTag('')}
                  className={`px-3 py-1.5 text-xs font-mono font-bold border-3 border-ink ${
                    !selectedTag ? 'bg-mint shadow-brutal-sm' : 'bg-paper-2 hover:bg-sky'
                  }`}
                >
                  All Tags
                </button>
                {availableTags.map((tag) => (
                  <button
                    key={tag}
                    onClick={() => onSelectTag(tag)}
                    className={`px-3 py-1.5 text-xs font-mono font-bold border-3 border-ink ${
                      selectedTag === tag ? 'bg-mint shadow-brutal-sm' : 'bg-paper-2 hover:bg-sky'
                    }`}
                  >
                    #{tag}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="border-t-3 border-ink pt-4 mt-8 flex items-center gap-3">
          <BrutalButton variant="ghost" className="w-1/2" onClick={onReset}>
            Reset Drawer
          </BrutalButton>
          <BrutalButton variant="primary" className="w-1/2" onClick={onClose}>
            Apply Filters
          </BrutalButton>
        </div>
      </div>
    </div>
  );
}
