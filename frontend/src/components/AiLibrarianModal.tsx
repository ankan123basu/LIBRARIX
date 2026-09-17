'use client';

import React, { useState } from 'react';
import { Bot, Send, Sparkles, X, BookOpen, CheckCircle2 } from 'lucide-react';
import BrutalButton from '@/components/ui/BrutalButton';
import Sticker from '@/components/ui/Sticker';
import { fetchWithAuth } from '@/lib/api';

interface Message {
  sender: 'user' | 'assistant';
  text: string;
}

export default function AiLibrarianModal() {
  const [isOpen, setIsOpen] = useState(false);
  const [question, setQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    {
      sender: 'assistant',
      text: 'Hello! I am your AI Campus Librarian. Ask me anything about course textbooks, waitlist priorities, circulation rules, or recommendations!',
    },
  ]);

  const handleAsk = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim() || loading) return;

    const userQ = question.trim();
    setQuestion('');
    setMessages((prev) => [...prev, { sender: 'user', text: userQ }]);
    setLoading(true);

    try {
      const data = await fetchWithAuth('/ai/ask', {
        method: 'POST',
        body: JSON.stringify({ question: userQ }),
      });
      const answer = data?.answer || data?.response || 'I found relevant catalog books for your query in the LIBRARIX archive!';
      setMessages((prev) => [...prev, { sender: 'assistant', text: answer }]);
    } catch (err: any) {
      // Local grounding fallback if backend unauthenticated or offline
      setMessages((prev) => [
        ...prev,
        {
          sender: 'assistant',
          text: `[LIBRARIX Grounding Context]: Standard loan period is 7 days with a 24-hour grace period. Dynamic priority queue scores grant capstone students urgency boosts! Search our catalogue for '${userQ}'.`,
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* Floating Trigger Widget at Bottom Right */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 z-50 px-4 py-3 border-3 border-ink bg-sky font-mono text-xs font-black text-ink flex items-center space-x-2 shadow-brutal hover:bg-[#8CD3E1] active:translate-x-[2px] active:translate-y-[2px]"
      >
        <Bot className="w-5 h-5 text-ink animate-bounce" />
        <span>ASK AI LIBRARIAN</span>
        <Sticker color="highlight" rotate="2deg">
          AI RAG
        </Sticker>
      </button>

      {/* Modal Dialog */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/70 backdrop-blur-xs p-4">
          <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal-lg max-w-2xl w-full h-[600px] flex flex-col justify-between relative animate-fade-in">
            {/* Header */}
            <div className="border-b-3 border-ink pb-4 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 border-3 border-ink bg-sky flex items-center justify-center shadow-brutal-sm">
                  <Bot className="w-6 h-6 text-ink" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-display text-xl font-black text-ink uppercase">
                      AI CAMPUS LIBRARIAN
                    </h3>
                    <Sticker color="mint" rotate="-2deg">
                      LIVE RAG
                    </Sticker>
                  </div>
                  <p className="font-mono text-xs text-ink/80">
                    Context-grounded assistant for textbooks, queue scores & policy rules
                  </p>
                </div>
              </div>
              <button
                onClick={() => setIsOpen(false)}
                className="p-1.5 border-3 border-ink bg-paper hover:bg-blush"
              >
                <X className="w-5 h-5 text-ink" />
              </button>
            </div>

            {/* Chat Messages Log */}
            <div className="flex-1 overflow-y-auto my-4 space-y-4 pr-2 font-mono text-xs">
              {messages.map((msg, idx) => (
                <div
                  key={idx}
                  className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[85%] border-3 border-ink p-4 shadow-brutal-sm font-bold leading-relaxed ${
                      msg.sender === 'user'
                        ? 'bg-highlight text-ink'
                        : 'bg-paper text-ink space-y-2'
                    }`}
                  >
                    <div className="flex items-center gap-1.5 text-[10px] font-black uppercase border-b border-ink/30 pb-1 mb-1">
                      {msg.sender === 'user' ? (
                        <span>YOU</span>
                      ) : (
                        <span className="flex items-center gap-1 text-stamp">
                          <Sparkles className="w-3 h-3" />
                          <span>AI LIBRARIAN</span>
                        </span>
                      )}
                    </div>
                    <p className="whitespace-pre-wrap">{msg.text}</p>
                  </div>
                </div>
              ))}

              {loading && (
                <div className="flex justify-start">
                  <div className="border-3 border-ink bg-paper p-4 shadow-brutal-sm font-mono text-xs font-bold text-ink animate-pulse flex items-center space-x-2">
                    <Sparkles className="w-4 h-4 text-stamp animate-spin" />
                    <span>CONSULTING VECTOR CATALOGUE & LLM CONTEXT...</span>
                  </div>
                </div>
              )}
            </div>

            {/* Input Form */}
            <form onSubmit={handleAsk} className="border-t-3 border-ink pt-4 flex gap-3">
              <input
                type="text"
                placeholder="Ask about textbooks, course codes, fine rules, or waitlist queues..."
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                className="flex-1 bg-paper border-3 border-ink px-4 py-2.5 font-mono text-xs text-ink font-bold focus:outline-none focus:ring-3 focus:ring-ink"
              />
              <BrutalButton variant="primary" type="submit" disabled={loading}>
                <Send className="w-4 h-4" />
                <span>ASK</span>
              </BrutalButton>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
