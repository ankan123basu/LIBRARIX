'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { BookOpen, LayoutDashboard, LogOut, Layers, ShieldCheck } from 'lucide-react';
import { clearStoredToken, getStoredUser } from '@/lib/api';
import { User } from '@/lib/types';
import Logo from '@/components/brand/Logo';
import BrutalButton from '@/components/ui/BrutalButton';

export default function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    setUser(getStoredUser());
  }, [pathname]);

  const handleLogout = () => {
    clearStoredToken();
    setUser(null);
    router.push('/login');
  };

  return (
    <nav className="sticky top-0 z-40 bg-paper-2 border-b-3 border-ink shadow-brutal-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        {/* Fixed Home Logo Link */}
        <Link
          href="/"
          aria-label="LIBRARIX home"
          className="focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-ink focus-visible:ring-offset-2 p-1"
        >
          <Logo size="md" variant="default" showTagline={true} />
        </Link>

        {/* Navigation Links */}
        <div className="flex items-center space-x-3 sm:space-x-5">
          <Link
            href="/catalog"
            className={`px-3 py-1.5 border-3 border-ink text-xs sm:text-sm font-mono font-bold transition-all ${
              pathname === '/catalog'
                ? 'bg-highlight shadow-brutal-sm translate-x-[-2px] translate-y-[-2px]'
                : 'bg-paper-2 hover:bg-paper'
            }`}
          >
            <span className="flex items-center space-x-1.5">
              <BookOpen className="w-4 h-4" />
              <span>CATALOGUE</span>
            </span>
          </Link>

          <Link
            href="/dashboard"
            className={`px-3 py-1.5 border-3 border-ink text-xs sm:text-sm font-mono font-bold transition-all ${
              pathname === '/dashboard'
                ? 'bg-lilac shadow-brutal-sm translate-x-[-2px] translate-y-[-2px]'
                : 'bg-paper-2 hover:bg-paper'
            }`}
          >
            <span className="flex items-center space-x-1.5">
              <Layers className="w-4 h-4" />
              <span>DASHBOARD</span>
            </span>
          </Link>

          {user && (
            <Link
              href="/dashboard"
              className={`px-3 py-1.5 border-3 border-ink text-xs sm:text-sm font-mono font-bold transition-all ${
                pathname === '/dashboard' || pathname === '/me'
                  ? 'bg-mint shadow-brutal-sm translate-x-[-2px] translate-y-[-2px]'
                  : 'bg-paper-2 hover:bg-paper'
              }`}
            >
              <span className="flex items-center space-x-1.5">
                <LayoutDashboard className="w-4 h-4" />
                <span>MY DASHBOARD</span>
              </span>
            </Link>
          )}

          {user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_LIBRARIAN' || user.role === 'ROLE_CURATOR') && (
            <Link
              href="/admin"
              className={`px-3 py-1.5 border-3 border-ink text-xs sm:text-sm font-mono font-bold transition-all ${
                pathname === '/admin'
                  ? 'bg-blush shadow-brutal-sm translate-x-[-2px] translate-y-[-2px]'
                  : 'bg-paper-2 hover:bg-paper'
              }`}
            >
              <span className="flex items-center space-x-1.5">
                <ShieldCheck className="w-4 h-4 text-stamp" />
                <span>STAFF CONSOLE</span>
              </span>
            </Link>
          )}

          {user ? (
            <div className="flex items-center space-x-3 pl-3 border-l-3 border-ink">
              <div className="text-right hidden md:block font-mono">
                <p className="text-xs font-bold text-ink leading-tight">{user.fullName}</p>
                <span className={`inline-block text-[10px] px-2 py-0.5 border-2 border-ink font-bold uppercase mt-0.5 ${
                  user.role === 'ROLE_ADMIN'
                    ? 'bg-stamp text-paper'
                    : user.role === 'ROLE_LIBRARIAN' || user.role === 'ROLE_CURATOR'
                    ? 'bg-lilac text-ink'
                    : 'bg-mint text-ink'
                }`}>
                  {user.role.replace('ROLE_', '')}
                </span>
              </div>

              <button
                onClick={handleLogout}
                className="p-2 border-3 border-ink bg-stamp text-paper shadow-brutal-sm hover:bg-[#C92C21] active:translate-x-[2px] active:translate-y-[2px]"
                title="Sign out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center space-x-2">
              <Link href="/login">
                <BrutalButton variant="ghost" size="sm">
                  SIGN IN
                </BrutalButton>
              </Link>
              <Link href="/register">
                <BrutalButton variant="primary" size="sm">
                  REGISTER
                </BrutalButton>
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
