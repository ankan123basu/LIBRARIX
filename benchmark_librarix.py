"""
LIBRARIX Core Algorithm Design Simulation Benchmark Suite
=========================================================
DISCLOSURE: This is a standalone Python Monte Carlo simulator.
It models the algorithms implemented in the Java/Spring Boot backend.
It does NOT call the running JVM or any live HTTP endpoints.

All metrics are design simulations of algorithms whose Java implementations
are verified to exist in the codebase. Numbers describe the algorithm's
behavior under synthetic workloads, not live system measurements.

Reproducible: seed=42. Run with: python benchmark_librarix.py
"""

import math
import random
import re
import time
from collections import defaultdict, OrderedDict

random.seed(42)

print("=" * 80)
print("  LIBRARIX ALGORITHM DESIGN SIMULATION BENCHMARK SUITE (Python Monte Carlo)")
print("  Seed: 42 | All metrics are design simulations, not live system measurements")
print("=" * 80 + "\n")


# =============================================================================
# ALGORITHM 1: Weighted Fair Queue Engine
# Java source: PriorityQueueEngine.java (com.librarix.algorithm)
# Formula:  S = (hoursWaiting * 1.5) + urgencyBoost + (tierMultiplier * 10.0)
# Urgency:  STANDARD=+10, HIGH=+25, CRITICAL=+50
# Tiers:    REGULAR=1.0x, CAPSTONE=1.5x, FACULTY=2.0x
# =============================================================================
print("[1/5] Weighted Fair Queue vs Naive FIFO")
print("       Java source: PriorityQueueEngine.java")
print("       Formula: S = (waitHours * 1.5) + urgencyBoost + (tierMult * 10.0)\n")

URGENCY_BOOST = {"STANDARD": 10.0, "HIGH": 25.0, "CRITICAL": 50.0}
TIER_MULT = {"REGULAR": 1.0, "CAPSTONE": 1.5, "FACULTY": 2.0}

events = []
for i in range(100):
    tier = random.choices(["REGULAR", "CAPSTONE", "FACULTY"], weights=[60, 30, 10])[0]
    urgency = random.choices(["STANDARD", "HIGH", "CRITICAL"], weights=[70, 20, 10])[0]
    wait_h = random.uniform(0.0, 48.0)
    score = (wait_h * 1.5) + URGENCY_BOOST[urgency] + (TIER_MULT[tier] * 10.0)
    events.append({"tier": tier, "urgency": urgency, "join_idx": i, "score": score})

fifo = sorted(events, key=lambda e: e["join_idx"])
wfq = sorted(events, key=lambda e: e["score"], reverse=True)

# All Capstone + Faculty members
all_high = lambda lst: [i+1 for i, e in enumerate(lst) if e["tier"] in ("CAPSTONE", "FACULTY")]
# Only urgent (HIGH/CRITICAL) Capstone + Faculty
urgent_high = lambda lst: [i+1 for i, e in enumerate(lst)
                           if e["tier"] in ("CAPSTONE", "FACULTY") and e["urgency"] in ("HIGH", "CRITICAL")]

fifo_all = all_high(fifo)
wfq_all = all_high(wfq)
fifo_urgent = urgent_high(fifo)
wfq_urgent = urgent_high(wfq)

avg = lambda xs: sum(xs) / len(xs)
pct_change = lambda old, new: ((old - new) / old) * 100

print(f"  All Capstone/Faculty members:")
print(f"    FIFO avg rank: {avg(fifo_all):.1f} / 100")
print(f"    WFQ  avg rank: {avg(wfq_all):.1f} / 100")
print(f"    Rank improvement: {pct_change(avg(fifo_all), avg(wfq_all)):.1f}%")
print(f"  Urgent (HIGH/CRITICAL) Capstone/Faculty:")
print(f"    FIFO avg rank: {avg(fifo_urgent):.1f} / 100")
print(f"    WFQ  avg rank: {avg(wfq_urgent):.1f} / 100")
print(f"    Rank improvement: {pct_change(avg(fifo_urgent), avg(wfq_urgent)):.1f}%\n")


# =============================================================================
# ALGORITHM 2: L2 Semantic Vector Search vs Keyword Substring
# Java source: HuggingFaceL2Tokenizer.java, SemanticSearchService.java
# Port: Exact 1:1 replication of Java tokenizer & L2 distance logic
# Comparison: BOTH methods take top-5 (no threshold advantage for either)
# =============================================================================
print("[2/5] L2 Vector Search vs Keyword Substring Search")
print("       Java source: HuggingFaceL2Tokenizer.java, SemanticSearchService.java")
print("       Fair comparison: both methods take unfiltered top-5 results\n")

# Catalog: 15 real academic textbooks with realistic metadata text
# This matches how SemanticSearchService.java builds docText on line 38-43:
#   String.format("%s %s %s %s", title, authorOrBrand, location, String.join(" ", tags))
CATALOG = [
    {"id": "res-101", "text": "Designing Data-Intensive Applications Martin Kleppmann SHELF-A1 distributed-systems databases architecture"},
    {"id": "res-102", "text": "MongoDB The Definitive Guide Shannon Bradshaw SHELF-A2 database nosql mongodb"},
    {"id": "res-103", "text": "Spring Boot 3 in Action Craig Walls SHELF-B1 java spring-boot backend"},
    {"id": "res-104", "text": "Operating System Concepts Silberschatz SHELF-B2 operating-systems kernel memory"},
    {"id": "res-105", "text": "Structure and Interpretation of Computer Programs Abelson Sussman SHELF-B3 lisp functional-programming cs-classic"},
    {"id": "res-106", "text": "Database System Concepts Sudarshan SHELF-C1 databases sql transactions"},
    {"id": "res-107", "text": "Clean Code Robert Martin SHELF-C2 clean-code refactoring software-craft"},
    {"id": "res-108", "text": "System Design Interview Alex Xu SHELF-C3 system-design scalability distributed"},
    {"id": "res-109", "text": "Introduction to Algorithms Cormen Leiserson Rivest Stein SHELF-D1 algorithms data-structures cs-theory"},
    {"id": "res-110", "text": "Computer Networking A Top-Down Approach Kurose Ross SHELF-D2 networking protocols tcp-ip"},
    {"id": "res-111", "text": "Artificial Intelligence A Modern Approach Russell Norvig SHELF-D3 ai machine-learning search"},
    {"id": "res-112", "text": "Deep Learning Ian Goodfellow Bengio Courville SHELF-E1 deep-learning neural-networks transformers"},
    {"id": "res-113", "text": "Computer Organization and Design RISC-V Patterson Hennessy SHELF-E2 architecture risc-v hardware"},
    {"id": "res-114", "text": "The Art of Computer Programming Donald Knuth SHELF-E3 algorithms combinatorics sorting"},
    {"id": "res-115", "text": "Compilers Principles Techniques Tools Aho Lam Sethi Ullman SHELF-F1 compilers parsing code-generation"},
]

# Ground-truth: 10 conceptual queries a student might ask, with expected book IDs
# These are hand-labeled: which books SHOULD a good search return?
QUERIES = [
    ("distributed systems fault tolerance replication", {"res-101", "res-108"}),
    ("operating system kernel memory paging", {"res-104"}),
    ("neural networks deep learning backpropagation", {"res-112", "res-111"}),
    ("SQL database transactions indexing", {"res-106", "res-102"}),
    ("compiler parsing syntax tree", {"res-115"}),
    ("functional programming lisp recursion", {"res-105"}),
    ("microservices spring boot REST API", {"res-103"}),
    ("refactoring clean code testing", {"res-107"}),
    ("graph algorithms sorting dynamic programming", {"res-109", "res-114"}),
    ("TCP IP networking protocols routing", {"res-110"}),
]


def tokenize_l2(text):
    """Exact port of HuggingFaceL2Tokenizer.createL2NormalizedVector()"""
    tokens = re.split(r"[^a-zA-Z0-9]+", text.lower())
    freq = defaultdict(float)
    for t in tokens:
        if len(t) >= 2:
            freq[t] += 1.0
    norm = math.sqrt(sum(v * v for v in freq.values()))
    if norm == 0:
        return {}
    return {k: v / norm for k, v in freq.items()}


def l2_similarity(v1, v2):
    """Exact port of HuggingFaceL2Tokenizer.calculateL2SimilarityScore()"""
    all_keys = set(v1) | set(v2)
    dist = math.sqrt(sum((v1.get(k, 0.0) - v2.get(k, 0.0)) ** 2 for k in all_keys))
    return 1.0 / (1.0 + dist)


def keyword_match_score(query_text, doc_text):
    """Simple: count of query terms found in doc text"""
    q_terms = set(query_text.lower().split())
    d_terms = set(re.split(r"[^a-zA-Z0-9]+", doc_text.lower()))
    return len(q_terms & d_terms)


l2_precs, l2_recs = [], []
kw_precs, kw_recs = [], []

for q_text, expected in QUERIES:
    q_vec = tokenize_l2(q_text)

    # L2 Vector Search: rank all docs by L2 similarity, take top 5
    l2_ranked = sorted(
        [(doc["id"], l2_similarity(q_vec, tokenize_l2(doc["text"]))) for doc in CATALOG],
        key=lambda x: x[1], reverse=True
    )
    top5_l2 = set(x[0] for x in l2_ranked[:5])

    # Keyword Substring: rank all docs by keyword overlap count, take top 5
    kw_ranked = sorted(
        [(doc["id"], keyword_match_score(q_text, doc["text"])) for doc in CATALOG],
        key=lambda x: x[1], reverse=True
    )
    top5_kw = set(x[0] for x in kw_ranked[:5])

    # Precision@5 = correct in top 5 / 5
    # Recall@5 = correct in top 5 / total correct
    l2_tp = len(top5_l2 & expected)
    kw_tp = len(top5_kw & expected)

    l2_precs.append(l2_tp / 5.0)
    l2_recs.append(l2_tp / len(expected))
    kw_precs.append(kw_tp / 5.0)
    kw_recs.append(kw_tp / len(expected))

avg_l2_p = sum(l2_precs) / len(l2_precs) * 100
avg_l2_r = sum(l2_recs) / len(l2_recs) * 100
avg_kw_p = sum(kw_precs) / len(kw_precs) * 100
avg_kw_r = sum(kw_recs) / len(kw_recs) * 100

print(f"  Keyword Substring  -> Precision@5: {avg_kw_p:.1f}%  Recall@5: {avg_kw_r:.1f}%")
print(f"  L2 Vector Distance -> Precision@5: {avg_l2_p:.1f}%  Recall@5: {avg_l2_r:.1f}%")
if avg_kw_p > 0:
    print(f"  Precision gain: {((avg_l2_p - avg_kw_p) / avg_kw_p) * 100:+.1f}%")
if avg_kw_r > 0:
    print(f"  Recall gain:    {((avg_l2_r - avg_kw_r) / avg_kw_r) * 100:+.1f}%")
print()


# =============================================================================
# ALGORITHM 3: LRU-K (K=2) Cache vs Standard LRU
# Java source: LruKCacheService.java (com.librarix.cache)
# Live wiring: ResourceService.getResourceById() uses LruKCacheService
# Workload: 200 catalog lookups, 70/30 Zipfian (top 6 items get 70% hits)
# =============================================================================
print("[3/5] LRU-K (K=2) Cache Hit Rate vs Standard LRU")
print("       Java source: LruKCacheService.java")
print("       Live wiring: ResourceService.getResourceById() -> lruKCacheService.get()\n")

item_pool = [f"res-{101 + i}" for i in range(60)]
lookups = []
for _ in range(200):
    if random.random() < 0.70:
        lookups.append(random.choice(item_pool[:6]))   # hot items
    else:
        lookups.append(random.choice(item_pool[6:]))    # cold tail


class StdLRU:
    def __init__(self, cap):
        self.cap = cap
        self.cache = OrderedDict()

    def access(self, key):
        if key in self.cache:
            self.cache.move_to_end(key)
            return True
        if len(self.cache) >= self.cap:
            self.cache.popitem(last=False)
        self.cache[key] = True
        return False


class LRUK:
    """Port of LruKCacheService.java eviction logic"""
    def __init__(self, cap, k=2):
        self.cap = cap
        self.k = k
        self.cache = {}
        self.history = defaultdict(list)
        self.clock = 0

    def access(self, key):
        self.clock += 1
        self.history[key].append(self.clock)
        if len(self.history[key]) > self.k:
            self.history[key] = self.history[key][-self.k:]

        if key in self.cache:
            return True

        if len(self.cache) >= self.cap:
            # Evict entry with largest backward K-distance (oldest Kth access)
            worst_key, worst_ktime = None, float("inf")
            for ck in self.cache:
                h = self.history[ck]
                ktime = h[0] if len(h) >= self.k else 0  # 0 = Instant.MIN equivalent
                if ktime < worst_ktime:
                    worst_ktime = ktime
                    worst_key = ck
            if worst_key:
                del self.cache[worst_key]

        self.cache[key] = True
        return False


std_lru = StdLRU(10)
lruk = LRUK(10, k=2)

std_hits = sum(1 for k in lookups if std_lru.access(k))
lruk_hits = sum(1 for k in lookups if lruk.access(k))

std_rate = std_hits / 200 * 100
lruk_rate = lruk_hits / 200 * 100

print(f"  Standard LRU (cap=10) hit rate: {std_rate:.1f}%  ({std_hits}/200)")
print(f"  LRU-K K=2   (cap=10) hit rate:  {lruk_rate:.1f}%  ({lruk_hits}/200)")
if std_rate > 0:
    print(f"  Hit rate change: {((lruk_rate - std_rate) / std_rate) * 100:+.1f}%")
print()


# =============================================================================
# ALGORITHM 4: RAG Context Retrieval Accuracy
# Java source: LibrarianAssistantService.java -> semanticSearchService.searchSemantic(q, 5)
# What this measures: given a librarian question about a specific book,
# does the L2 vector search retrieve that book in the top-5 context window?
# This is the retrieval step BEFORE the LLM generates a response.
# =============================================================================
print("[4/5] RAG Context Retrieval Accuracy (Vector Search Top-5 Recall)")
print("       Java source: LibrarianAssistantService.askLibrarian()")
print("       Measures: does SemanticSearchService retrieve the correct book in top-5?\n")

# 10 realistic librarian questions, each targeting a specific known book
LIBRARIAN_QUERIES = [
    ("Is Designing Data-Intensive Applications available to borrow?", "res-101"),
    ("Where is the Operating System Concepts textbook located?", "res-104"),
    ("I need a book on deep learning and neural networks", "res-112"),
    ("Do you have the Spring Boot textbook for my Java project?", "res-103"),
    ("I'm looking for a database textbook covering SQL and transactions", "res-106"),
    ("Where can I find the Compilers Dragon Book?", "res-115"),
    ("I need the CLRS algorithms textbook", "res-109"),
    ("Do you have Clean Code by Robert Martin?", "res-107"),
    ("I want a book on artificial intelligence", "res-111"),
    ("Is the computer networking textbook available?", "res-110"),
]

retrieval_hits = 0
for q, target_id in LIBRARIAN_QUERIES:
    q_vec = tokenize_l2(q)
    scores = sorted(
        [(doc["id"], l2_similarity(q_vec, tokenize_l2(doc["text"]))) for doc in CATALOG],
        key=lambda x: x[1], reverse=True
    )
    top5 = [x[0] for x in scores[:5]]
    hit = target_id in top5
    retrieval_hits += int(hit)
    print(f"    Q: \"{q[:60]}...\"  Target: {target_id}  Retrieved: {'YES' if hit else 'MISS'}  (rank {[x[0] for x in scores].index(target_id) + 1})")

retrieval_recall = retrieval_hits / len(LIBRARIAN_QUERIES) * 100
print(f"\n  Context Retrieval Recall (Top-5): {retrieval_recall:.0f}% ({retrieval_hits}/{len(LIBRARIAN_QUERIES)})\n")


# =============================================================================
# ALGORITHM 5: 3D Shelf Slotting Physical Picking Distance
# Java source: ShelfSlottingOptimizer.java (com.librarix.shelf)
# What this measures: total Euclidean walking+reaching distance per pick
# across 10,000 annual borrow operations under Pareto demand skew
# =============================================================================
print("[5/5] 3D Shelf Slotting: Physical Picking Distance Reduction")
print("       Java source: ShelfSlottingOptimizer.java")
print("       Workload: 10,000 annual borrow picks, Pareto demand distribution\n")

# Pareto demand: top items get disproportionate borrow counts
demand = [100.0 / (i + 1) ** 0.8 for i in range(15)]
total_picks = 10000
picks_per_item = [int((w / sum(demand)) * total_picks) for w in demand]

AISLES_Z = [0.0, 2.5, 5.0, 7.5, 10.0, 12.5, 15.0, 17.5, 20.0, 22.5]
LEVELS_Y = [0.4, 1.5, 2.4]  # Bottom, Eye-Level, Top
ENTRANCE = (0.0, 1.5, 0.0)  # Picking entrance point

# (a) Random un-optimized layout: each item assigned random aisle + shelf level
random_total_dist = 0.0
for i in range(15):
    y = random.choice(LEVELS_Y)
    z = random.choice(AISLES_Z)
    dist = math.sqrt((y - ENTRANCE[1]) ** 2 + (z - ENTRANCE[2]) ** 2)
    random_total_dist += dist * picks_per_item[i]

# (b) Optimized layout: top-30% popularity items at eye-level in front aisles
optimized_total_dist = 0.0
for i in range(15):
    if i < 5:  # Top ~33% demand items -> eye level, front aisles
        y = 1.5
        z = (i // 8) * 2.5
    else:  # Lower demand -> alternating bottom/top shelves
        y = 0.4 if i % 2 == 0 else 2.4
        z = (i // 8) * 2.5
    dist = math.sqrt((y - ENTRANCE[1]) ** 2 + (z - ENTRANCE[2]) ** 2)
    optimized_total_dist += dist * picks_per_item[i]

avg_random = random_total_dist / total_picks
avg_optimized = optimized_total_dist / total_picks
dist_reduction = ((avg_random - avg_optimized) / avg_random) * 100

print(f"  Random layout:    {avg_random:.2f} m avg picking distance per borrow")
print(f"  Optimized layout: {avg_optimized:.2f} m avg picking distance per borrow")
print(f"  Distance reduction: {dist_reduction:.1f}%\n")


# =============================================================================
# SUMMARY TABLE
# =============================================================================
print("=" * 100)
print("  SUMMARY: All metrics are Python design simulations (seed=42)")
print("=" * 100)

rows = [
    ("WFQ Urgent High-Tier Rank", f"{avg(fifo_urgent):.0f}/100 (FIFO)", f"{avg(wfq_urgent):.0f}/100 (WFQ)", f"-{pct_change(avg(fifo_urgent), avg(wfq_urgent)):.0f}%"),
    ("WFQ All High-Tier Rank", f"{avg(fifo_all):.0f}/100 (FIFO)", f"{avg(wfq_all):.0f}/100 (WFQ)", f"-{pct_change(avg(fifo_all), avg(wfq_all)):.0f}%"),
    ("Semantic Precision@5", f"{avg_kw_p:.0f}% (Keyword)", f"{avg_l2_p:.0f}% (L2 Vector)", f"+{((avg_l2_p-avg_kw_p)/avg_kw_p)*100:.0f}%" if avg_kw_p > 0 else "N/A"),
    ("Semantic Recall@5", f"{avg_kw_r:.0f}% (Keyword)", f"{avg_l2_r:.0f}% (L2 Vector)", f"{((avg_l2_r-avg_kw_r)/avg_kw_r)*100:+.0f}%" if avg_kw_r > 0 else "N/A"),
    ("LRU-K Cache Hit Rate", f"{std_rate:.0f}% (Std LRU)", f"{lruk_rate:.0f}% (LRU-2)", f"{((lruk_rate-std_rate)/std_rate)*100:+.0f}%" if std_rate > 0 else "N/A"),
    ("RAG Context Retrieval", "0% (No retrieval)", f"{retrieval_recall:.0f}% (L2 Top-5)", f"{retrieval_recall:.0f}% Recall"),
    ("Shelf Pick Distance", f"{avg_random:.1f}m (Random)", f"{avg_optimized:.1f}m (Optimized)", f"-{dist_reduction:.0f}%"),
]

print(f"  {'Metric':<26} {'Baseline':<22} {'LIBRARIX':<22} {'Change':<12}")
print(f"  {'-'*82}")
for m, b, r, c in rows:
    print(f"  {m:<26} {b:<22} {r:<22} {c:<12}")
print("=" * 100)
