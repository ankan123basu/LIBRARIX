'use client';

import React, { useRef, useState, useEffect } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Float, OrbitControls, Html } from '@react-three/drei';
import * as THREE from 'three';

function BookMesh() {
  const groupRef = useRef<THREE.Group>(null);
  const coverRef = useRef<THREE.Group>(null);
  const [hovered, setHovered] = useState(false);

  useFrame((state, delta) => {
    if (groupRef.current) {
      if (!hovered) {
        groupRef.current.rotation.y += delta * 0.4;
        groupRef.current.rotation.x = Math.sin(state.clock.elapsedTime * 0.8) * 0.08;
      } else {
        groupRef.current.rotation.y = THREE.MathUtils.lerp(groupRef.current.rotation.y, 0.2, 0.1);
        groupRef.current.rotation.x = THREE.MathUtils.lerp(groupRef.current.rotation.x, -0.1, 0.1);
      }
    }

    // Smooth cover hinge open on hover
    if (coverRef.current) {
      const targetCoverAngle = hovered ? -0.7 : 0;
      coverRef.current.rotation.y = THREE.MathUtils.lerp(coverRef.current.rotation.y, targetCoverAngle, 0.12);
    }
  });

  return (
    <group
      ref={groupRef}
      onPointerOver={() => setHovered(true)}
      onPointerOut={() => setHovered(false)}
      scale={hovered ? [1.1, 1.1, 1.1] : [1, 1, 1]}
      position={[0, 0, 0]}
    >
      {/* Back Cover - Blush Pastel #FFA8CE */}
      <mesh position={[0, 0, -0.26]}>
        <boxGeometry args={[1.9, 2.6, 0.06]} />
        <meshStandardMaterial color="#FFA8CE" roughness={0.3} metalness={0.1} />
      </mesh>
      <lineSegments position={[0, 0, -0.26]}>
        <edgesGeometry args={[new THREE.BoxGeometry(1.91, 2.61, 0.065)]} />
        <lineBasicMaterial color="#0B0B0B" linewidth={3} />
      </lineSegments>

      {/* Pages Block - Warm Paper #F7F5EF with ridged depth */}
      <mesh position={[0.08, 0, 0]}>
        <boxGeometry args={[1.7, 2.48, 0.46]} />
        <meshStandardMaterial color="#F7F5EF" roughness={0.8} />
      </mesh>
      <lineSegments position={[0.08, 0, 0]}>
        <edgesGeometry args={[new THREE.BoxGeometry(1.71, 2.49, 0.465)]} />
        <lineBasicMaterial color="#0B0B0B" linewidth={2} />
      </lineSegments>

      {/* Book Spine - Inked Heavy Dark #0B0B0B */}
      <mesh position={[-0.92, 0, 0]}>
        <boxGeometry args={[0.08, 2.62, 0.58]} />
        <meshStandardMaterial color="#0B0B0B" roughness={0.4} />
      </mesh>

      {/* Spine Label Text */}
      <Html transform position={[-0.97, 0, 0]} rotation={[0, -Math.PI / 2, Math.PI / 2]} scale={0.08} center distanceFactor={10}>
        <div className="font-mono font-black text-highlight select-none pointer-events-none text-xs tracking-widest uppercase">
          LIBRARIX SYSTEM v1.0
        </div>
      </Html>

      {/* Front Cover Hinge Group (Pivots from left spine -0.92) */}
      <group ref={coverRef} position={[-0.92, 0, 0.26]}>
        <group position={[0.92, 0, 0]}>
          {/* Front Cover Mesh - Pastel Yellow #FFE14D */}
          <mesh>
            <boxGeometry args={[1.9, 2.6, 0.06]} />
            <meshStandardMaterial color="#FFE14D" roughness={0.3} metalness={0.1} />
          </mesh>
          <lineSegments>
            <edgesGeometry args={[new THREE.BoxGeometry(1.91, 2.61, 0.065)]} />
            <lineBasicMaterial color="#0B0B0B" linewidth={3} />
          </lineSegments>

          {/* Front Title Foil Stamp Box */}
          <mesh position={[0, 0.4, 0.035]}>
            <boxGeometry args={[1.5, 0.8, 0.01]} />
            <meshStandardMaterial color="#0B0B0B" />
          </mesh>

          <Html transform position={[0, 0.4, 0.045]} scale={0.12} center distanceFactor={10}>
            <div className="font-heading font-black text-highlight select-none pointer-events-none tracking-tight text-center text-3xl uppercase">
              LIBRARIX
            </div>
          </Html>

          {/* Dewey Call Number Stamp */}
          <mesh position={[0, -0.6, 0.035]}>
            <boxGeometry args={[1.3, 0.35, 0.01]} />
            <meshStandardMaterial color="#0B0B0B" />
          </mesh>
          <Html transform position={[0, -0.6, 0.045]} scale={0.08} center distanceFactor={10}>
            <div className="font-mono font-black text-mint select-none pointer-events-none text-xs uppercase tracking-wider">
              005.133 LIB • ACTIVE
            </div>
          </Html>
        </group>
      </group>

      {/* Ribbon Bookmark Hanging Down */}
      <mesh position={[0.2, -1.5, 0]} rotation={[0, 0, -0.2]}>
        <boxGeometry args={[0.15, 0.6, 0.01]} />
        <meshStandardMaterial color="#E53935" />
      </mesh>

      {/* Floating Neobrutalist Sticker Tag on Hover */}
      {hovered && (
        <Html position={[0, -1.7, 0.2]} center>
          <div className="bg-ink text-highlight font-mono text-xs font-black px-3 py-1 border-2 border-paper shadow-brutal-sm rounded-none whitespace-nowrap uppercase">
            ⚡ INTERACTIVE COVER • HOVER OPEN
          </div>
        </Html>
      )}
    </group>
  );
}

export default function Hero3DBook() {
  const [reducedMotion, setReducedMotion] = useState(false);
  const [hasWebGL, setHasWebGL] = useState(true);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
      setReducedMotion(mediaQuery.matches);

      try {
        const canvas = document.createElement('canvas');
        const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
        if (!gl) setHasWebGL(false);
      } catch (e) {
        setHasWebGL(false);
      }
    }
  }, []);

  if (reducedMotion || !hasWebGL) {
    return (
      <div className="w-full h-[320px] sm:h-[400px] flex items-center justify-center p-6">
        <div className="w-64 h-80 bg-highlight border-3 border-ink shadow-brutal p-6 flex flex-col justify-between transform -rotate-3 hover:rotate-0 transition-transform">
          <div className="border-b-3 border-ink pb-3">
            <span className="bg-ink text-paper font-mono text-[10px] font-bold px-2 py-0.5">
              005.133 LIB
            </span>
            <h3 className="font-heading text-2xl font-black text-ink mt-2">LIBRARIX</h3>
            <p className="font-mono text-xs text-ink/80 font-bold">DIGITAL ARCHIVE</p>
          </div>
          <div className="space-y-2 font-mono text-xs">
            <div className="bg-paper p-2 border-2 border-ink">STATUS: ACTIVE</div>
            <div className="bg-mint p-2 border-2 border-ink">TYPE: HARDWARE / BOOK</div>
          </div>
          <div className="text-right font-mono text-[10px] font-bold text-ink">
            ★ NEOBRUTALIST 2D FALLBACK
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full h-[320px] sm:h-[400px] relative">
      <Canvas
        camera={{ position: [0, 0, 4.6], fov: 45 }}
        gl={{ alpha: true, antialias: true, powerPreference: 'high-performance' }}
      >
        <ambientLight intensity={1.3} />
        <directionalLight position={[5, 8, 5]} intensity={1.8} castShadow />
        <directionalLight position={[-5, -2, -3]} intensity={0.6} color="#A4D7E1" />
        
        <Float speed={2.5} rotationIntensity={0.3} floatIntensity={0.5}>
          <BookMesh />
        </Float>

        <OrbitControls
          enableZoom={false}
          enablePan={false}
          maxPolarAngle={Math.PI / 1.6}
          minPolarAngle={Math.PI / 2.5}
        />
      </Canvas>
    </div>
  );
}
