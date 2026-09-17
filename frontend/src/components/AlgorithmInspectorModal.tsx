'use client';

import React, { useState } from 'react';
import { X, Sparkles, Cpu, Layers, Calculator, Search, ShieldCheck } from 'lucide-react';
import BrutalButton from '@/components/ui/BrutalButton';
import Sticker from '@/components/ui/Sticker';

interface Props {
  isOpen: boolean;
  onClose: () => void;
}

export default function AlgorithmInspectorModal({ isOpen, onClose }: Props) {
  const [activeTab, setActiveTab] = useState<'rag' | 'wfq' | 'slotting' | 'fine'>('rag');

  // RAG Vector Search Simulator State
  const [ragQuery, setRagQuery] = useState('distributed consensus stream processing');

  // WFQ Priority Queue Simulator State
  const [waitHours, setWaitHours] = useState(24);
  const [urgency, setUrgency] = useState<'STANDARD' | 'HIGH' | 'CRITICAL'>('CRITICAL');
  const [userTier, setUserTier] = useState<'REGULAR' | 'CAPSTONE' | 'FACULTY'>('CAPSTONE');

  if (!isOpen) return null;

  // WFQ Formula Calculations
  const urgencyBoost = urgency === 'STANDARD' ? 10 : urgency === 'HIGH' ? 25 : 50;
  const tierMultiplier = userTier === 'REGULAR' ? 1.0 : userTier === 'CAPSTONE' ? 1.5 : 2.0;
  const agingScore = waitHours * 1.5;
  const totalPriorityScore = agingScore + urgencyBoost + tierMultiplier * 10.0;

  // RAG Mock Tokenization & Similarity Calculation
  const ragTokens = ragQuery.toLowerCase().split(/\s+/).filter(Boolean);
  const sampleDocs = [
    { title: 'Designing Data-Intensive Applications', author: 'Martin Kleppmann', tags: ['distributed-systems', 'consensus', 'stream-processing'], score: 0.94 },
    { title: 'Kafka: The Definitive Guide', author: 'Gwen Shapira', tags: ['event-streaming', 'distributed-systems', 'partitioning'], score: 0.88 },
    { title: 'Cassandra: The Definitive Guide', author: 'Jeff Carpenter', tags: ['nosql', 'distributed-database', 'replication'], score: 0.79 },
    { title: 'Computer Networking Top-Down', author: 'James Kurose', tags: ['networking', 'protocols', 'tcp-ip'], score: 0.42 },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/70 backdrop-blur-xs p-4 overflow-y-auto">
      <div className="border-3 border-ink bg-paper-2 shadow-brutal-lg max-w-4xl w-full my-8 relative flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="border-b-3 border-ink bg-sky p-5 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-3">
            <Cpu className="w-6 h-6 text-ink" />
            <div>
              <div className="flex items-center gap-2">
                <h2 className="font-display text-2xl font-black text-ink uppercase tracking-tight">
                  CORE ALGORITHM & AI RAG INSPECTOR
                </h2>
                <Sticker color="highlight" rotate="-2deg">
                  LIVE DEMO MATH
                </Sticker>
              </div>
              <p className="font-mono text-xs text-ink/80">
                Interactive verification panel for RAG vector search, WFQ waitlists, 3D slotting, and fine logic
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 border-3 border-ink bg-paper hover:bg-stamp hover:text-paper font-bold transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Tabs */}
        <div className="flex overflow-x-auto border-b-3 border-ink bg-paper shrink-0">
          {[
            { id: 'rag', label: '1. RAG VECTOR SEARCH', icon: <Search className="w-4 h-4" /> },
            { id: 'wfq', label: '2. WEIGHTED FAIR QUEUE', icon: <Calculator className="w-4 h-4" /> },
            { id: 'slotting', label: '3. 3D WAREHOUSE SLOTTING', icon: <Layers className="w-4 h-4" /> },
            { id: 'fine', label: '4. RULE-BASED FINE ENGINE', icon: <ShieldCheck className="w-4 h-4" /> },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex items-center gap-2 px-5 py-3 border-r-3 border-ink font-mono text-xs font-bold uppercase transition-all whitespace-nowrap ${
                activeTab === tab.id ? 'bg-highlight text-ink font-black' : 'bg-paper-2 hover:bg-paper text-ink/70'
              }`}
            >
              {tab.icon}
              <span>{tab.label}</span>
            </button>
          ))}
        </div>

        {/* Body Content */}
        <div className="p-6 overflow-y-auto space-y-6 font-mono text-xs">
          {/* TAB 1: RAG VECTOR SEARCH */}
          {activeTab === 'rag' && (
            <div className="space-y-6">
              <div className="border-3 border-ink bg-paper p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-black text-ink uppercase">TRY LIVE RAG VECTOR SEARCH QUERY</span>
                  <span className="bg-sky border border-ink px-2 py-0.5 text-[10px] font-bold">
                    HF L2 Euclidean Distance Normalized
                  </span>
                </div>
                <div className="relative">
                  <input
                    type="text"
                    value={ragQuery}
                    onChange={(e) => setRagQuery(e.target.value)}
                    placeholder="Enter natural language query..."
                    className="w-full bg-paper-2 border-3 border-ink pl-3 pr-4 py-2 font-bold text-ink focus:outline-none focus:ring-2 focus:ring-ink"
                  />
                </div>
                <div className="flex flex-wrap gap-2 text-[11px]">
                  <span className="font-bold text-ink">Extracted Query Tokens:</span>
                  {ragTokens.map((t, idx) => (
                    <span key={idx} className="bg-highlight border border-ink px-2 py-0.5 font-bold">
                      [{t}]
                    </span>
                  ))}
                </div>
              </div>

              {/* Vector Match Computation Output */}
              <div className="space-y-3">
                <h4 className="font-display text-base font-black text-ink uppercase">
                  SIMULATED L2 VECTOR SPACE MATCHES & SIMILARITY SCORES
                </h4>
                <div className="space-y-3">
                  {sampleDocs.map((doc, idx) => (
                    <div key={idx} className="border-3 border-ink bg-paper p-4 flex items-center justify-between gap-4">
                      <div className="space-y-1">
                        <h5 className="font-display text-sm font-black text-ink">{doc.title}</h5>
                        <p className="text-ink/80 text-[11px] font-bold">{doc.author}</p>
                        <div className="flex flex-wrap gap-1 pt-1">
                          {doc.tags.map((tag) => (
                            <span key={tag} className="bg-paper-2 border border-ink text-[10px] px-1.5 py-0.5">
                              #{tag}
                            </span>
                          ))}
                        </div>
                      </div>
                      <div className="text-right shrink-0">
                        <div className="font-display text-xl font-black text-ink">
                          {(doc.score * 100).toFixed(1)}%
                        </div>
                        <span className="text-[10px] font-bold text-mint bg-ink px-2 py-0.5 border border-ink inline-block mt-1">
                          L2 DISTANCE MATCH
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: WEIGHTED FAIR QUEUE */}
          {activeTab === 'wfq' && (
            <div className="space-y-6">
              <div className="border-3 border-ink bg-highlight p-4 space-y-2">
                <h4 className="font-display text-base font-black text-ink uppercase">
                  WEIGHTED FAIR QUEUE (WFQ) FORMULA BREAKDOWN
                </h4>
                <div className="bg-paper border-2 border-ink p-3 font-mono text-xs space-y-1 font-bold">
                  <p className="text-stamp">
                    Score = (WaitTimeHours × 1.5) + UrgencyBoost + (UserTierMultiplier × 10.0)
                  </p>
                </div>
              </div>

              {/* Interactive Controls */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 border-3 border-ink bg-paper p-4">
                <div className="space-y-2">
                  <label className="font-bold text-ink uppercase block">Wait Time (Hours): {waitHours}h</label>
                  <input
                    type="range"
                    min="0"
                    max="120"
                    value={waitHours}
                    onChange={(e) => setWaitHours(Number(e.target.value))}
                    className="w-full accent-ink"
                  />
                  <p className="text-[10px] text-ink/70">Aging Score: {agingScore.toFixed(1)} pts</p>
                </div>

                <div className="space-y-2">
                  <label className="font-bold text-ink uppercase block">Urgency Level</label>
                  <select
                    value={urgency}
                    onChange={(e) => setUrgency(e.target.value as any)}
                    className="w-full bg-paper-2 border-2 border-ink p-1.5 font-bold"
                  >
                    <option value="STANDARD">Standard Coursework (+10)</option>
                    <option value="HIGH">High Exam Deadline (+25)</option>
                    <option value="CRITICAL">Critical Capstone (+50)</option>
                  </select>
                </div>

                <div className="space-y-2">
                  <label className="font-bold text-ink uppercase block">User Role Tier</label>
                  <select
                    value={userTier}
                    onChange={(e) => setUserTier(e.target.value as any)}
                    className="w-full bg-paper-2 border-2 border-ink p-1.5 font-bold"
                  >
                    <option value="REGULAR">Regular Undergrad (1.0x)</option>
                    <option value="CAPSTONE">Senior Capstone (1.5x)</option>
                    <option value="FACULTY">Faculty / Researcher (2.0x)</option>
                  </select>
                </div>
              </div>

              {/* Live Output Computation Card */}
              <div className="border-3 border-ink bg-paper-2 p-6 text-center space-y-3 shadow-brutal">
                <span className="font-mono text-xs font-black uppercase text-stamp">COMPUTED PRIORITY SCORE</span>
                <div className="font-display text-5xl font-black text-ink">
                  {totalPriorityScore.toFixed(1)} <span className="text-lg">PTS</span>
                </div>
                <div className="flex justify-center gap-4 text-xs font-bold pt-2 border-t-2 border-ink">
                  <span>Aging: +{agingScore.toFixed(1)}</span>
                  <span>Urgency: +{urgencyBoost}</span>
                  <span>Tier Boost: +{(tierMultiplier * 10.0).toFixed(1)}</span>
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: 3D SLOTTING */}
          {activeTab === 'slotting' && (
            <div className="space-y-6">
              <div className="border-3 border-ink bg-paper p-4 space-y-3">
                <h4 className="font-display text-base font-black text-ink uppercase">
                  GREEDY WAREHOUSE SLOTTING ALGORITHM
                </h4>
                <p className="leading-relaxed">
                  LIBRARIX optimizes 3D shelf placement based on demand popularity and co-borrow affinity graphs.
                </p>
                <div className="bg-sky border-2 border-ink p-3 font-bold">
                  Popularity Score = (Historical Loans × 2.0) + (Current Available Quantity × 0.5)
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="border-3 border-ink bg-highlight p-4 space-y-2">
                  <span className="font-black text-xs uppercase block text-ink">TOP 30% POPULARITY</span>
                  <p className="font-bold text-lg text-ink">Eye-Level Height (y = 1.5m)</p>
                  <p className="text-[11px] text-ink/80">
                    Highest demand items placed directly in optimal human ergonomic sightline.
                  </p>
                </div>

                <div className="border-3 border-ink bg-paper p-4 space-y-2">
                  <span className="font-black text-xs uppercase block text-ink">MEDIUM POPULARITY</span>
                  <p className="font-bold text-lg text-ink">Top Level (y = 2.4m)</p>
                  <p className="text-[11px] text-ink/80">
                    Secondary inventory stored on upper tier shelves.
                  </p>
                </div>

                <div className="border-3 border-ink bg-paper p-4 space-y-2">
                  <span className="font-black text-xs uppercase block text-ink">STANDARD POPULARITY</span>
                  <p className="font-bold text-lg text-ink">Bottom Level (y = 0.4m)</p>
                  <p className="text-[11px] text-ink/80">
                    Heavy or standard usage items placed on lower shelf tiers.
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* TAB 4: RULE-BASED FINE ENGINE */}
          {activeTab === 'fine' && (
            <div className="space-y-6">
              <div className="border-3 border-ink bg-paper p-4 space-y-3">
                <h4 className="font-display text-base font-black text-ink uppercase">
                  RULE-BASED OVERDUE FINE & WAIVER ENGINE
                </h4>
                <div className="space-y-2">
                  <div className="flex justify-between border-b-2 border-ink pb-1 font-bold">
                    <span>ASSET TYPE</span>
                    <span>DAILY OVERDUE RATE</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Books & Study Textbooks</span>
                    <span className="font-bold text-stamp">$0.50 / Day</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Hardware Kits & Lab Oscilloscopes</span>
                    <span className="font-bold text-stamp">$2.00 / Day</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Seminar Study Rooms</span>
                    <span className="font-bold text-stamp">$5.00 / Day</span>
                  </div>
                </div>
              </div>

              <div className="border-3 border-ink bg-mint p-4 space-y-2">
                <h5 className="font-bold text-ink uppercase">AUTOMATED WAIVER RULE EVALUATIONS</h5>
                <p className="text-[11px] leading-relaxed">
                  First-time offenses and approved Senior Capstone deadline extensions trigger automatic fine waivers via the Rule-Based Waiver Engine.
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="border-t-3 border-ink bg-paper p-4 flex justify-end shrink-0">
          <BrutalButton variant="primary" size="md" onClick={onClose}>
            CLOSE INSPECTOR
          </BrutalButton>
        </div>
      </div>
    </div>
  );
}
