'use client';

import React, { useRef, useState } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import * as THREE from 'three';
import { ShelfPositionDTO } from '@/lib/types';
import StatusBadge from '@/components/ui/StatusBadge';
import BrutalButton from '@/components/ui/BrutalButton';

interface Shelf3DSceneProps {
  layout: ShelfPositionDTO[];
  onSelectBook: (book: ShelfPositionDTO) => void;
}

function BookMesh({
  book,
  position,
  onSelect,
  onHover,
}: {
  book: ShelfPositionDTO;
  position: [number, number, number];
  onSelect: (book: ShelfPositionDTO) => void;
  onHover: (book: ShelfPositionDTO | null) => void;
}) {
  const visualRef = useRef<THREE.Group>(null);
  const [hovered, setHovered] = useState(false);

  // State-to-color map
  const getColorHex = () => {
    if (book.spineColorHex) return book.spineColorHex;
    if (book.status === 'AVAILABLE') return '#FFE14D'; // --highlight
    if (book.status === 'RESERVED') return '#FFA8CE';  // --blush
    if (book.status === 'BORROWED') return '#7DE8A8';  // --mint
    return '#A4D7E1'; // --sky
  };

  useFrame(() => {
    if (visualRef.current) {
      const targetZ = hovered ? 0.32 : 0;
      const targetRotY = hovered ? -0.15 : 0;
      visualRef.current.position.z = THREE.MathUtils.lerp(visualRef.current.position.z, targetZ, 0.2);
      visualRef.current.rotation.y = THREE.MathUtils.lerp(visualRef.current.rotation.y, targetRotY, 0.2);
    }
  });

  return (
    <group
      position={position}
      onPointerOver={() => {
        setHovered(true);
        onHover(book);
        document.body.style.cursor = 'pointer';
      }}
      onPointerOut={() => {
        setHovered(false);
        onHover(null);
        document.body.style.cursor = 'auto';
      }}
      onClick={() => {
        onSelect(book);
      }}
    >
      {/* Non-overlapping raycast hit box */}
      <mesh visible={false} position={[0, 0, 0]}>
        <boxGeometry args={[0.24, 0.50, 0.32]} />
      </mesh>

      {/* Visual Animated Mesh Group */}
      <group ref={visualRef}>
        {/* Book Spine Box */}
        <mesh position={[0, 0, 0]}>
          <boxGeometry args={[0.26, 0.52, 0.34]} />
          <meshStandardMaterial color={getColorHex()} roughness={0.3} metalness={0.1} />
        </mesh>

        {/* Heavy Ink Outline */}
        <lineSegments position={[0, 0, 0]}>
          <edgesGeometry args={[new THREE.BoxGeometry(0.265, 0.525, 0.345)]} />
          <lineBasicMaterial color="#0B0B0B" linewidth={3} />
        </lineSegments>

        {/* Pages Block Ridge */}
        <mesh position={[0.02, 0, -0.01]}>
          <boxGeometry args={[0.22, 0.49, 0.32]} />
          <meshStandardMaterial color="#F7F5EF" roughness={0.9} />
        </mesh>

        {/* Native 3D Spine Library Label Sticker */}
        <mesh position={[0, -0.04, 0.172]}>
          <planeGeometry args={[0.16, 0.34]} />
          <meshStandardMaterial color="#FFFDF0" roughness={0.5} />
        </mesh>
        {/* Spine Accent Callout Band */}
        <mesh position={[0, 0.08, 0.174]}>
          <planeGeometry args={[0.14, 0.06]} />
          <meshStandardMaterial color="#0B0B0B" />
        </mesh>
        <mesh position={[0, -0.12, 0.174]}>
          <planeGeometry args={[0.12, 0.02]} />
          <meshStandardMaterial color="#0B0B0B" />
        </mesh>
      </group>
    </group>
  );
}

function ShelfPlank({ position, width = 3.6 }: { position: [number, number, number]; width?: number }) {
  return (
    <group position={position}>
      {/* Wood / Paper Plank */}
      <mesh position={[0, 0, 0]}>
        <boxGeometry args={[width, 0.08, 0.55]} />
        <meshStandardMaterial color="#F7F5EF" roughness={0.7} />
      </mesh>
      {/* Ink Outline */}
      <lineSegments position={[0, 0, 0]}>
        <edgesGeometry args={[new THREE.BoxGeometry(width + 0.01, 0.085, 0.555)]} />
        <lineBasicMaterial color="#0B0B0B" linewidth={3} />
      </lineSegments>
    </group>
  );
}

function ShelfStructure({ position }: { position: [number, number, number] }) {
  return (
    <group position={position}>
      {/* Vertical Support Posts */}
      <mesh position={[-1.8, 1.4, 0]}>
        <boxGeometry args={[0.1, 2.8, 0.55]} />
        <meshStandardMaterial color="#0B0B0B" roughness={0.5} />
      </mesh>
      <mesh position={[1.8, 1.4, 0]}>
        <boxGeometry args={[0.1, 2.8, 0.55]} />
        <meshStandardMaterial color="#0B0B0B" roughness={0.5} />
      </mesh>

      {/* 3 Planks: Level 1 (0.28m), Level 2 (1.38m), Level 3 (2.28m) */}
      <ShelfPlank position={[0, 0.28, 0]} width={3.7} />
      <ShelfPlank position={[0, 1.38, 0]} width={3.7} />
      <ShelfPlank position={[0, 2.28, 0]} width={3.7} />
    </group>
  );
}

export default function Shelf3DScene({ layout, onSelectBook }: Shelf3DSceneProps) {
  const [hoveredBook, setHoveredBook] = useState<ShelfPositionDTO | null>(null);

  // Fit up to 8 books per shelf row to stay bounded inside the 3.6m shelf posts (-1.35m to +1.35m)
  const itemsPerLevel = 8;
  const displayItems = layout.slice(0, 24); // Show top 24 active items per aisle view

  const formattedBooks = displayItems.map((book, idx) => {
    const levelIndex = idx % 3; // 0=Bottom (Level 1), 1=Middle (Level 2 Eye-Level), 2=Top (Level 3)
    const slotIndex = Math.floor(idx / 3); // 0 to 7 across row

    const posX = -1.3 + slotIndex * 0.37;
    const posY = levelIndex === 0 ? 0.56 : levelIndex === 1 ? 1.66 : 2.56;
    const posZ = 0;

    return {
      book,
      position: [posX, posY, posZ] as [number, number, number],
    };
  });

  return (
    <div className="w-full h-[650px] border-3 border-ink bg-paper shadow-brutal relative overflow-hidden">
      {/* Neobrutalist HUD Overlay */}
      <div className="absolute top-4 left-4 z-20 border-3 border-ink bg-paper-2 p-3.5 shadow-brutal-sm font-mono text-xs space-y-2 max-w-sm">
        <div className="flex items-center justify-between gap-2 border-b-2 border-ink pb-1.5">
          <span className="font-display text-sm font-black text-ink uppercase">
            3D SHELF HALL CORRIDOR
          </span>
          <span className="bg-ink text-highlight font-bold text-[10px] px-2 py-0.5 border border-ink">
            {layout.length} ASSETS
          </span>
        </div>

        {hoveredBook ? (
          <div className="space-y-1.5 bg-paper p-2 border-2 border-ink">
            <div className="flex items-center justify-between gap-1">
              <span className="font-bold text-[10px] bg-ink text-paper px-1.5 py-0.5">
                BARCODE #{hoveredBook.barcode}
              </span>
              <StatusBadge status={hoveredBook.status} />
            </div>
            <h4 className="font-display text-xs font-black text-ink line-clamp-1">
              {hoveredBook.title}
            </h4>
            <p className="text-[10px] text-ink/80">{hoveredBook.authorOrBrand}</p>
            <p className="text-[9px] text-stamp font-bold">CLICK ITEM TO BORROW / WAITLIST</p>
          </div>
        ) : (
          <p className="text-[11px] text-ink/80 leading-normal">
            Hover over any book to inspect call numbers • Click item to open checkout card
          </p>
        )}

        <div className="flex flex-wrap gap-2 pt-1 text-[10px] font-bold">
          <span className="bg-highlight text-ink px-1.5 py-0.5 border border-ink">Available</span>
          <span className="bg-blush text-ink px-1.5 py-0.5 border border-ink">Waitlist</span>
          <span className="bg-mint text-ink px-1.5 py-0.5 border border-ink">Checked Out</span>
        </div>
      </div>

      <Canvas camera={{ position: [0, 1.4, 4.0], fov: 45 }} dpr={[1, 1.75]}>
        <ambientLight intensity={1.3} />
        <directionalLight position={[5, 10, 5]} intensity={1.8} castShadow />
        <directionalLight position={[-5, -2, -3]} intensity={0.6} color="#A4D7E1" />

        {/* 3D Shelf Architecture */}
        <ShelfStructure position={[0, 0, 0]} />

        {/* 3D Books bounded to shelf width */}
        {formattedBooks.map(({ book, position }, idx) => (
          <BookMesh
            key={book.resourceId || book.barcode || `shelf-book-${idx}`}
            book={book}
            position={position}
            onSelect={onSelectBook}
            onHover={setHoveredBook}
          />
        ))}

        {/* OrbitControls */}
        <OrbitControls
          target={[0, 1.4, 0]}
          maxPolarAngle={Math.PI / 1.8}
          minPolarAngle={Math.PI / 3.5}
          minDistance={1.8}
          maxDistance={5.2}
        />
      </Canvas>
    </div>
  );
}

