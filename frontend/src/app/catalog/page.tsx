'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Search, Filter, RefreshCw, Layers, Book, Cpu, Wrench, DoorOpen, ArrowRight } from 'lucide-react';
import { ResourceDTO } from '@/lib/types';
import { fetchWithAuth } from '@/lib/api';
import ResourceCard from '@/components/ResourceCard';
import DrawerFilter from '@/components/ui/DrawerFilter';
import BrutalButton from '@/components/ui/BrutalButton';
import Sticker from '@/components/ui/Sticker';

const FALLBACK_CATALOG: ResourceDTO[] = [
  {
    id: 'res-101',
    barcode: '005.133 LIB',
    title: 'Designing Data-Intensive Applications',
    authorOrBrand: 'Martin Kleppmann',
    description: 'The definitive guide to distributed systems, storage engines, consensus protocols, and stream processing architectures.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg',
    type: 'BOOK',
    totalQuantity: 5,
    availableQuantity: 3,
    location: 'SHELF-A1',
    tags: ['programming', 'distributed-systems', 'architecture'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-102',
    barcode: '005.741 MON',
    title: 'MongoDB: The Definitive Guide (3rd Ed)',
    authorOrBrand: 'Shannon Bradshaw & Eoin Brazil',
    description: 'Comprehensive guide to document data modeling, aggregation pipelines, replica sets, and sharded cluster management.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9781491954249-L.jpg',
    type: 'BOOK',
    totalQuantity: 4,
    availableQuantity: 0,
    location: 'SHELF-A2',
    tags: ['database', 'nosql', 'mongodb'],
    status: 'BORROWED',
    activeQueueCount: 2,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-103',
    barcode: '005.276 SPR',
    title: 'Spring Boot 3 in Action',
    authorOrBrand: 'Craig Walls',
    description: 'Building microservices, Spring Security JWT authentication, WebFlux reactive streams, and REST API controllers.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9781617292545-L.jpg',
    type: 'BOOK',
    totalQuantity: 3,
    availableQuantity: 1,
    location: 'SHELF-A3',
    tags: ['java', 'spring-boot', 'backend'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-104',
    barcode: '621.381 OSC',
    title: 'Rigol DS1054Z 50MHz Digital Storage Oscilloscope',
    authorOrBrand: 'Rigol Technologies',
    description: '4-channel digital oscilloscope with 1GSa/s sampling rate, 12Mpts memory depth, and SPI/I2C protocol decoder module.',
    coverImageUrl: 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80',
    type: 'HARDWARE',
    totalQuantity: 2,
    availableQuantity: 2,
    location: 'LAB-ROOM-204',
    tags: ['hardware', 'electronics', 'oscilloscope'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-105',
    barcode: '621.395 FPGA',
    title: 'Xilinx Artix-7 FPGA Nexys A7 Trainer Kit',
    authorOrBrand: 'Digilent / Xilinx',
    description: 'FPGA development board with 101,440 logic cells, 240 DSP slices, 15.8 Mb BRAM, PMOD expanders, and VGA output.',
    coverImageUrl: 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80',
    type: 'LAB_KIT',
    totalQuantity: 3,
    availableQuantity: 0,
    location: 'LAB-ROOM-204',
    tags: ['lab-kit', 'fpga', 'verilog'],
    status: 'BORROWED',
    activeQueueCount: 4,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-106',
    barcode: '371.330 SEM',
    title: 'Capstone Seminar Room 402B (8-Person Capacity)',
    authorOrBrand: 'Campus Library Facilities',
    description: 'Acoustically dampened seminar room equipped with 4K interactive touchscreen display, dual whiteboards, and HDMI presentation hub.',
    coverImageUrl: 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=600&q=80',
    type: 'SEMINAR_ROOM',
    totalQuantity: 1,
    availableQuantity: 1,
    location: 'FLOOR-4',
    tags: ['seminar-room', 'collaboration', 'study'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-107',
    barcode: '005.117 CLN',
    title: 'Clean Code: A Handbook of Agile Software Craftsmanship',
    authorOrBrand: 'Robert C. Martin (Uncle Bob)',
    description: 'Principles, patterns, and practices of writing clean, readable, refactorable, and maintainable software code.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg',
    type: 'BOOK',
    totalQuantity: 6,
    availableQuantity: 4,
    location: 'SHELF-B1',
    tags: ['clean-code', 'refactoring', 'agile'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-108',
    barcode: '005.300 SYS',
    title: 'System Design Interview — An Insider\'s Guide',
    authorOrBrand: 'Alex Xu',
    description: 'Step-by-step framework for designing large-scale web systems: rate limiters, key-value stores, distributed caches, and newsfeed algorithms.',
    coverImageUrl: 'https://images-na.ssl-images-amazon.com/images/I/71u9i8S2GGL.jpg',
    type: 'BOOK',
    totalQuantity: 4,
    availableQuantity: 1,
    location: 'SHELF-B2',
    tags: ['system-design', 'interview', 'scalability'],
    status: 'AVAILABLE',
    activeQueueCount: 1,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-109',
    barcode: '005.100 ALG',
    title: 'Introduction to Algorithms (CLRS 4th Edition)',
    authorOrBrand: 'Thomas H. Cormen, Charles E. Leiserson',
    description: 'The standard algorithm textbook covering dynamic programming, graph algorithms, NP-completeness, and B-trees.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780262046305-L.jpg',
    type: 'BOOK',
    totalQuantity: 8,
    availableQuantity: 5,
    location: 'SHELF-B3',
    tags: ['algorithms', 'data-structures', 'cs-theory'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-110',
    barcode: '004.600 NET',
    title: 'Computer Networking: A Top-Down Approach (8th Ed)',
    authorOrBrand: 'James Kurose & Keith Ross',
    description: 'Top-down layer approach to computer networks: HTTP/3, TCP/UDP sockets, BGP routing protocols, and Wi-Fi security.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780133594140-L.jpg',
    type: 'BOOK',
    totalQuantity: 5,
    availableQuantity: 2,
    location: 'SHELF-C1',
    tags: ['networking', 'protocols', 'tcp-ip'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-111',
    barcode: '006.300 ART',
    title: 'Artificial Intelligence: A Modern Approach (4th Ed)',
    authorOrBrand: 'Stuart Russell & Peter Norvig',
    description: 'The leading textbook in AI covering probabilistic reasoning, search algorithms, reinforcement learning, and NLP.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780134610993-L.jpg',
    type: 'BOOK',
    totalQuantity: 6,
    availableQuantity: 0,
    location: 'SHELF-C2',
    tags: ['ai', 'machine-learning', 'python'],
    status: 'BORROWED',
    activeQueueCount: 3,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-112',
    barcode: '006.310 DEE',
    title: 'Deep Learning (Adaptive Computation & ML Series)',
    authorOrBrand: 'Ian Goodfellow, Yoshua Bengio, Aaron Courville',
    description: 'Mathematical foundation of deep neural networks, backpropagation, CNNs, Transformers, and Generative Adversarial Networks.',
    coverImageUrl: 'https://covers.openlibrary.org/b/isbn/9780262035613-L.jpg',
    type: 'BOOK',
    totalQuantity: 4,
    availableQuantity: 2,
    location: 'SHELF-C3',
    tags: ['deep-learning', 'neural-networks', 'pytorch'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-113',
    barcode: '621.398 RPI',
    title: 'Raspberry Pi 5 (8GB) Edge Computing Starter Kit',
    authorOrBrand: 'Raspberry Pi Foundation',
    description: 'Quad-core ARM Cortex-A76 board with active cooler, NVMe M.2 HAT, 64GB microSD, and camera module for IoT edge AI.',
    coverImageUrl: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80',
    type: 'HARDWARE',
    totalQuantity: 4,
    availableQuantity: 3,
    location: 'LAB-ROOM-204',
    tags: ['hardware', 'raspberry-pi', 'iot', 'edge-ai'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-114',
    barcode: '621.399 ARD',
    title: 'Arduino Mega 2560 Sensor & Mechatronics Kit',
    authorOrBrand: 'Arduino Official',
    description: 'ATmega2560 board with 54 digital I/O pins, 16 analog inputs, stepper motor drivers, ultrasonic sensors, and LCD shield.',
    coverImageUrl: 'https://images.unsplash.com/photo-1553406830-ef2513450d76?auto=format&fit=crop&w=600&q=80',
    type: 'LAB_KIT',
    totalQuantity: 5,
    availableQuantity: 4,
    location: 'LAB-ROOM-204',
    tags: ['lab-kit', 'arduino', 'mechatronics'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-115',
    barcode: '621.382 DMM',
    title: 'Keysight 34461A 6½ Digit Precision Multimeter',
    authorOrBrand: 'Keysight Technologies',
    description: 'Truevolt benchtop digital multimeter with 0.0035% DC accuracy, LAN/USB connectivity, and trend chart graphing display.',
    coverImageUrl: 'https://images.unsplash.com/photo-1581092335397-9583fe92d232?auto=format&fit=crop&w=600&q=80',
    type: 'HARDWARE',
    totalQuantity: 2,
    availableQuantity: 1,
    location: 'LAB-ROOM-205',
    tags: ['hardware', 'multimeter', 'precision'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'res-116',
    barcode: '371.331 ROB',
    title: 'Robotics Lab Project Pod 104 (12-Person Capacity)',
    authorOrBrand: 'Campus Engineering Facilities',
    description: 'High-clearance robotics development bay equipped with overhead power drop cords, soldering station, 3D printer, and Ethernet drops.',
    coverImageUrl: 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=600&q=80',
    type: 'SEMINAR_ROOM',
    totalQuantity: 1,
    availableQuantity: 1,
    location: 'BUILDING-4',
    tags: ['seminar-room', 'robotics', 'workshop'],
    status: 'AVAILABLE',
    activeQueueCount: 0,
    createdAt: new Date().toISOString(),
  }
];

export default function CatalogPage() {
  const [resources, setResources] = useState<ResourceDTO[]>([]);
  const [search, setSearch] = useState('');
  const [selectedType, setSelectedType] = useState<string>('ALL');
  const [selectedTag, setSelectedTag] = useState<string>('');
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchCatalog = async () => {
    setLoading(true);
    try {
      const data = await fetchWithAuth(`/resources?search=${encodeURIComponent(search)}&size=50`);
      const items = Array.isArray(data?.content)
        ? data.content
        : Array.isArray(data)
        ? data
        : [];
      setResources(items.length > 0 ? items : FALLBACK_CATALOG);
    } catch (err) {
      // Fallback to sample catalog if unauthenticated, offline, or timed out
      setResources(FALLBACK_CATALOG);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCatalog();
  }, [search]);

  // Extract unique tags for card-catalogue filter drawer
  const availableTags = Array.from(
    new Set(resources.flatMap((res) => res.tags || []))
  );

  const filteredResources = resources.filter((res: ResourceDTO) => {
    const matchesType = selectedType === 'ALL' || !selectedType || res.type === selectedType;
    const matchesTag = !selectedTag || (res.tags && res.tags.includes(selectedTag));
    return matchesType && matchesTag;
  });

  return (
    <div className="space-y-8">
      {/* Search Header */}
      <div className="border-3 border-ink bg-paper-2 p-6 shadow-brutal flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <h1 className="font-display text-3xl font-black text-ink uppercase tracking-tight">
              CAMPUS CATALOGUE
            </h1>
            <Sticker color="highlight" rotate="-2deg">
              LIVE INVENTORY
            </Sticker>
          </div>
          <p className="font-mono text-xs text-ink/80">
            Card catalogue search across books, hardware, oscilloscopes, and seminar rooms
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          {/* Search Input */}
          <div className="relative flex-1 md:w-72">
            <Search className="w-4 h-4 text-ink absolute left-3.5 top-3" />
            <input
              type="text"
              placeholder="Search title, author, barcode..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-paper border-3 border-ink pl-10 pr-4 py-2 font-mono text-xs text-ink placeholder:text-ink/50 focus:outline-none focus:ring-3 focus:ring-ink"
            />
          </div>

          {/* Drawer Filter Trigger */}
          <BrutalButton variant="ghost" size="md" onClick={() => setIsDrawerOpen(true)}>
            <Filter className="w-4 h-4" />
            <span>DRAWER</span>
          </BrutalButton>

          {/* 3D Hall Switch Toggle */}
          <Link href="/shelf-hall">
            <BrutalButton variant="secondary" size="md">
              <Layers className="w-4 h-4" />
              <span>3D HALL</span>
            </BrutalButton>
          </Link>

          <button
            onClick={fetchCatalog}
            className="p-2.5 border-3 border-ink bg-paper-2 shadow-brutal-sm hover:bg-paper active:translate-x-[2px] active:translate-y-[2px]"
            title="Refresh Catalogue"
          >
            <RefreshCw className={`w-4 h-4 text-ink ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Category Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b-3 border-ink">
        {[
          { label: 'ALL ASSETS', value: 'ALL', icon: <Layers className="w-4 h-4" /> },
          { label: 'BOOKS', value: 'BOOK', icon: <Book className="w-4 h-4" /> },
          { label: 'HARDWARE KITS', value: 'HARDWARE', icon: <Cpu className="w-4 h-4" /> },
          { label: 'LAB EQUIPMENT', value: 'LAB_KIT', icon: <Wrench className="w-4 h-4" /> },
          { label: 'SEMINAR ROOMS', value: 'SEMINAR_ROOM', icon: <DoorOpen className="w-4 h-4" /> },
        ].map((tab) => (
          <button
            key={tab.value}
            onClick={() => setSelectedType(tab.value)}
            className={`flex items-center space-x-2 px-4 py-2 border-3 border-ink font-mono text-xs font-bold uppercase transition-all whitespace-nowrap ${
              selectedType === tab.value
                ? 'bg-highlight shadow-brutal-sm translate-x-[-2px] translate-y-[-2px]'
                : 'bg-paper-2 hover:bg-paper'
            }`}
          >
            {tab.icon}
            <span>{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Catalogue Drawer Filter */}
      <DrawerFilter
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        selectedType={selectedType}
        onSelectType={setSelectedType}
        selectedTag={selectedTag}
        onSelectTag={setSelectedTag}
        availableTags={availableTags}
        onReset={() => {
          setSelectedType('ALL');
          setSelectedTag('');
        }}
      />

      {/* Grid */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="border-3 border-ink bg-paper-2 p-6 h-56 animate-pulse" />
          ))}
        </div>
      ) : filteredResources.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredResources.map((resource) => (
            <ResourceCard key={resource.id} resource={resource} />
          ))}
        </div>
      ) : (
        <div className="border-3 border-ink bg-paper-2 shadow-brutal p-12 text-center space-y-4">
          <Layers className="w-12 h-12 text-ink mx-auto" />
          <h3 className="font-display text-2xl font-black text-ink uppercase">
            No Matching Assets Found
          </h3>
          <p className="font-mono text-xs text-ink max-w-md mx-auto">
            Try adjusting your search criteria or resetting the card catalogue drawer filters.
          </p>
          <BrutalButton
            variant="primary"
            onClick={() => {
              setSearch('');
              setSelectedType('ALL');
              setSelectedTag('');
            }}
          >
            RESET ALL FILTERS
          </BrutalButton>
        </div>
      )}
    </div>
  );
}
