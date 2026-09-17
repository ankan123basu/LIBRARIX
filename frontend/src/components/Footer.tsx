'use client';

import React from 'react';
import Link from 'next/link';
import { ArrowUp, Globe, DollarSign, ShieldCheck, Sparkles, BookOpen, Github } from 'lucide-react';
import Logo from '@/components/brand/Logo';
import Sticker from '@/components/ui/Sticker';

export default function Footer() {
  const scrollToTop = () => {
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  return (
    <footer className="border-t-3 border-ink bg-paper mt-16">
      {/* Back To Top Full-Width Strip */}
      <button
        onClick={scrollToTop}
        className="w-full bg-paper-2 hover:bg-highlight border-b-3 border-ink py-3 font-mono text-xs font-black text-ink flex items-center justify-center space-x-2 transition-colors active:translate-y-[1px]"
      >
        <ArrowUp className="w-4 h-4" />
        <span>BACK TO TOP</span>
      </button>

      {/* Main Multi-Column Amazon-Style Link Grid */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Column 1: Get to Know Us */}
          <div className="space-y-3 font-mono text-xs">
            <h4 className="font-display text-sm font-black text-ink uppercase tracking-wider border-b-2 border-ink pb-2">
              GET TO KNOW US
            </h4>
            <ul className="space-y-2 text-ink/80 font-bold">
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  About LIBRARIX Engine
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  STOMP WebSocket Alert Engine
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  Weighted Fair Queue Algorithm
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  Rule-Based Fine & Waiver Engine
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  Campus Press & Media Kit
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 2: Circulation & Assets */}
          <div className="space-y-3 font-mono text-xs">
            <h4 className="font-display text-sm font-black text-ink uppercase tracking-wider border-b-2 border-ink pb-2">
              CIRCULATION & ASSETS
            </h4>
            <ul className="space-y-2 text-ink/80 font-bold">
              <li>
                <Link href="/catalog" className="hover:text-ink hover:underline">
                  Course Textbooks & Reserves
                </Link>
              </li>
              <li>
                <Link href="/catalog" className="hover:text-ink hover:underline">
                  Computer Science & Algorithms
                </Link>
              </li>
              <li>
                <Link href="/catalog" className="hover:text-ink hover:underline">
                  Mathematics & Physics Monograph Vault
                </Link>
              </li>
              <li>
                <Link href="/catalog" className="hover:text-ink hover:underline">
                  Artificial Intelligence & Data Science
                </Link>
              </li>
              <li>
                <Link href="/shelf-hall" className="hover:text-ink hover:underline">
                  3D Interactive Shelf Hall
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 3: Staff & Administration */}
          <div className="space-y-3 font-mono text-xs">
            <h4 className="font-display text-sm font-black text-ink uppercase tracking-wider border-b-2 border-ink pb-2">
              STAFF & ADMINISTRATION
            </h4>
            <ul className="space-y-2 text-ink/80 font-bold">
              <li>
                <Link href="/admin" className="hover:text-ink hover:underline">
                  Curator Metadata Management
                </Link>
              </li>
              <li>
                <Link href="/admin" className="hover:text-ink hover:underline">
                  Librarian Active Loan Desk
                </Link>
              </li>
              <li>
                <Link href="/admin" className="hover:text-ink hover:underline">
                  Administrative Fine Waiver Portal
                </Link>
              </li>
              <li>
                <Link href="/admin" className="hover:text-ink hover:underline">
                  Spectral Co-Borrow Graph Optimizer
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  Spring Boot REST API Docs
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 4: Connect & Support */}
          <div className="space-y-3 font-mono text-xs">
            <h4 className="font-display text-sm font-black text-ink uppercase tracking-wider border-b-2 border-ink pb-2">
              CONNECT & HELP
            </h4>
            <ul className="space-y-2 text-ink/80 font-bold">
              <li>
                <Link href="/dashboard" className="hover:text-ink hover:underline">
                  My Borrower Dashboard
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  Capstone Urgency Priority Policy
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  System Status & STOMP Monitor
                </Link>
              </li>
              <li>
                <Link href="/" className="hover:text-ink hover:underline">
                  Security & RBAC Controls
                </Link>
              </li>
              <li>
                <a href="https://github.com" target="_blank" rel="noopener noreferrer" className="hover:text-ink hover:underline flex items-center gap-1">
                  <Github className="w-3.5 h-3.5" />
                  <span>GitHub Repository</span>
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Regional & Language Controls Strip */}
        <div className="mt-12 pt-8 border-t-3 border-ink flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-4">
            <Link href="/" aria-label="LIBRARIX Home">
              <Logo size="sm" showTagline={false} />
            </Link>

            <div className="flex items-center space-x-2 font-mono text-xs font-bold">
              <button className="px-3 py-1 bg-paper-2 border-2 border-ink shadow-brutal-sm flex items-center space-x-1.5 hover:bg-highlight">
                <Globe className="w-3.5 h-3.5" />
                <span>ENGLISH (US)</span>
              </button>
              <button className="px-3 py-1 bg-paper-2 border-2 border-ink shadow-brutal-sm flex items-center space-x-1.5 hover:bg-highlight">
                <DollarSign className="w-3.5 h-3.5" />
                <span>USD - CAMPUS CREDIT</span>
              </button>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <Sticker color="mint" rotate="-2deg">
              ⚡ STOMP STATUS: READY
            </Sticker>
            <Sticker color="sky" rotate="2deg">
              🛡️ SPRING BOOT 3.2
            </Sticker>
          </div>
        </div>

        {/* Legal & Copyright Bar */}
        <div className="mt-8 pt-6 border-t-2 border-ink/30 text-center font-mono text-[11px] text-ink/70 space-y-2">
          <div className="flex flex-wrap justify-center gap-4 font-bold">
            <a href="#" className="hover:underline">Conditions of Use</a>
            <span>•</span>
            <a href="#" className="hover:underline">Privacy Notice</a>
            <span>•</span>
            <a href="#" className="hover:underline">Campus Resource Guidelines</a>
            <span>•</span>
            <a href="#" className="hover:underline">Cookies & Ad Choices</a>
          </div>
          <p>© 2026 LIBRARIX Inc. or its affiliates. All rights reserved. Neobrutalist + 3D Immersive Layer.</p>
        </div>
      </div>
    </footer>
  );
}
