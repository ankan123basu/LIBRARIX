'use client';

import React, { useEffect, useState } from 'react';
import { Bell, CheckCircle, Clock, AlertTriangle, X } from 'lucide-react';
import { Notification } from '@/lib/types';
import { connectWebSocket, disconnectWebSocket } from '@/lib/websocket';
import { getStoredUser } from '@/lib/api';

export default function LiveNotificationToast() {
  const [activeToast, setActiveToast] = useState<Notification | null>(null);

  useEffect(() => {
    const user = getStoredUser();
    if (!user || !user.id) return;

    const client = connectWebSocket(user.id, (notification) => {
      setActiveToast(notification);
      // Auto dismiss after 8 seconds
      setTimeout(() => {
        setActiveToast(null);
      }, 8000);
    });

    return () => {
      disconnectWebSocket();
    };
  }, []);

  if (!activeToast) return null;

  return (
    <div className="fixed top-5 right-5 z-50 max-w-md w-full animate-bounce-in">
      <div className="glass-card p-4 rounded-xl border border-indigo-500/50 glow-indigo shadow-2xl flex items-start space-x-3 bg-gray-900/90 text-white">
        <div className="p-2 rounded-lg bg-indigo-500/20 text-indigo-400 mt-0.5">
          <Bell className="w-5 h-5 animate-pulse" />
        </div>
        <div className="flex-1">
          <h4 className="font-semibold text-indigo-300 text-sm">{activeToast.title}</h4>
          <p className="text-xs text-gray-300 mt-1 leading-relaxed">{activeToast.message}</p>
        </div>
        <button
          onClick={() => setActiveToast(null)}
          className="text-gray-400 hover:text-white p-1 rounded-lg transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
