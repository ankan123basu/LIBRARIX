import type { Metadata } from 'next';
import { Archivo_Black, Space_Grotesk, JetBrains_Mono } from 'next/font/google';
import './globals.css';
import Navbar from '@/components/Navbar';
import Footer from '@/components/Footer';
import LiveNotificationToast from '@/components/LiveNotificationToast';

const archivoBlack = Archivo_Black({
  weight: '400',
  subsets: ['latin'],
  variable: '--font-archivo',
  display: 'swap',
});

const spaceGrotesk = Space_Grotesk({
  subsets: ['latin'],
  variable: '--font-space',
  display: 'swap',
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ['latin'],
  variable: '--font-mono',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'LIBRARIX — Intelligent Library & Resource Circulation',
  description: 'Neobrutalist Campus Resource Management, Priority Waitlist Queue, and 3D Shelf Hall.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html
      lang="en"
      className={`${archivoBlack.variable} ${spaceGrotesk.variable} ${jetbrainsMono.variable}`}
    >
      <body className="bg-paper text-ink font-body min-h-screen flex flex-col antialiased relative selection:bg-highlight selection:text-ink">
        {/* Dotted Grid Background */}
        <div
          className="fixed inset-0 pointer-events-none z-0 bg-dotted-grid"
          aria-hidden="true"
        />

        <div className="relative z-10 flex flex-col min-h-screen">
          <Navbar />
          <LiveNotificationToast />
          <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {children}
          </main>
          <Footer />
        </div>
      </body>
    </html>
  );
}
