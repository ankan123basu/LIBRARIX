'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Mail, KeyRound, User as UserIcon, GraduationCap, AlertCircle, ArrowRight } from 'lucide-react';
import { setStoredToken } from '@/lib/api';
import { UserTier } from '@/lib/types';
import BrutalButton from '@/components/ui/BrutalButton';
import Logo from '@/components/brand/Logo';
import Sticker from '@/components/ui/Sticker';

export default function RegisterPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [userTier, setUserTier] = useState<UserTier>('REGULAR');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await fetch('http://localhost:8085/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, fullName, userTier }),
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.message || 'Registration failed');
      }

      setStoredToken(data.token);
      localStorage.setItem(
        'librarix_user',
        JSON.stringify({
          id: data.id,
          email: data.email,
          fullName: data.fullName,
          role: data.role,
          userTier: data.userTier,
        })
      );

      router.push('/catalog');
    } catch (err: any) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto my-8 border-3 border-ink bg-paper-2 shadow-brutal-lg grid grid-cols-1 md:grid-cols-2 overflow-hidden">
      {/* Left Split Panel */}
      <div className="border-b-3 md:border-b-0 md:border-r-3 border-ink bg-sky p-8 flex flex-col justify-between space-y-6">
        <Sticker color="highlight" rotate="-4deg" className="self-start">
          🎓 ACADEMIC MEMBER REGISTRATION
        </Sticker>

        <div className="space-y-4">
          <Logo size="lg" />
          <h2 className="font-display text-2xl sm:text-3xl font-black text-ink leading-tight uppercase">
            JOIN THE CAMPUS RESOURCE CIRCULATION NETWORK
          </h2>
          <p className="font-mono text-xs text-ink leading-relaxed">
            Gain priority scoring for capstone projects, hardware kits, and real-time waitlist updates.
          </p>
        </div>

        <div className="border-3 border-ink bg-paper p-4 font-mono text-xs space-y-1">
          <span className="font-bold text-ink uppercase block border-b-2 border-ink pb-1">
            TIER WEIGHT MULTIPLIERS:
          </span>
          <p>• Regular Student: 1.0x Base</p>
          <p>• Senior Capstone: 1.5x Priority Boost</p>
          <p>• Faculty/Researcher: 2.0x Priority Boost</p>
        </div>
      </div>

      {/* Right Split Panel (Form) */}
      <div className="p-8 space-y-6 bg-paper-2">
        <h3 className="font-display text-2xl font-black text-ink uppercase">
          CREATE ACCOUNT
        </h3>

        {error && (
          <div className="p-3.5 border-3 border-ink bg-stamp text-paper font-mono text-xs font-bold flex items-center space-x-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4 font-mono text-xs">
          <div className="space-y-1.5">
            <label className="block font-bold text-ink uppercase">Full Name</label>
            <div className="relative">
              <UserIcon className="w-4 h-4 text-ink absolute left-3.5 top-3" />
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="Jane Doe"
                className="w-full bg-paper border-3 border-ink pl-10 pr-4 py-2.5 font-bold text-ink placeholder:text-ink/40 focus:outline-none focus:ring-3 focus:ring-ink"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block font-bold text-ink uppercase">Campus Email</label>
            <div className="relative">
              <Mail className="w-4 h-4 text-ink absolute left-3.5 top-3" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="jane@librarix.edu"
                className="w-full bg-paper border-3 border-ink pl-10 pr-4 py-2.5 font-bold text-ink placeholder:text-ink/40 focus:outline-none focus:ring-3 focus:ring-ink"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block font-bold text-ink uppercase">Password</label>
            <div className="relative">
              <KeyRound className="w-4 h-4 text-ink absolute left-3.5 top-3" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Minimum 6 characters"
                className="w-full bg-paper border-3 border-ink pl-10 pr-4 py-2.5 font-bold text-ink placeholder:text-ink/40 focus:outline-none focus:ring-3 focus:ring-ink"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block font-bold text-ink uppercase">Academic Tier</label>
            <div className="relative">
              <GraduationCap className="w-4 h-4 text-ink absolute left-3.5 top-3" />
              <select
                value={userTier}
                onChange={(e) => setUserTier(e.target.value as UserTier)}
                className="w-full bg-paper border-3 border-ink pl-10 pr-4 py-2.5 font-bold text-ink focus:outline-none focus:ring-3 focus:ring-ink"
              >
                <option value="REGULAR">Regular Member (1.0x)</option>
                <option value="CAPSTONE">Senior Capstone Student (1.5x)</option>
                <option value="FACULTY">Faculty / Researcher (2.0x)</option>
              </select>
            </div>
          </div>

          <BrutalButton
            type="submit"
            variant="primary"
            size="lg"
            className="w-full mt-4"
            disabled={loading}
          >
            <span>{loading ? 'CREATING ACCOUNT...' : 'REGISTER ACCOUNT'}</span>
            <ArrowRight className="w-4 h-4" />
          </BrutalButton>
        </form>

        <div className="border-t-3 border-ink pt-4 text-center font-mono text-xs">
          <p className="text-ink">
            Already have an account?{' '}
            <Link href="/login" className="font-bold underline text-ink hover:bg-highlight px-1">
              Sign in here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
