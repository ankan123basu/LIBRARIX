# LIBRARIX — Algorithmic Decisions & Interview Defense Guide

This document contains **simple, clear explanations** for all decisions and algorithms in **LIBRARIX**. It is structured specifically to help you explain every algorithm confidently during a technical or FAANG-level system design interview.

---

## 1. Weighted Fair Queue Engine (Heap & Dynamic Urgency Scoring)

### Simple Elevator Pitch
> *"Instead of a plain FIFO waitlist that makes a faculty member wait behind 40 casual browsers, we use a Priority Queue with dynamic scoring and wait-time aging so urgent requests jump ahead without starving anyone."*

### Why Naive FIFO Fails
- A student needing a core textbook for a Capstone project due in 24 hours is trapped behind 40 people who requested it weeks ago for casual reading.
- However, pure priority queues cause **starvation** — low-priority members never get the book.

### How It Works (In Plain English)
Every person in the queue gets a dynamic score:

$$\text{PriorityScore} = (\text{waitHours} \times 1.5) + \text{urgencyBoost} + (\text{tierMultiplier} \times 10.0)$$

- **User Tier**: `REGULAR` ($1.0\times$), `CAPSTONE` ($1.5\times$), `FACULTY` ($2.0\times$)
- **Urgency**: `STANDARD` ($+10$), `HIGH` ($+25$), `CRITICAL` (+50)
- **Wait Aging**: $+1.5$ points added **every hour**.

### Why Starvation is Solved
Because the wait-time term grows continuously ($+1.5\text{ pts/hour}$), a `REGULAR` user who has waited 30 hours ($+45\text{ pts}$) will eventually surpass a newly joined `FACULTY` member with static boost ($+20\text{ pts}$).

### Complexity
- **Time**: $O(\log N)$ to insert/pop using a Max-Heap Priority Queue.
- **Space**: $O(N)$ queue entries in memory.

### 🗣️ Word-for-Word Interview Script
> *"We implemented a Weighted Fair Queue using a Max-Heap. Each reservation calculates a score based on User Tier, self-declared Urgency, and a linear wait-time aging factor. The key detail is the aging rate of 1.5 points per hour — this mathematically bounds maximum wait time and prevents low-tier starvation while prioritizing urgent academic needs."*

---

## 2. Trie Autocomplete Engine ($O(p + k)$ Prefix Search)

### Simple Elevator Pitch
> *"Instead of scanning the entire database with regex for every keypress, we store catalog titles in a Trie prefix tree so autocomplete runs in microseconds."*

### Why Naive Regex Fails
- Running `db.resources.find({ title: /^data.*/i })` performs a full collection scan ($O(N)$) over all database documents on every single character typed into the search bar.

### How It Works (In Plain English)
- We insert every book title into a tree of character nodes (`d -> a -> t -> a`).
- When a user types a prefix like `"dat"`, we walk down 3 character nodes to reach the node `'t'`.
- From node `'t'`, we do a short Depth-First Search (DFS) to gather the first $k$ leaf titles.

### Complexity
- **Time**: $O(p + k)$ where $p = \text{prefix length}$ and $k = \text{number of suggestions}$. (Compare: Regex is $O(N)$).
- **Space**: $O(\text{Total Characters in Titles})$.
- **Benchmark (Warmed JVM)**: **65x–80x faster latency** than database regex scans (0.35ms–0.80ms vs 25.1ms–59.0ms for 1,000 queries, ~400 ns/query).

### 🗣️ Word-for-Word Interview Script
> *"We built an in-memory Trie data structure for catalog autocomplete. Instead of triggering an O(N) database regex query on every keystroke, the Trie traverses the prefix in O(p) time and collects the top-K suggestions using DFS in O(k) time. After JVM JIT warmup, this reduced autocomplete search latency by 70x to 80x depending on CPU execution states."*

---

## 3. Bloom Filter Duplicate Loan Pre-Check

### Simple Elevator Pitch
> *"Before querying the database to check if a user already borrowed a book, we check a bit array in memory that instantly tells us 'NO' 58% of the time with zero DB calls."*

### Why Naive DB Queries Fail
- Every borrow request executes a database query `findActiveLoanByUserAndResource`. Under heavy click traffic, duplicate submissions flood MongoDB with useless read operations.

### How It Works (In Plain English)
- We allocate a 4096-bit array in RAM.
- When a loan is created, we hash `userId:resourceId` using 3 FNV-1a hash functions and set those 3 bit positions to `1`.
- When a borrow request comes in:
  - If any of the 3 bits is `0` $\rightarrow$ The user **definitely does NOT** have an active loan. We skip the DB call completely! (**Zero false negatives**).
  - If all 3 bits are `1` $\rightarrow$ The user **might** have an active loan. We check MongoDB to be sure.

### Complexity
- **Time**: $O(k) = O(1)$ constant time bit lookup.
- **Space**: 4096 bits = 512 bytes of memory.
- **Benchmark**: Eliminates **57.6% of database queries** with a controlled 1.5% false positive rate.

### 🗣️ Word-for-Word Interview Script
> *"To protect our MongoDB database from redundant read traffic, we added a Bloom filter pre-check using 3 FNV-1a hash functions. If the filter returns false, we are 100% sure no duplicate loan exists and skip the DB query entirely. This eliminated over 57% of database reads on checkout attempts."*

---

## 4. Availability Segment Tree ($O(\log N)$ Range Analytics)

### Simple Elevator Pitch
> *"To quickly find how many books are due between any two dates, we use a Segment Tree that provides O(log N) range query bounds instead of scanning database records."*

### Why Naive Loop Scanning Fails at Scale
- Querying return volumes across custom date ranges requires looping over array elements $O(N)$ or building expensive aggregation pipelines.

### How It Works (In Plain English)
- We map 365 calendar days into an array of loan due counts.
- We build a binary tree over this array where each parent node stores the sum of its left and right children.
- To query range $[L, R]$, we combine pre-computed sums from partial tree nodes in $O(\log N)$ steps.

### Complexity & Empirical Reality
- **Time**: $O(\log N)$ per range query (where $N = 365$ days).
- **Space**: $O(4N)$ tree node array.
- **Empirical Measurement**: At $N = 365$ days, a contiguous array fits in CPU L1 cache, so both naive loop and Segment Tree execute 10,000 range queries in ~0.4ms–1.1ms (~40–110 ns/query). The Segment Tree's $O(\log N)$ efficiency provides guaranteed bounds as $N$ scales to large multi-year horizons.

### 🗣️ Word-for-Word Interview Script
> *"For operational analytics on return due-dates, we built a Segment Tree over a 365-day array. At our current 365-day dataset scale, a naive contiguous array sweep actually ties or slightly beats the Segment Tree because the entire array fits inside CPU L1 cache. The Segment Tree's real engineering value is guaranteeing O(log N) worst-case range query bounds as catalog history scales to multi-year horizons where L1 cache locality stops saving you."*

---

## 5. Dijkstra Route Optimizer (Multi-Book Walking Path)

### Simple Elevator Pitch
> *"When a student has 4 books to pick up from different shelves, we use Dijkstra's algorithm to compute the shortest walking path through the library aisles."*

### Why Naive Route Order Fails
- Walking in random or alphabetical shelf order (`SHELF-A`, `SHELF-C`, `SHELF-J`) makes students backtrack across aisles, covering unnecessary physical distance.

### How It Works (In Plain English)
- We model the 12 library shelf zones (`SHELF-A` through `SHELF-L`) as a weighted undirected graph where edge weights represent physical walking distances in meters.
- We run Dijkstra's algorithm with a min-heap priority queue to find the shortest path from the entrance through all target shelf nodes.

### Complexity
- **Time**: $O(E \log V)$ where $V = 12$ shelf nodes, $E = \text{aisle edges}$.
- **Benchmark**: Saves **42.4% physical walking distance** (reduces 24.5 meters down to 14.1 meters average).

### 🗣️ Word-for-Word Interview Script
> *"We modeled the library layout as a weighted graph of shelf zones and implemented Dijkstra's shortest path algorithm using a min-heap. When a student requests a multi-book pickup list, the backend calculates the optimal physical route, cutting walking distance by over 40%."*

---

## 6. Sliding Window Rate Limiter ($O(1)$ Amortized Throttling)

### Simple Elevator Pitch
> *"We use a sliding window log with a rolling queue of request timestamps to strictly enforce 50 requests/minute per user with zero boundary burst vulnerabilities."*

### Why Fixed Window Limiting Fails
- A fixed window limiter (resetting at 00:00, 01:00) allows 50 requests at 00:59 and 50 requests at 01:01, resulting in a spike of 100 requests in 2 seconds.

### How It Works (In Plain English)
- We store incoming request timestamps in a `ConcurrentLinkedDeque` per user.
- On each request, we discard any timestamps older than `(currentTime - 60000ms)`.
- If the remaining queue size is $< 50$, we accept the request; otherwise, we return HTTP 429 Too Many Requests.

### Complexity
- **Time**: $O(1)$ amortized per request.
- **Space**: $O(M)$ memory where $M \le 50$ timestamps per active user.
- **Benchmark**: Verified 100% precision enforcement (50 accepted, 50 rejected under 100-request burst).

### 🗣️ Word-for-Word Interview Script
> *"We implemented a sliding window log rate limiter using a Spring HandlerInterceptor backed by a ConcurrentLinkedDeque. By maintaining a rolling window of request timestamps, we eliminate boundary traffic spikes and achieve sub-millisecond per-request rate enforcement."*

---

## 7. LRU-K Eviction Cache ($K=2$)

### Simple Elevator Pitch
> *"Standard LRU evicts popular books when someone scans the catalog once. LRU-K tracks the last 2 access timestamps so an item needs 2 hits before getting long-term cache priority."*

### How It Works (In Plain English)
- Standard LRU evicts the entry with the oldest last-access time.
- LRU-K ($K=2$) measures the **2nd most recent access time**.
- An item accessed only once has a 2nd access time of $-\infty$, making it the first item evicted if space is needed.

### Benchmark
- **+10.9% relative hit rate increase** (+7.0 percentage points absolute) over standard LRU under Zipfian catalog browsing (71.5% vs 64.5%).

---

## 8. Co-Borrow Graph Clustering (Label Propagation Algorithm)

### Simple Elevator Pitch
> *"We build a graph of books linked by co-borrowing history and use Label Propagation to automatically group related books into clusters for recommendations."*

### How It Works (In Plain English)
- Each book is a node. An edge is created between two books if the same student borrowed both.
- Over 20 iterations, every book adopts the cluster label held by the majority of its graph neighbors.
- Books in the same cluster are recommended to users who read other books in that cluster.

---

## 9. How to Answer Questions About Limitations & Scale in Interviews

### Question 1: *"Does a 79.8x speedup matter on a dataset of 60 books?"*
- **Your Answer**:
  > *"To be completely transparent — no, at 60 books saving 32ms per 1,000 queries is just a few microseconds per request. The engineering value isn't that 60 books was a performance bottleneck; it's selecting an $O(p+k)$ structure that scales asymptotically when the catalog grows to 100,000 books where $O(N)$ regex scans would degrade database performance."*

### Question 2: *"Did you use JMH for your benchmarks?"*
- **Your Answer**:
  > *"No, we wrote custom JUnit benchmark tests with a 50-iteration JIT warmup phase and median measurements over 20 iterations, protected with blackhole consumption to prevent dead-code elimination. For production-grade benchmarking, JMH would be the gold standard to handle JVM process forking and OS jitter."*

### Question 3: *"Are these micro-benchmarks or HTTP end-to-end latencies?"*
- **Your Answer**:
  > *"These are algorithm-level micro-benchmarks running directly inside the JVM. In a live HTTP environment, Spring MVC interceptor chains, Jackson JSON serialization, and MongoDB network I/O add overhead. The micro-benchmark isolates the algorithmic data structure performance."*
