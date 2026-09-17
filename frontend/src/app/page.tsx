import Link from 'next/link';
import dynamic from 'next/dynamic';
import { ArrowRight, Zap, Clock, ShieldCheck, Layers, BookOpen, Sparkles } from 'lucide-react';
import BrutalCard from '@/components/ui/BrutalCard';
import BrutalButton from '@/components/ui/BrutalButton';
import Sticker from '@/components/ui/Sticker';
import SectionRule from '@/components/ui/SectionRule';

const LiquidChrome = dynamic(() => import('@/components/ui/LiquidChrome'), {
  ssr: false,
});

const Hero3DBook = dynamic(() => import('@/components/ui/Hero3DBook'), {
  ssr: false,
});

export default function Home() {
  return (
    <div className="space-y-12 py-6">
      {/* Hero Section with LiquidChrome WebGL Shader */}
      <div className="relative border-3 border-ink shadow-brutal-lg overflow-hidden p-2 bg-lilac">
        {/* WebGL Shader Container (Pointer events none, z-0) */}
        <div className="absolute inset-0 pointer-events-none z-0">
          <LiquidChrome baseColor={[0.8, 0.72, 1.0]} speed={0.15} interactive={false} />
        </div>

        {/* Hero Foreground Content Overlay (~92% opacity paper block) */}
        <div className="relative z-10 border-3 border-ink bg-paper/95 p-6 sm:p-10 space-y-8 backdrop-blur-xs">
          {/* Stickers pinned at angles */}
          <div className="absolute -top-4 -left-3 hidden sm:block">
            <Sticker color="mint" rotate="-4deg">
              ⚡ STOMP WebSocket Live
            </Sticker>
          </div>
          <div className="absolute -top-4 -right-3 hidden sm:block">
            <Sticker color="blush" rotate="4deg">
              📚 3D Interactive Asset Layer
            </Sticker>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            {/* Left Content Column */}
            <div className="lg:col-span-7 space-y-6">
              <div className="inline-flex items-center gap-2 border-3 border-ink bg-sky px-3 py-1 font-mono text-xs font-bold uppercase shadow-brutal-sm">
                <Sparkles className="w-4 h-4 text-ink" />
                <span>Next-Gen Campus Library Platform</span>
              </div>

              <h1 className="font-display text-4xl sm:text-5xl lg:text-6xl font-black text-ink leading-[0.95] tracking-tight">
                SMART CAMPUS RESOURCE MANAGEMENT &{' '}
                <span className="border-3 border-ink bg-highlight shadow-brutal px-3 py-1 inline-block my-2">
                  PRIORITY WAITLIST QUEUE
                </span>
              </h1>

              <p className="font-body text-base sm:text-lg text-ink leading-relaxed">
                LIBRARIX is an enterprise-grade campus textbook circulation system—powered by a Weighted Fair Reservation Queue Engine, 3D Spatial Shelf Placement, AI Semantic Search, and real-time STOMP WebSocket notifications for academic books.
              </p>

              <div className="flex flex-wrap items-center gap-4 pt-2">
                <Link href="/catalog">
                  <BrutalButton variant="primary" size="lg">
                    <span>EXPLORE CATALOGUE</span>
                    <ArrowRight className="w-5 h-5" />
                  </BrutalButton>
                </Link>
                <Link href="/shelf-hall">
                  <BrutalButton variant="secondary" size="lg">
                    <Layers className="w-5 h-5" />
                    <span>ENTER 3D SHELF HALL</span>
                  </BrutalButton>
                </Link>
              </div>
            </div>

            {/* Right 3D Interactive Asset Column */}
            <div className="lg:col-span-5 border-3 border-ink bg-paper-2 p-2 shadow-brutal relative">
              <Hero3DBook />
            </div>
          </div>
        </div>
      </div>

      {/* 3 Real Differentiators */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <BrutalCard color="sky" className="space-y-3">
          <div className="w-10 h-10 border-3 border-ink bg-paper-2 flex items-center justify-center shadow-brutal-sm">
            <Zap className="w-6 h-6 text-ink" />
          </div>
          <h3 className="font-display text-xl font-black text-ink uppercase">
            Weighted Fair Queue Engine
          </h3>
          <p className="font-body text-sm text-ink leading-normal">
            Calculates dynamic score boosts (Score = WaitTime × 1.5 + Urgency + UserTier) to guarantee zero queue starvation for capstone students.
          </p>
        </BrutalCard>

        <BrutalCard color="mint" className="space-y-3">
          <div className="w-10 h-10 border-3 border-ink bg-paper-2 flex items-center justify-center shadow-brutal-sm">
            <Clock className="w-6 h-6 text-ink" />
          </div>
          <h3 className="font-display text-xl font-black text-ink uppercase">
            Real-Time STOMP Toasts
          </h3>
          <p className="font-body text-sm text-ink leading-normal">
            Subscribes to live WebSocket topics to push immediate alerts to your device the second a reserved resource is checked back in.
          </p>
        </BrutalCard>

        <BrutalCard color="blush" className="space-y-3">
          <div className="w-10 h-10 border-3 border-ink bg-paper-2 flex items-center justify-center shadow-brutal-sm">
            <ShieldCheck className="w-6 h-6 text-ink" />
          </div>
          <h3 className="font-display text-xl font-black text-ink uppercase">
            Rule-Based Fine Engine
          </h3>
          <p className="font-body text-sm text-ink leading-normal">
            Automated per-category daily rates, 24-hour grace periods, fine caps, and administrative waiver request workflows.
          </p>
        </BrutalCard>
      </div>

      {/* Numbered 3-Step Sequence */}
      <SectionRule title="CIRCULATION WORKFLOW" number="01" />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal space-y-3 relative">
          <span className="font-display text-3xl font-black bg-ink text-highlight px-3 py-1 border-2 border-ink inline-block">
            01
          </span>
          <h4 className="font-display text-lg font-bold text-ink uppercase">
            Browse & Search Catalogue
          </h4>
          <p className="font-body text-xs text-ink leading-relaxed">
            Scan physical items, search by title/author/barcode, or open the 3D Shelf Hall to inspect items visually in 3D space.
          </p>
        </div>

        <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal space-y-3 relative">
          <span className="font-display text-3xl font-black bg-ink text-blush px-3 py-1 border-2 border-ink inline-block">
            02
          </span>
          <h4 className="font-display text-lg font-bold text-ink uppercase">
            Borrow or Join Waitlist
          </h4>
          <p className="font-body text-xs text-ink leading-relaxed">
            Borrow available assets instantly or enter the priority waitlist queue with your coursework urgency level.
          </p>
        </div>

        <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal space-y-3 relative">
          <span className="font-display text-3xl font-black bg-ink text-mint px-3 py-1 border-2 border-ink inline-block">
            03
          </span>
          <h4 className="font-display text-lg font-bold text-ink uppercase">
            Receive Notification & Return
          </h4>
          <p className="font-body text-xs text-ink leading-relaxed">
            Get instant WebSocket push alerts when your turn arrives. Return on time to avoid automatic fine slip generation.
          </p>
        </div>
      </div>
    </div>
  );
}
