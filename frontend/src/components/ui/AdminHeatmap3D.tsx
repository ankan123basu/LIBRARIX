'use client';

import React, { useState, useEffect } from 'react';
import { Canvas } from '@react-three/fiber';
import { Float, Html, OrbitControls } from '@react-three/drei';
import * as THREE from 'three';
import { ShelfPositionDTO } from '@/lib/types';

interface AdminHeatmap3DProps {
  shelfLayout: ShelfPositionDTO[];
}

interface ZoneMetrics {
  aisleId: string;
  totalItems: number;
  availableCount: number;
  borrowedCount: number;
  occupancyRate: number;
}

function PillarMesh({
  metrics,
  position,
  color,
}: {
  metrics: ZoneMetrics;
  position: [number, number, number];
  color: string;
}) {
  const [hovered, setHovered] = useState(false);
  const height = Math.max(0.6, metrics.totalItems * 0.4);

  return (
    <group
      position={[position[0], height / 2, position[2]]}
      onPointerOver={() => setHovered(true)}
      onPointerOut={() => setHovered(false)}
      scale={hovered ? [1.1, 1.05, 1.1] : [1, 1, 1]}
    >
      {/* Extruded Pillar */}
      <mesh>
        <boxGeometry args={[1.2, height, 1.2]} />
        <meshStandardMaterial color={color} roughness={0.3} />
      </mesh>

      {/* Ink Edges */}
      <lineSegments>
        <edgesGeometry args={[new THREE.BoxGeometry(1.2, height, 1.2)]} />
        <lineBasicMaterial color="#0B0B0B" linewidth={3} />
      </lineSegments>

      {/* Label on Top */}
      <Html
        transform
        position={[0, height / 2 + 0.1, 0]}
        rotation={[-Math.PI / 2, 0, 0]}
        scale={0.25}
        center
      >
        <div className="font-heading font-black text-ink select-none pointer-events-none text-xl">
          {metrics.aisleId}
        </div>
      </Html>

      {/* Hover Info Tooltip */}
      {hovered && (
        <Html position={[0, height / 2 + 0.8, 0]} center>
          <div className="bg-paper border-3 border-ink p-3 shadow-brutal text-ink font-mono text-xs w-48 pointer-events-none z-50">
            <div className="bg-ink text-paper px-2 py-0.5 font-bold mb-2 flex justify-between">
              <span>AISLE {metrics.aisleId}</span>
              <span>{metrics.occupancyRate.toFixed(0)}%</span>
            </div>
            <div className="space-y-1">
              <p>Total Assets: <strong>{metrics.totalItems}</strong></p>
              <p className="text-mint font-bold bg-ink px-1 inline-block">
                Available: {metrics.availableCount}
              </p>
              <p className="text-stamp font-bold">
                Borrowed: {metrics.borrowedCount}
              </p>
            </div>
          </div>
        </Html>
      )}
    </group>
  );
}

export default function AdminHeatmap3D({ shelfLayout }: AdminHeatmap3DProps) {
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

  // Aggregate layout into Aisle Metrics
  const aisles = Array.from(new Set(shelfLayout.map((s) => s.aisleId || 'AISLE-1')));
  if (aisles.length === 0) aisles.push('A1', 'A2', 'B1', 'B2');

  const zoneMetrics: ZoneMetrics[] = aisles.map((aisleId) => {
    const items = shelfLayout.filter((s) => (s.aisleId || 'AISLE-1') === aisleId);
    const total = items.length || 3;
    const available = items.filter((s) => s.status === 'AVAILABLE').length || 2;
    const borrowed = total - available;
    return {
      aisleId,
      totalItems: total,
      availableCount: available,
      borrowedCount: borrowed,
      occupancyRate: total > 0 ? (borrowed / total) * 100 : 40,
    };
  });

  const colors = ['#FFE14D', '#7DE8A8', '#FFA8CE', '#A4D7E1', '#D1C4E9'];

  return (
    <div className="bg-paper border-3 border-ink shadow-brutal p-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between border-b-3 border-ink pb-4 mb-4 gap-2">
        <div>
          <span className="bg-lilac text-ink border border-ink font-mono text-[10px] font-bold px-2 py-0.5 uppercase">
            3D OCCUPANCY HEATMAP
          </span>
          <h3 className="font-heading text-xl font-black text-ink">AISLE ACTIVITY & DENSITY</h3>
        </div>

        {hasWebGL && !reducedMotion && (
          <button
            onClick={() => setViewMode(viewMode === '3d' ? '2d' : '3d')}
            className="px-3 py-1.5 bg-highlight border-2 border-ink shadow-brutal-sm font-mono text-xs font-bold hover:bg-mint transition-colors"
          >
            {viewMode === '3d' ? 'SWITCH TO 2D METRICS' : 'SWITCH TO 3D HEATMAP'}
          </button>
        )}
      </div>

      {viewMode === '3d' && hasWebGL && !reducedMotion ? (
        <div className="w-full h-[320px] bg-paper-2 border-2 border-ink relative overflow-hidden">
          <div className="absolute top-2 left-2 z-10 bg-ink text-paper font-mono text-[10px] px-2 py-1 border border-paper">
            ROTATE 3D OCCUPANCY GRID
          </div>
          <Canvas camera={{ position: [4, 5, 6], fov: 45 }}>
            <ambientLight intensity={1.2} />
            <directionalLight position={[6, 10, 6]} intensity={1.6} castShadow />
            
            {/* Ground Plane Grid */}
            <mesh rotation={[-Math.PI / 2, 0, 0]} position={[0, 0, 0]}>
              <planeGeometry args={[10, 10]} />
              <meshStandardMaterial color="#F7F5EF" roughness={0.9} />
            </mesh>
            <gridHelper args={[10, 10, '#0B0B0B', '#0B0B0B']} position={[0, 0.01, 0]} />

            {/* Pillars for each zone */}
            {zoneMetrics.map((zone, idx) => {
              const row = Math.floor(idx / 2);
              const col = idx % 2;
              const posX = col * 2.5 - 1.25;
              const posZ = row * 2.5 - 1.25;
              const color = colors[idx % colors.length];

              return (
                <PillarMesh
                  key={zone.aisleId}
                  metrics={zone}
                  position={[posX, 0, posZ]}
                  color={color}
                />
              );
            })}

            <OrbitControls enableZoom={false} maxPolarAngle={Math.PI / 2.2} minPolarAngle={Math.PI / 4} />
          </Canvas>
        </div>
      ) : (
        /* 2D Metrics Table Fallback */
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          {zoneMetrics.map((zone, idx) => (
            <div
              key={zone.aisleId}
              className="p-4 border-3 border-ink bg-paper-2 shadow-brutal-sm flex flex-col justify-between"
              style={{ backgroundColor: colors[idx % colors.length] + '33' }}
            >
              <div className="border-b-2 border-ink pb-2 mb-2 flex justify-between items-center">
                <span className="font-heading text-lg font-black text-ink">{zone.aisleId}</span>
                <span className="font-mono text-xs font-bold bg-ink text-paper px-2 py-0.5">
                  {zone.occupancyRate.toFixed(0)}% LOAD
                </span>
              </div>
              <div className="space-y-1 font-mono text-xs text-ink">
                <p>Total Shelf Assets: <strong>{zone.totalItems}</strong></p>
                <p>Available: <strong>{zone.availableCount}</strong></p>
                <p>Borrowed: <strong>{zone.borrowedCount}</strong></p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
