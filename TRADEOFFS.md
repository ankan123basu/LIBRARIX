# LIBRARIX — Technical Trade-Offs & Interview Defense Guide

This document presents all system design, data structure, and architectural trade-offs in **LIBRARIX** in simple, clear terms. It includes an **Interview Defense Q&A Section** with expected interviewer pushback and an **Honest Engineering Limitations & Methodology** section.

---

## 1. Data Structure & Algorithm Trade-Off Matrix

| Component | Our Choice | Alternative | Key Trade-Off & Rationale |
|:---|:---|:---|:---|
| **Catalog Search** | **In-Memory Trie** ($O(p+k)$) | MongoDB Regex (`^prefix.*`) | **Trade-off**: Uses ~2MB of RAM.<br>**Gain**: **65x–80x search speedup** (0.35ms–0.80ms vs 25.1ms–59.0ms) and zero DB CPU load during auto-complete. |
| **Duplicate Borrow Check** | **Bloom Filter** (Probabilistic) | Direct DB Query (`findActive`) | **Trade-off**: 1.5% false positive rate.<br>**Gain**: Saves **57.6% of database reads** with zero false negatives (0% risk of wrong rejection). |
| **Due-Date Analytics** | **Segment Tree** ($O(\log N)$) | Linear Array Sweep ($O(N)$) | **Trade-off**: Requires $4N$ memory overhead.<br>**Gain**: Guarantees $O(\log N)$ range sum bounds as $N$ scales; at $N=365$, both execute 10k queries in ~0.4ms–1.1ms. |
| **Rate Throttling** | **Sliding Window Log** | Fixed Window Counter | **Trade-off**: Stores timestamps in a queue per user.<br>**Gain**: Eliminates boundary traffic bursts and guarantees **100% rate precision**. |
| **Hot Path Cache** | **LRU-K Cache** ($K=2$) | Standard LRU Cache | **Trade-off**: Stores 2 timestamps per key instead of 1.<br>**Gain**: **+10.9% higher hit rate** (71.5% vs 64.5%) by preventing single-scan cache pollution. |
| **Pickup Routing** | **Dijkstra Min-Heap** | Alphabetical / Arbitrary Order | **Trade-off**: $O(E \log V)$ path calculation cost.<br>**Gain**: Cuts physical walking distance by **42.4%** across library aisles. |

---

## 2. Architecture & Infrastructure Trade-Off Matrix

| Architectural Choice | Alternative Considered | Why We Chose Our Approach |
|:---|:---|:---|
| **MongoDB 7.0 Document Database** | PostgreSQL Relational DB | **Why**: Books have dynamic schemas (varying tags, edition details, vector embeddings). MongoDB document models permit flexible schema evolution without migrations, and `@Version` provides optimistic locking. |
| **Stateless JWT Auth** | Server-Side HTTP Sessions | **Why**: Eliminates server session state, enabling effortless horizontal scaling and low RAM overhead. |
| **Spring Simple STOMP Broker** | External RabbitMQ Relay | **Why**: Zero external infrastructure setup required for local deployment and viva demonstrations while supporting full STOMP protocol. |

---

## 3. Interview Defense Q&A (How to Answer Interviewer Pushback)

### Question 1: *"What happens if your Trie runs out of RAM when the catalog grows to 10 million books?"*
- **Your Answer**:
  > *"For a catalog of 10 million titles, an in-memory Trie might take ~500MB of RAM. If memory becomes constrained, we have two scaling paths: first, we can back the Trie with a distributed memory store like Redis using sorted sets (ZSET) for prefix range scanning. Second, we can shard the Trie across nodes based on title prefix letters (A–M on Node 1, N–Z on Node 2)."*

---

### Question 2: *"Bloom filters have false positives. Doesn't a false positive stop a student from borrowing a book?"*
- **Your Answer**:
  > *"No, absolutely not! Bloom filters have **zero false negatives**, but their false positives only mean the filter says 'Maybe'. When the filter returns 'Maybe', we simply perform the real MongoDB database check to confirm. The student is never wrongly rejected. The Bloom filter just acts as a fast pre-check that lets 58% of non-duplicate requests bypass the database entirely."*

---

### Question 3: *"Why did you choose LRU-K (K=2) instead of standard LRU or LFU?"*
- **Your Answer**:
  > *"Standard LRU suffers from cache pollution — if a user performs a sequential catalog scan, popular books get evicted. LFU has the opposite problem: items that were popular months ago stay in cache forever ('frequency pollution'). LRU-2 hits the sweet spot: an item needs at least 2 accesses to gain retention priority, resisting one-off sweeps while adapting quickly to changing popularity."*

---

### Question 4: *"Why Segment Tree instead of a simple database aggregate query or array loop?"*
- **Your Answer**:
  > *"MongoDB aggregate queries require scanning documents on every request, which scales linearly with active loans. At our current 365-day array scale, a naive loop actually ties or slightly beats a Segment Tree because 365 integers fit inside CPU L1 cache. However, the Segment Tree's real value is architectural: it guarantees O(log N) worst-case range query bounds as data scales to multi-year horizons where cache locality no longer saves you."*

---

### Question 5: *"How do you handle race conditions when 100 students try to borrow the last copy of a book simultaneously?"*
- **Your Answer**:
  > *"We use MongoDB Optimistic Locking via Spring Data's `@Version` field on the Resource document. When 100 concurrent requests attempt to decrement `availableQuantity`, only the first request succeeds. The remaining 99 requests detect a version mismatch, throw an `OptimisticLockingFailureException`, and are gracefully redirected to join the Weighted Fair Queue waitlist."*

---

## 4. Honest Engineering Limitations & Methodological Disclaimers

Every mature software engineer acknowledges the boundaries of their measurements and architecture. Here is the honest breakdown of LIBRARIX's current limitations:

### 1. Dataset Scale vs Asymptotic Value
- **Reality**: On our dataset of 60 items to low hundreds, saving 32ms per 1,000 queries equates to a few microseconds per single request — negligible in human perception.
- **Engineering Purpose**: The value of using $O(p+k)$ Tries or $O(\log N)$ Segment Trees is demonstrating asymptotic efficiency. The performance advantage compounds exponentially as the catalog scales to tens of thousands of records.

### 2. Manual Timing vs JMH Framework
- **Reality**: Our benchmark suite uses custom JIT warmup iterations (50 runs) and steady-state median timing in JUnit (`System.nanoTime()`), not the industry-standard **JMH (Java Microbenchmark Harness)**.
- **Engineering Purpose**: While our warmup + median approach eliminates cold-start JIT compilation noise, JMH is the gold standard for avoiding compiler dead-code elimination, handling OS thread jitter, and CPU frequency scaling.

### 3. Isolated Micro-benchmarks vs End-to-End HTTP Latency
- **Reality**: Benchmarks measure raw Java data structures in isolation inside unit tests.
- **Engineering Purpose**: Full HTTP requests incur additional latency overheads from Spring MVC interceptor chains, Jackson JSON serialization, Spring Security filter evaluation, and MongoDB network socket I/O.

### 4. Graph Topology Dependency (Dijkstra)
- **Reality**: The 42.4% walking distance reduction is specific to our 12-zone library aisle adjacency graph.
- **Engineering Purpose**: It proves shortest-path optimization on this specific graph topology, but percentage savings will vary depending on physical floor plan layouts.

### 5. In-Memory Concurrency vs Multi-Node Distributed Scale
- **Reality**: LIBRARIX is a single-node modular monolith. Algorithms use thread-safe collections (`ConcurrentHashMap`, `ConcurrentLinkedDeque`), but have not been stress-tested under high multi-threaded contention across distributed clusters.
- **Engineering Purpose**: To scale horizontally, stateful structures (Trie, Rate Limiter log, Bloom filter) would be migrated to distributed Redis/Memcached infrastructure.
