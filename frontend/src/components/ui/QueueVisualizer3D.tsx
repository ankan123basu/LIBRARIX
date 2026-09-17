'use client';

import React, { useState, useEffect } from 'react';
import { Canvas } from '@react-three/fiber';
import { Float, Html, OrbitControls } from '@react-three/drei';
import * as THREE from 'three';
import { QueueEntryDTO } from '@/lib/types';
import StatusBadge from '@/components/ui/StatusBadge';

interface QueueVisualizer3DProps {
  queueEntries: QueueEntryDTO[];
}

function TokenMesh({
  entry,
  index,
  total,
}: {
  entry: QueueEntryDTO;
  index: number;
  total: number;
}) {
  const [hovered, setHovered] = useState(false);

  // Determine token color based on urgency
  let tokenColor = '#FFE14D'; // STANDARD - Pastel Yellow
  if (entry.urgencyLevel === 'HIGH') tokenColor = '#7DE8A8'; // Mint
  if (entry.urgencyLevel === 'CRITICAL') tokenColor = '#FFA8CE'; // Blush

  // Position stacked vertically or horizontally in 3D arc
  const spacing = 1.1;
  const startX = -((total - 1) * spacing) / 2;
  const posX = startX + index * spacing;
  const posY = Math.sin(index * 0.5) * 0.2;
  const posZ = -index * 0.15;

  return (
    <group
      position={[posX, posY, posZ]}
      onPointerOver={() => setHovered(true)}
      onPointerOut={() => setHovered(false)}
      scale={hovered ? [1.15, 1.15, 1.15] : [1, 1, 1]}
    >
      {/* 3D Ticket Token Box */}
      <mesh position={[0, 0, 0]}>
        <boxGeometry args={[0.95, 1.35, 0.12]} />
        <meshStandardMaterial color={tokenColor} roughness={0.3} />
      </mesh>

      {/* Ink Outline */}
      <lineSegments position={[0, 0, 0]}>
        <edgesGeometry args={[new THREE.BoxGeometry(0.95, 1.35, 0.12)]} />
        <lineBasicMaterial color="#0B0B0B" linewidth={3} />
      </lineSegments>

      {/* Position Badge in 3D */}
      <Html position={[0, 0, 0.08]} transform center scale={0.25}>
        <div className="bg-ink text-paper font-mono font-black text-xs px-2 py-1 select-none pointer-events-none border border-paper">
          #{entry.queuePosition}
        </div>
      </Html>

      {/* Hover Info Tag */}
      {hovered && (
        <Html position={[0, 1.1, 0.2]} center>
          <div className="bg-paper border-3 border-ink p-3 shadow-brutal text-ink font-mono text-xs w-48 pointer-events-none z-50">
            <div className="flex items-center justify-between border-b-2 border-ink pb-1 mb-1 font-bold">
              <span>POS #{entry.queuePosition}</span>
              <span className="bg-ink text-paper px-1.5 py-0.5 text-[10px]">
                {entry.urgencyLevel}
              </span>
            </div>
            <p className="font-bold truncate">{entry.userFullName}</p>
            <p className="text-[10px] text-ink/70">Score: {entry.calculatedPriorityScore.toFixed(1)}</p>
          </div>
        </Html>
      )}
    </group>
  );
}

export default function QueueVisualizer3D({ queueEntries }: QueueVisualizer3DProps) {
  const [viewMode, setViewMode] = useState<'3d' | '2d'>('3d');
  const [reducedMotion, setReducedMotion] = useState(false);
  const [hasWebGL, setHasWebGL] = useState(true);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
      setReducedMotion(mediaQuery.matches);
      if (mediaQuery.matches) setViewMode('2d');

      try {
        const canvas = document.createElement('canvas');
        const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
        if (!gl) {
          setHasWebGL(false);
          setViewMode('2d');
        }
      } catch (e) {
        setHasWebGL(false);
        setViewMode('2d');
      }
    }
  }, []);

  if (!queueEntries || queueEntries.length === 0) {
    return (
      <div className="p-6 bg-paper-2 border-3 border-ink text-center font-mono font-bold text-sm">
        NO ACTIVE WAITLIST QUEUE FOR THIS RESOURCE
      </div>
    );
  }

  return (
    <div className="bg-paper border-3 border-ink shadow-brutal p-4">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between border-b-3 border-ink pb-3 mb-4 gap-2">
        <div>
          <span className="bg-ink text-paper font-mono text-[10px] font-bold px-2 py-0.5 uppercase tracking-wider">
            STOMP WAITLIST VISUALIZER
          </span>
          <h4 className="font-heading text-lg font-black text-ink">QUEUE STACK ({queueEntries.length})</h4>
        </div>

        <div className="flex items-center space-x-2">
          {hasWebGL && !reducedMotion && (
            <button
              onClick={() => setViewMode(viewMode === '3d' ? '2d' : '3d')}
              className="px-3 py-1 bg-highlight border-2 border-ink shadow-brutal-sm font-mono text-xs font-bold hover:bg-lilac transition-colors"
            >
              {viewMode === '3d' ? 'SWITCH TO 2D LIST' : 'SWITCH TO 3D STACK'}
            </button>
          )}
        </div>
      </div>

      {/* 3D Canvas View */}
      {viewMode === '3d' && hasWebGL && !reducedMotion ? (
        <div className="w-full h-[280px] bg-paper-2 border-2 border-ink relative overflow-hidden">
          <div className="absolute top-2 left-2 z-10 bg-ink text-paper font-mono text-[10px] px-2 py-1 border border-paper">
            DRAG TO ROTATE 3D QUEUE
          </div>
          <Canvas camera={{ position: [0, 0, 5], fov: 50 }}>
            <ambientLight intensity={1.3} />
            <directionalLight position={[4, 6, 4]} intensity={1.5} />
            <Float speed={1.5} rotationIntensity={0.2} floatIntensity={0.3}>
              <group position={[0, -0.2, 0]}>
                {queueEntries.map((entry, idx) => (
                  <TokenMesh
                    key={entry.id || idx}
                    entry={entry}
                    index={idx}
                    total={queueEntries.length}
                  />
                ))}
              </group>
            </Float>
            <OrbitControls enableZoom={false} maxPolarAngle={Math.PI / 1.8} minPolarAngle={Math.PI / 3} />
          </Canvas>
        </div>
      ) : (
        /* 2D Fallback View */
        <div className="space-y-2">
          {queueEntries.map((entry) => (
            <div
              key={entry.id}
              className="p-3 border-2 border-ink bg-paper-2 flex items-center justify-between font-mono text-xs shadow-brutal-sm hover:translate-x-1 transition-transform"
            >
              <div className="flex items-center space-x-3">
                <span className="w-8 h-8 bg-ink text-paper flex items-center justify-center font-bold text-sm">
                  #{entry.queuePosition}
                </span>
                <div>
                  <p className="font-bold text-ink">{entry.userFullName}</p>
                  <p className="text-[10px] text-ink/70">
                    Priority Score: {entry.calculatedPriorityScore.toFixed(1)}
                  </p>
                </div>
              </div>
              <StatusBadge status={entry.urgencyLevel} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
