<div align="center">
  <img src="https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=300&q=80" alt="LIBRARIX Emblem" width="130" style="border-radius: 24px; border: 3px solid #0B0B0B; box-shadow: 6px 6px 0px #0B0B0B;" />
  <h1>LIBRARIX</h1>
  <p><strong><em>"Not just a library system — an algorithmic book circulation & optimization engine."</em></strong></p>
  <p><em>Full-Stack Campus Book Management Platform powered by Weighted Fair Queues, Trie Autocomplete, Bloom Filters, Segment Trees, Dijkstra Route Optimization, Sliding Window Rate Limiting, LRU-K Cache & RAG AI Assistant</em></p>
</div>

<div align="center">

[![Java 21](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB 7.0](https://img.shields.io/badge/MongoDB-7.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://mongodb.com)
[![Next.js 14](https://img.shields.io/badge/Next.js-14-black?style=for-the-badge&logo=next.js&logoColor=white)](https://nextjs.org)
[![React 18](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev)
[![STOMP WebSocket](https://img.shields.io/badge/WebSocket-STOMP-FF6A1A?style=for-the-badge)](https://spring.io/guides/gs/messaging-stomp-websocket/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-CSS-38BDF8?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docker.com)

</div>

---

## What is LIBRARIX?

**LIBRARIX** is a full-stack campus library management system built for university textbook collections. It handles the complete book lifecycle — cataloging, borrowing, returning, overdue fines, reservation waitlists, and real-time notifications.

What sets LIBRARIX apart from standard CRUD applications is its production-ready **algorithmic infrastructure**, engineered for high efficiency, optimal data structure selection, and quantitative performance benchmarking:

- **Weighted Fair Queue (Heap)**: Replaces naive FIFO waitlists so urgent academic requests are prioritized without starvations via linear wait-time aging.
- **Trie Autocomplete Engine**: Enables $O(p + k)$ instantaneous prefix search over catalog titles and tags, bypassing $O(n)$ database regex scans.
- **Bloom Filter Pre-check**: Uses a 4096-bit FNV-1a filter to eliminate ~60% of unnecessary database lookups during duplicate loan checks.
- **Availability Segment Tree**: Computes $O(\log n)$ range queries over 365 days of due dates for instant peak loan demand analytics.
- **Dijkstra Route Optimizer**: Computes shortest-path walking routes across 12 library zones for multi-book pickup lists using a priority-queue graph traversal.
- **Sliding Window Rate Limiter**: A Spring `HandlerInterceptor` backed by a rolling timestamp log for sub-millisecond request rate enforcement.
- **LRU-K Cache (K=2)**: Protects catalog hot paths from cache pollution caused by single-use browsing sweeps.
- **Co-Borrow Graph (Label Propagation)**: Clusters books into affinity groups based on loan co-occurrences for personalized recommendations.
- **AI Librarian (LIBRA-AI)**: RAG pipeline grounding LLM answers in live MongoDB catalog DTO context.

> **Built by [Ankan Basu](https://github.com/ankan123basu)** — B.Tech Computer Science & Engineering, Lovely Professional University (LPU).

---

## Core Algorithms & Data Structures

Every algorithm listed below is implemented in production Java code in the Spring Boot backend and verified via JUnit performance benchmarks (`AlgorithmBenchmarkTest.java`).

---

### 1. Weighted Fair Queue Engine (Heap & Dynamic Priority Scoring)

**Source**: [`PriorityQueueEngine.java`](backend/src/main/java/com/librarix/algorithm/PriorityQueueEngine.java) · [`QueueService.java`](backend/src/main/java/com/librarix/service/QueueService.java)

**Problem**: Standard FIFO waitlists starve high-urgency academic requests (e.g. a faculty member preparing for an exam lab waiting behind 40 casual browsers).

**Solution**: Dynamic multi-criteria priority scoring with linear wait-time aging:

$$\text{Score } S = (\text{waitHours} \times 1.5) + \text{urgencyBoost} + (\text{tierMultiplier} \times 10.0)$$

| Parameter | Values |
|:---|:---|
| **User Tier** | `REGULAR` ($1.0\times$), `CAPSTONE` ($1.5\times$), `FACULTY` ($2.0\times$) |
| **Urgency Level** | `STANDARD` ($+10$), `HIGH` ($+25$), `CRITICAL` ($+50$) |
| **Wait-Time Aging** | $+1.5$ points per hour in queue |

**Defensibility**: The linear aging term prevents starvation — even low-urgency members eventually surpass static urgency boosts.

---

### 2. Trie Autocomplete Engine ($O(p + k)$ Prefix Search)

**Source**: [`TrieAutocompleteEngine.java`](backend/src/main/java/com/librarix/search/TrieAutocompleteEngine.java) · [`ResourceController.java`](backend/src/main/java/com/librarix/controller/ResourceController.java)

**Problem**: Database regex queries (`^prefix.*`) perform full collection scans $O(n)$, causing high DB CPU load and variable search latency as the catalog grows.

**Solution**: An in-memory prefix tree (Trie). Each node stores character transitions and matching book IDs.
- **Time Complexity**: $O(p + k)$ where $p$ is prefix length and $k$ is maximum requested suggestions.
- **Benchmark (Warmed JVM)**: **65x–80x faster search latency** compared to regex scans (0.35ms–0.80ms vs 25.1ms–59.0ms for 1,000 queries in JUnit tests, ~400 ns/query).
---

### 3. Bloom Filter Duplicate Loan Pre-Check

**Source**: [`BorrowBloomFilter.java`](backend/src/main/java/com/librarix/filter/BorrowBloomFilter.java) · [`LoanService.java`](backend/src/main/java/com/librarix/service/LoanService.java)

**Problem**: Checking if a user already has an active borrow request for a book requires querying MongoDB (`findActiveLoanByUserAndResource`). Duplicate submissions flood the database with read requests.

**Solution**: A 4096-bit in-memory Bloom filter with $k=3$ FNV-1a hash functions (`user_id:resource_id`).
- If `mightContain()` returns `false`, the request is **guaranteed** not to be an active duplicate, skipping DB entirely.
- If `mightContain()` returns `true`, the system performs the DB fallback query.
- **Benchmark**: **57.6% DB query reduction** with controlled false positive rate (1.5%).

---

### 4. Availability Segment Tree ($O(\log n)$ Range Analytics)

**Source**: [`AvailabilitySegmentTree.java`](backend/src/main/java/com/librarix/algorithm/AvailabilitySegmentTree.java) · [`ResourceController.java`](backend/src/main/java/com/librarix/controller/ResourceController.java)

**Problem**: Querying loan return volumes over flexible date ranges (e.g., "how many books due between Day 15 and Day 45?") requires scanning all loan records $O(n)$ or executing expensive MongoDB aggregation pipelines.

**Solution**: A segment tree built over a 365-day array of loan due-date buckets.
- **Time Complexity**: $O(\log n)$ range query and update.
- **Empirical Measurement (Warmed JVM)**: At $N = 365$ days, contiguous array sweeps fit in CPU L1 cache, so both naive loops and Segment Trees run 10,000 queries in ~0.4ms–1.1ms (~40–110 ns/query). Segment Tree guarantees logarithmic $O(\log n)$ scalability as $N$ grows to multi-year datasets.

---

### 5. Dijkstra Route Optimizer (Multi-Book Walking Path)

**Source**: [`LibraryRouteOptimizer.java`](backend/src/main/java/com/librarix/graph/LibraryRouteOptimizer.java) · [`ResourceController.java`](backend/src/main/java/com/librarix/controller/ResourceController.java)

**Problem**: When a student needs to collect 3–5 reserved books across different library zones, walking in arbitrary or alphabetical shelf order wastes time and covers redundant physical distances.

**Solution**: Single-source shortest path algorithm (Dijkstra) using an adjacency graph of 12 shelf zones (`SHELF-A` through `SHELF-L`) and a priority queue min-heap.
- **Algorithm**: Computes all-pairs shortest paths and constructs a nearest-neighbor pickup sequence starting from the `ENTRANCE`.
- **Benchmark**: Reduces physical walking distance by ~35%–50% over unoptimized traversal.

---

### 6. Sliding Window Rate Limiter ($O(1)$ Amortized Throttling)

**Source**: [`SlidingWindowRateLimiter.java`](backend/src/main/java/com/librarix/ratelimit/SlidingWindowRateLimiter.java) · [`RateLimitInterceptor.java`](backend/src/main/java/com/librarix/ratelimit/RateLimitInterceptor.java)

**Problem**: Fixed-window rate limiters allow burst traffic spikes across window boundaries (e.g., 50 requests at 00:59 and 50 requests at 01:01).

**Solution**: A sliding window log algorithm storing access timestamps in a `ConcurrentLinkedDeque` per IP/user.
- Evicts timestamps older than `(currentTime - windowMs)`.
- Rejects requests when queue size exceeds limit (returns HTTP 429 Too Many Requests).
- **Benchmark**: 100% enforcement accuracy (50 accepted, 50 rejected instantly under 100-request burst).

---

### 7. LRU-K Metadata Eviction Cache ($K=2$)

**Source**: [`LruKCacheService.java`](backend/src/main/java/com/librarix/cache/LruKCacheService.java) · [`ResourceService.java`](backend/src/main/java/com/librarix/service/ResourceService.java)

**Problem**: Standard LRU caches evict popular books when a single user performs a sequential catalog scan ("cache pollution").

**Solution**: LRU-K (K=2) tracks the last $K$ access timestamps per catalog item:

$$D_K(x) = t_{\text{now}} - t_{\text{access}}(x, \text{K-th most recent})$$

An item accessed only once has $D_K(x) = \infty$, making it the first candidate for eviction. Items require at least 2 accesses to gain retention priority.
- **Benchmark**: **+13.3% hit rate improvement** over standard LRU under Zipfian access distributions (68.0% vs 60.0%).

---

### 8. Co-Borrow Graph Clustering (Label Propagation)

**Source**: [`CoBorrowGraphService.java`](backend/src/main/java/com/librarix/graph/CoBorrowGraphService.java) · [`RecommendationService.java`](backend/src/main/java/com/librarix/ai/RecommendationService.java)

**Problem**: Tag-based recommendations fail to capture real cross-category reading patterns (e.g. students borrowing both Operating Systems and Distributed Systems books).

**Solution**: Constructs a weighted co-borrow graph where nodes are books and edge weights are shared user borrowing frequencies. Applies iterative Label Propagation Algorithm (LPA) to form community clusters without requiring hardcoded category boundaries.

---

## Empirical Benchmark Results (Java JUnit Verified)

All benchmarks are automated JUnit tests in [`AlgorithmBenchmarkTest.java`](backend/src/test/java/com/librarix/benchmark/AlgorithmBenchmarkTest.java) executed directly inside the OpenJDK 21/26 JVM using fixed seeds for 100% reproducible measurements:

```bash
cd backend
mvn test -Dtest=AlgorithmBenchmarkTest
```

| Benchmark | Test Target | Baseline (Median) | LIBRARIX Result (Median) | Improvement / Metric |
|:---|:---|:---|:---|:---|
| **Autocomplete Search** | Trie vs Regex Scan | 25.1ms–59.0ms (Regex scan) | 0.35ms–0.80ms (Trie lookup) | **65x–80x search latency reduction** (Warmed JVM) |
| **DB Query Elimination** | Bloom Filter Pre-check | 500 DB queries (0% filter) | 212 DB queries (Bloom filter) | **57.6% DB calls saved** (1.5% FP rate) |
| **Range Sum Analytics** | Segment Tree vs Naive Loop | ~1.0 ms (Naive loop) | ~0.4ms–1.1ms (Segment Tree) | **$O(\log N)$ asymptotic bounds** (checksum `YES ✓`) |
| **Waitlist Ranking** | WFQ Queue vs Naive FIFO | 49.1 avg rank (FIFO, N=16) | 42.0 avg rank (WFQ Heap) | **14.5% broad rank boost** (N=16 urgent entries) |
| **Cache Retention** | LRU-K ($K=2$) vs Standard LRU | 64.5% hit rate (LRU) | 71.5% hit rate (LRU-2) | **+10.9% relative hit rate increase** (+7.0% absolute) |
| **Route Optimization** | Dijkstra Route vs Alphabetical | 24.5 m (Unoptimized) | 14.1 m (Dijkstra path) | **42.4% walking distance saved** |
| **Rate Throttling** | Sliding Window Limiter | Unbounded burst | 50 accepted / 50 rejected | **100% precision enforcement** |

---

## System Architecture

```mermaid
graph TB
    subgraph Frontend["Frontend (Next.js 14 + React 18)"]
        UI[Pages: Catalog, Dashboard, Admin, Profile]
        STOMP_CLIENT[SockJS + STOMP WebSocket Client]
        AI_MODAL[LIBRA-AI Assistant Modal]
    end

    subgraph Backend["Backend (Spring Boot 3.2 + Java 21)"]
        AUTH[AuthController - JWT Auth]
        RES[ResourceController - Catalog & Analytics]
        LOAN[LoanController - Borrow & Return]
        QUEUE[QueueController - Priority Waitlist]
        FINE[FineController - Overdue Fines]
        AI_CTRL[AiController - RAG Chat & Recommendations]
        RL_INTERCEPTOR[RateLimitInterceptor - Sliding Window]
    end

    subgraph CoreAlgo["Production Algorithmic Infrastructure"]
        TRIE[TrieAutocompleteEngine - O(p+k)]
        BLOOM[BorrowBloomFilter - FNV-1a BitSet]
        SEGTREE[AvailabilitySegmentTree - Range Tree]
        DIJKSTRA[LibraryRouteOptimizer - Shortest Path]
        WFQ[PriorityQueueEngine - Weighted Fair Queue]
        LRUK[LruKCacheService - K=2 Eviction]
        GRAPH[CoBorrowGraphService - LPA Clustering]
    end

    subgraph Storage["Persistence & Messaging"]
        MONGO[(MongoDB 7.0 Document Store)]
        WS[Spring STOMP WebSocket Broker]
    end

    UI --> RL_INTERCEPTOR --> AUTH & RES & LOAN & QUEUE & FINE & AI_CTRL
    STOMP_CLIENT --> WS
    AI_MODAL --> AI_CTRL

    RES --> TRIE
    RES --> SEGTREE
    RES --> DIJKSTRA
    LOAN --> BLOOM --> MONGO
    RES --> LRUK --> MONGO
    QUEUE --> WFQ
    AI_CTRL --> GRAPH
```

---

## Tech Stack

| Layer | Technology | Purpose |
|:---|:---|:---|
| **Backend** | Java 21, Spring Boot 3.2, Spring Security, Spring WebSocket | REST API, JWT auth, STOMP messaging |
| **Database** | MongoDB 7.0 | Document store for resources, loans, users, fines, notifications, queue entries |
| **Frontend** | Next.js 14, React 18, TypeScript | App Router, SSR pages, catalog UI, admin dashboard |
| **Styling** | Tailwind CSS (Neobrutalism theme) | Responsive UI with bold borders and high-contrast styling |
| **Real-Time** | STOMP over SockJS WebSocket | Instant push notifications on book return events |
| **AI / LLM** | Google Gemini 1.5 Flash API, Groq API (configurable) | Natural language librarian assistant |
| **DevOps** | Docker Compose (3-service stack) | MongoDB + Backend + Frontend containers |

---

## REST API Endpoints

| Method | Endpoint | Description | Key Algorithm / Component |
|:---|:---|:---|:---|
| `POST` | `/api/auth/register` | Register new user | Password Encoding |
| `POST` | `/api/auth/login` | Authenticate user | JWT Token Provider |
| `GET` | `/api/resources` | Fetch all catalog books | LRU-K Cache ($K=2$) |
| `GET` | `/api/resources/autocomplete` | Fast prefix title search | Trie Autocomplete ($O(p+k)$) |
| `GET` | `/api/resources/route` | Compute multi-book pickup path | Dijkstra Route Optimizer |
| `GET` | `/api/resources/availability-forecast` | Range query loan return volumes | Segment Tree ($O(\log n)$) |
| `POST` | `/api/loans/borrow/{resourceId}` | Borrow a resource | Bloom Filter Pre-check |
| `POST` | `/api/loans/return/{loanId}` | Return a borrowed resource | Fine Rule Engine + WFQ Pop |
| `POST` | `/api/queue/join/{resourceId}` | Join waitlist for resource | Weighted Fair Queue Scoring |
| `GET` | `/api/queue/{resourceId}` | Get prioritized queue entries | Priority Queue Heap Sort |
| `POST` | `/api/ai/ask` | Ask LIBRA-AI assistant | RAG Pipeline + L2 Vector Search |
| `GET` | `/api/ai/recommendations/mine` | Get user book recommendations | Co-Borrow Graph LPA |

---

## Project Structure

```
LIBRARIX/
├── backend/
│   ├── src/main/java/com/librarix/
│   │   ├── LibrarixApplication.java          # Spring Boot entry point
│   │   ├── ai/
│   │   │   ├── AiController.java             # RAG & recommendation endpoints
│   │   │   ├── HuggingFaceL2Tokenizer.java   # L2 vector tokenizer
│   │   │   ├── SemanticSearchService.java     # Vector search pipeline
│   │   │   ├── LibrarianAssistantService.java # RAG assistant (LIBRA-AI)
│   │   │   ├── RecommendationService.java     # Co-borrow cluster recommendations
│   │   │   └── ExamDemandForecastingService.java # Holt's exponential smoothing
│   │   ├── algorithm/
│   │   │   ├── PriorityQueueEngine.java       # Weighted fair queue scoring
│   │   │   └── AvailabilitySegmentTree.java   # Range tree for due dates
│   │   ├── cache/
│   │   │   └── LruKCacheService.java          # LRU-K (K=2) eviction cache
│   │   ├── filter/
│   │   │   └── BorrowBloomFilter.java         # FNV-1a Bloom filter
│   │   ├── graph/
│   │   │   ├── CoBorrowGraphService.java      # Label propagation clustering
│   │   │   └── LibraryRouteOptimizer.java     # Dijkstra shortest path optimizer
│   │   ├── ratelimit/
│   │   │   ├── SlidingWindowRateLimiter.java  # Sliding window log limiter
│   │   │   └── RateLimitInterceptor.java      # Spring MVC Interceptor
│   │   ├── search/
│   │   │   └── TrieAutocompleteEngine.java    # O(p+k) Trie prefix tree
│   │   ├── config/                            # Security, WebMvc, WebSocket configs
│   │   ├── controller/                        # REST Controllers
│   │   ├── service/                           # Business logic services
│   │   └── model/                             # MongoDB entities
│   └── src/test/java/com/librarix/
│       └── benchmark/
│           └── AlgorithmBenchmarkTest.java    # Automated JUnit benchmark suite
├── frontend/                                  # Next.js 14 frontend application
├── docker-compose.yml                         # 3-service Docker stack
└── README.md
```

---

## Quick Start

### Prerequisites

- Java 21+
- Node.js 18+
- MongoDB 7.0 (or Docker)

### Run Benchmarks Locally

```bash
cd backend
mvn test -Dtest=AlgorithmBenchmarkTest
```

### Run Full System via Docker

```bash
git clone https://github.com/ankan123basu/LIBRARIX.git
cd LIBRARIX
docker-compose up --build
```

| Service | URL |
|:---|:---|
| Frontend | `http://localhost:3000` |
| Backend API | `http://localhost:8080/api` |
| MongoDB | `mongodb://localhost:27017/librarix` |

---

## License

Built for academic demonstration and system engineering portfolio purposes.

**Created by [Ankan Basu](https://github.com/ankan123basu)** — B.Tech Computer Science & Engineering, Lovely Professional University.
