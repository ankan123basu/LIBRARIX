package com.librarix.benchmark;

import com.librarix.algorithm.AvailabilitySegmentTree;
import com.librarix.cache.LruKCacheService;
import com.librarix.filter.BorrowBloomFilter;
import com.librarix.graph.LibraryRouteOptimizer;
import com.librarix.ratelimit.SlidingWindowRateLimiter;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Pattern;

/**
 * LIBRARIX Algorithm Benchmark Suite — Java JUnit
 *
 * Micro-benchmark suite evaluating core data structure algorithms.
 * Uses 50 JIT warmup iterations and 20 steady-state median measurement iterations.
 * Includes explicit blackhole accumulation to prevent JVM Dead-Code Elimination (DCE).
 *
 * Disclaimers & Methodology Scope:
 * 1. Micro-benchmarks run in-process inside JUnit, not JMH (Java Microbenchmark Harness).
 * 2. Measurements evaluate isolated algorithms, not end-to-end HTTP request latencies.
 * 3. Relative speedups compare O(p+k) / O(log n) to naive O(n) baselines on a catalog of 60 items.
 */
public class AlgorithmBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 50;
    private static final int MEASURE_ITERATIONS = 20;

    // Blackhole variable to prevent Dead-Code Elimination (DCE) by the JIT compiler
    private static volatile int BLACKHOLE = 0;

    // =========================================================================
    // BENCHMARK 1: Trie Autocomplete vs Regex Scan
    // =========================================================================
    @Test
    void benchmarkTrieVsRegex() {
        System.out.println("\n=== BENCHMARK 1: Trie Autocomplete vs Regex Scan (Warmed JVM) ===");

        String[] titles = {
            "Designing Data-Intensive Applications", "MongoDB The Definitive Guide",
            "Spring Boot in Action", "Operating System Concepts",
            "Structure and Interpretation of Computer Programs",
            "Database System Concepts", "Clean Code", "System Design Interview",
            "Introduction to Algorithms", "Computer Networking A Top-Down Approach",
            "Artificial Intelligence A Modern Approach", "Deep Learning",
            "Computer Organization and Design", "The Art of Computer Programming",
            "Compilers Principles Techniques and Tools", "Design Patterns",
            "Distributed Systems Principles and Paradigms",
            "Computer Architecture A Quantitative Approach",
            "Modern Operating Systems", "Data Structures and Algorithms in Java",
            "Java Concurrency in Practice", "Effective Java",
            "Head First Design Patterns", "Refactoring Improving the Design",
            "The Pragmatic Programmer", "Code Complete",
            "Software Engineering at Google", "Programming Pearls",
            "Cracking the Coding Interview", "Elements of Programming Interviews",
            "Algorithm Design Manual", "Discrete Mathematics and Its Applications",
            "Linear Algebra and Its Applications", "Probability and Statistics",
            "Calculus Early Transcentrals", "Physics for Scientists and Engineers",
            "Digital Design and Computer Architecture", "Computer Graphics Principles",
            "Machine Learning A Probabilistic Perspective",
            "Pattern Recognition and Machine Learning",
            "Natural Language Processing with Python", "Information Retrieval",
            "Web Development with Node.js", "React Up and Running",
            "Learning SQL", "SQL Performance Explained",
            "NoSQL Distilled", "Redis in Action",
            "Kubernetes in Action", "Docker Deep Dive",
            "Site Reliability Engineering", "The Phoenix Project",
            "Accelerate", "Team Topologies", "Domain Driven Design",
            "Building Microservices", "Release It",
            "Fundamentals of Software Architecture", "Software Architecture in Practice",
            "Cloud Native Patterns"
        };

        Map<Character, Object[]> trieRoot = new HashMap<>();
        for (String title : titles) {
            for (String word : title.toLowerCase().split("\\s+")) {
                if (word.length() >= 2) insertTrie(trieRoot, word);
            }
        }

        Random rng = new Random(42);
        List<String> prefixes = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String title = titles[rng.nextInt(titles.length)];
            String[] words = title.toLowerCase().split("\\s+");
            String word = words[rng.nextInt(words.length)];
            int prefixLen = Math.min(2 + rng.nextInt(3), word.length());
            prefixes.add(word.substring(0, prefixLen));
        }

        // --- WARMUP PHASE ---
        int bh = 0;
        for (int w = 0; w < WARMUP_ITERATIONS; w++) {
            for (String prefix : prefixes) bh += searchTrie(trieRoot, prefix);
            for (String prefix : prefixes) {
                Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix) + ".*", Pattern.CASE_INSENSITIVE);
                for (String title : titles) {
                    for (String word : title.toLowerCase().split("\\s+")) {
                        if (pattern.matcher(word).matches()) bh++;
                    }
                }
            }
        }
        BLACKHOLE += bh;

        // --- MEASUREMENT PHASE ---
        long[] trieTimes = new long[MEASURE_ITERATIONS];
        long[] regexTimes = new long[MEASURE_ITERATIONS];
        int trieResults = 0, regexResults = 0;

        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long t0 = System.nanoTime();
            int tr = 0;
            for (String prefix : prefixes) tr += searchTrie(trieRoot, prefix);
            trieTimes[i] = System.nanoTime() - t0;
            trieResults = tr;

            long r0 = System.nanoTime();
            int rr = 0;
            for (String prefix : prefixes) {
                Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix) + ".*", Pattern.CASE_INSENSITIVE);
                for (String title : titles) {
                    for (String word : title.toLowerCase().split("\\s+")) {
                        if (pattern.matcher(word).matches()) rr++;
                    }
                }
            }
            regexTimes[i] = System.nanoTime() - r0;
            regexResults = rr;
        }

        Arrays.sort(trieTimes);
        Arrays.sort(regexTimes);
        long medianTrieNs = trieTimes[MEASURE_ITERATIONS / 2];
        long medianRegexNs = regexTimes[MEASURE_ITERATIONS / 2];

        double trieMs = medianTrieNs / 1_000_000.0;
        double regexMs = medianRegexNs / 1_000_000.0;
        double speedup = (double) medianRegexNs / medianTrieNs;

        System.out.printf("  Warmup: %d iterations | Measured: %d iterations (Median)%n", WARMUP_ITERATIONS, MEASURE_ITERATIONS);
        System.out.printf("  Trie lookup median:  %.3f ms for 1000 queries (%.0f ns/query)%n", trieMs, medianTrieNs / 1000.0);
        System.out.printf("  Regex scan median:   %.3f ms for 1000 queries (%.0f ns/query)%n", regexMs, medianRegexNs / 1000.0);
        System.out.printf("  Speedup:             %.1fx faster%n", speedup);
        System.out.printf("  Trie results: %d  |  Regex results: %d%n", trieResults, regexResults);
    }

    private void insertTrie(Map<Character, Object[]> root, String word) {
        Map<Character, Object[]> current = root;
        for (char c : word.toCharArray()) {
            current.computeIfAbsent(c, k -> new Object[]{new HashMap<Character, Object[]>(), false});
            Object[] node = current.get(c);
            current = (Map<Character, Object[]>) node[0];
        }
    }

    private int searchTrie(Map<Character, Object[]> root, String prefix) {
        Map<Character, Object[]> current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.containsKey(c)) return 0;
            current = (Map<Character, Object[]>) current.get(c)[0];
        }
        return countNodes(current);
    }

    private int countNodes(Map<Character, Object[]> node) {
        int count = node.size();
        for (Object[] child : node.values()) {
            count += countNodes((Map<Character, Object[]>) child[0]);
        }
        return count;
    }

    // =========================================================================
    // BENCHMARK 2: Bloom Filter DB Query Elimination
    // =========================================================================
    @Test
    void benchmarkBloomFilter() {
        System.out.println("\n=== BENCHMARK 2: Bloom Filter — DB Query Elimination ===");

        Random rng = new Random(42);
        Set<String> activeBorrows = new HashSet<>();
        List<String> userIds = new ArrayList<>();
        List<String> resourceIds = new ArrayList<>();
        for (int i = 0; i < 20; i++) userIds.add("user-" + i);
        for (int i = 0; i < 60; i++) resourceIds.add("res-" + i);

        java.util.BitSet bloomBits = new java.util.BitSet(4096);
        for (int i = 0; i < 100; i++) {
            String key = userIds.get(rng.nextInt(20)) + ":" + resourceIds.get(rng.nextInt(60));
            activeBorrows.add(key);
            for (int h = 0; h < 3; h++) {
                bloomBits.set(Math.abs((key.hashCode() + h * fnv1a(key)) % 4096));
            }
        }

        int dbQueriesWithFilter = 0;
        int dbQueriesWithout = 0;
        int falsePositives = 0;
        int trueNegatives = 0;

        for (int i = 0; i < 500; i++) {
            String key;
            if (rng.nextDouble() < 0.6) {
                List<String> borrowList = new ArrayList<>(activeBorrows);
                key = borrowList.get(rng.nextInt(borrowList.size()));
            } else {
                key = userIds.get(rng.nextInt(20)) + ":" + resourceIds.get(rng.nextInt(60));
            }

            dbQueriesWithout++;

            boolean bloomSaysMaybe = true;
            for (int h = 0; h < 3; h++) {
                if (!bloomBits.get(Math.abs((key.hashCode() + h * fnv1a(key)) % 4096))) {
                    bloomSaysMaybe = false;
                    break;
                }
            }

            if (bloomSaysMaybe) {
                dbQueriesWithFilter++;
                if (!activeBorrows.contains(key)) falsePositives++;
            } else {
                trueNegatives++;
            }
        }

        double eliminationRate = ((dbQueriesWithout - dbQueriesWithFilter) / (double) dbQueriesWithout) * 100;
        double fpRate = (falsePositives / (double) (falsePositives + trueNegatives)) * 100;

        System.out.printf("  Active loans indexed: %d%n", activeBorrows.size());
        System.out.printf("  Borrow attempts: 500 (60%% duplicate, 40%% new)%n");
        System.out.printf("  DB queries WITHOUT Bloom filter: %d%n", dbQueriesWithout);
        System.out.printf("  DB queries WITH Bloom filter:    %d%n", dbQueriesWithFilter);
        System.out.printf("  DB queries eliminated:           %.1f%%%n", eliminationRate);
        System.out.printf("  True negatives (skipped safely): %d%n", trueNegatives);
        System.out.printf("  False positives:                 %d (%.1f%% FP rate)%n", falsePositives, fpRate);
    }

    private int fnv1a(String key) {
        int hash = 0x811c9dc5;
        for (int i = 0; i < key.length(); i++) {
            hash ^= key.charAt(i);
            hash *= 0x01000193;
        }
        return Math.abs(hash);
    }

    // =========================================================================
    // BENCHMARK 3: Segment Tree vs Naive Loop — Range Query Speed
    // =========================================================================
    @Test
    void benchmarkSegmentTree() {
        System.out.println("\n=== BENCHMARK 3: Segment Tree vs Naive Loop — Range Query (Warmed JVM) ===");

        Random rng = new Random(42);
        int n = 365;
        int[] dayBuckets = new int[n];

        for (int i = 0; i < 500; i++) {
            dayBuckets[rng.nextInt(n)]++;
        }

        int[] tree = new int[4 * n];
        buildSegTree(dayBuckets, tree, 1, 0, n - 1);

        int[][] queries = new int[10_000][2];
        for (int i = 0; i < 10_000; i++) {
            int l = rng.nextInt(n);
            int r = l + rng.nextInt(n - l);
            queries[i] = new int[]{l, r};
        }

        // --- WARMUP PHASE ---
        int bh = 0;
        for (int w = 0; w < WARMUP_ITERATIONS; w++) {
            for (int[] q : queries) bh += querySegTree(tree, 1, 0, n - 1, q[0], q[1]);
            for (int[] q : queries) {
                for (int i = q[0]; i <= q[1]; i++) bh += dayBuckets[i];
            }
        }
        BLACKHOLE += bh;

        // --- MEASUREMENT PHASE ---
        long[] treeTimes = new long[MEASURE_ITERATIONS];
        long[] naiveTimes = new long[MEASURE_ITERATIONS];
        long treeSum = 0, naiveSum = 0;

        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long t0 = System.nanoTime();
            long ts = 0;
            for (int[] q : queries) ts += querySegTree(tree, 1, 0, n - 1, q[0], q[1]);
            treeTimes[i] = System.nanoTime() - t0;
            treeSum = ts;

            long n0 = System.nanoTime();
            long ns = 0;
            for (int[] q : queries) {
                for (int j = q[0]; j <= q[1]; j++) ns += dayBuckets[j];
            }
            naiveTimes[i] = System.nanoTime() - n0;
            naiveSum = ns;
        }

        Arrays.sort(treeTimes);
        Arrays.sort(naiveTimes);
        long medianTreeNs = treeTimes[MEASURE_ITERATIONS / 2];
        long medianNaiveNs = naiveTimes[MEASURE_ITERATIONS / 2];

        double treeMs = medianTreeNs / 1_000_000.0;
        double naiveMs = medianNaiveNs / 1_000_000.0;
        double speedup = (double) medianNaiveNs / medianTreeNs;

        System.out.printf("  Warmup: %d iterations | Measured: %d iterations (Median)%n", WARMUP_ITERATIONS, MEASURE_ITERATIONS);
        System.out.printf("  Segment tree median: %.3f ms (checksum: %d)%n", treeMs, treeSum);
        System.out.printf("  Naive loop median:   %.3f ms (checksum: %d)%n", naiveMs, naiveSum);
        System.out.printf("  Speedup:             %.1fx faster%n", speedup);
        System.out.printf("  Checksums match:     %s%n", treeSum == naiveSum ? "YES ✓" : "NO ✗ — BUG!");
    }

    private void buildSegTree(int[] arr, int[] tree, int node, int s, int e) {
        if (s == e) { tree[node] = arr[s]; return; }
        int mid = (s + e) / 2;
        buildSegTree(arr, tree, 2 * node, s, mid);
        buildSegTree(arr, tree, 2 * node + 1, mid + 1, e);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    private int querySegTree(int[] tree, int node, int s, int e, int l, int r) {
        if (r < s || e < l) return 0;
        if (l <= s && e <= r) return tree[node];
        int mid = (s + e) / 2;
        return querySegTree(tree, 2 * node, s, mid, l, r) +
               querySegTree(tree, 2 * node + 1, mid + 1, e, l, r);
    }

    // =========================================================================
    // BENCHMARK 4: WFQ Priority Queue — Rank Improvement
    // =========================================================================
    @Test
    void benchmarkWfqPriorityQueue() {
        System.out.println("\n=== BENCHMARK 4: Weighted Fair Queue vs FIFO ===");

        Random rng = new Random(42);
        double[] urgencyBoosts = {10.0, 25.0, 50.0};
        String[] urgencyNames = {"STANDARD", "HIGH", "CRITICAL"};
        int[] urgencyWeights = {70, 20, 10};
        double[] tierMults = {1.0, 1.5, 2.0};
        String[] tierNames = {"REGULAR", "CAPSTONE", "FACULTY"};
        int[] tierWeights = {60, 30, 10};

        record QueueEntry(int joinIdx, String tier, String urgency, double score) {}

        List<QueueEntry> entries = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int tIdx = weightedChoice(tierWeights, rng);
            int uIdx = weightedChoice(urgencyWeights, rng);
            double waitH = rng.nextDouble() * 48.0;
            double score = (waitH * 1.5) + urgencyBoosts[uIdx] + (tierMults[tIdx] * 10.0);
            entries.add(new QueueEntry(i, tierNames[tIdx], urgencyNames[uIdx], score));
        }

        List<QueueEntry> fifo = new ArrayList<>(entries);
        fifo.sort(Comparator.comparingInt(QueueEntry::joinIdx));
        List<QueueEntry> wfq = new ArrayList<>(entries);
        wfq.sort(Comparator.comparingDouble(QueueEntry::score).reversed());

        var fifoBroad = new ArrayList<Integer>();
        var wfqBroad = new ArrayList<Integer>();
        var fifoCritical = new ArrayList<Integer>();
        var wfqCritical = new ArrayList<Integer>();

        for (int i = 0; i < 100; i++) {
            QueueEntry fe = fifo.get(i), we = wfq.get(i);
            // Broad urgent tier (Capstone/Faculty + High/Critical)
            if ((fe.tier.equals("CAPSTONE") || fe.tier.equals("FACULTY")) &&
                (fe.urgency.equals("HIGH") || fe.urgency.equals("CRITICAL")))
                fifoBroad.add(i + 1);
            if ((we.tier.equals("CAPSTONE") || we.tier.equals("FACULTY")) &&
                (we.urgency.equals("HIGH") || we.urgency.equals("CRITICAL")))
                wfqBroad.add(i + 1);

            // Critical tier (Faculty + Critical)
            if (fe.tier.equals("FACULTY") && fe.urgency.equals("CRITICAL"))
                fifoCritical.add(i + 1);
            if (we.tier.equals("FACULTY") && we.urgency.equals("CRITICAL"))
                wfqCritical.add(i + 1);
        }

        double fifoBroadAvg = fifoBroad.stream().mapToInt(x -> x).average().orElse(0);
        double wfqBroadAvg = wfqBroad.stream().mapToInt(x -> x).average().orElse(0);
        double broadImprovement = ((fifoBroadAvg - wfqBroadAvg) / fifoBroadAvg) * 100;

        double fifoCritAvg = fifoCritical.stream().mapToInt(x -> x).average().orElse(0);
        double wfqCritAvg = wfqCritical.stream().mapToInt(x -> x).average().orElse(0);
        double critImprovement = fifoCritAvg > 0 ? ((fifoCritAvg - wfqCritAvg) / fifoCritAvg) * 100 : 0;

        System.out.printf("  Population: 100 entries (60%% Regular, 30%% Capstone, 10%% Faculty)%n");
        System.out.printf("  Formula: S = (waitH * 1.5) + urgencyBoost + (tierMult * 10.0)%n");
        System.out.printf("  Broad Urgent Group (Capstone/Faculty + High/Critical, N=%d entries):%n", fifoBroad.size());
        System.out.printf("    FIFO avg rank: %.1f / 100  |  WFQ avg rank: %.1f / 100  |  Improvement: %.1f%%%n",
                fifoBroadAvg, wfqBroadAvg, broadImprovement);
        System.out.printf("  Top Critical Tier (Faculty + Critical, N=%d entries):%n", fifoCritical.size());
        System.out.printf("    FIFO avg rank: %.1f / 100  |  WFQ avg rank: %.1f / 100  |  Improvement: %.1f%%%n",
                fifoCritAvg, wfqCritAvg, critImprovement);
    }

    // =========================================================================
    // BENCHMARK 5: LRU-K (K=2) vs Standard LRU — Cache Hit Rate
    // =========================================================================
    @Test
    void benchmarkLruKCache() {
        System.out.println("\n=== BENCHMARK 5: LRU-K (K=2) vs Standard LRU ===");

        Random rng = new Random(42);
        String[] pool = new String[60];
        for (int i = 0; i < 60; i++) pool[i] = "res-" + (101 + i);

        String[] lookups = new String[200];
        for (int i = 0; i < 200; i++) {
            lookups[i] = rng.nextDouble() < 0.70 ? pool[rng.nextInt(6)] : pool[6 + rng.nextInt(54)];
        }

        LinkedHashMap<String, Boolean> lru = new LinkedHashMap<>(16, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<String, Boolean> e) { return size() > 10; }
        };
        int stdHits = 0;
        for (String key : lookups) {
            if (lru.containsKey(key)) stdHits++;
            lru.put(key, true);
        }

        Map<String, Boolean> lrukCache = new LinkedHashMap<>();
        Map<String, List<Integer>> lrukHistory = new LinkedHashMap<>();
        int clock = 0, lrukHits = 0;
        for (String key : lookups) {
            clock++;
            lrukHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(clock);
            List<Integer> h = lrukHistory.get(key);
            if (h.size() > 2) h.remove(0);

            if (lrukCache.containsKey(key)) { lrukHits++; continue; }

            if (lrukCache.size() >= 10) {
                String worst = null; int worstKtime = Integer.MAX_VALUE;
                for (String ck : lrukCache.keySet()) {
                    List<Integer> ch = lrukHistory.getOrDefault(ck, List.of());
                    int ktime = ch.size() >= 2 ? ch.get(0) : 0;
                    if (ktime < worstKtime) { worstKtime = ktime; worst = ck; }
                }
                if (worst != null) lrukCache.remove(worst);
            }
            lrukCache.put(key, true);
        }

        double stdRate = stdHits / 200.0 * 100;
        double lrukRate = lrukHits / 200.0 * 100;

        System.out.printf("  Workload: 200 lookups, Zipfian (70%% hot / 30%% cold), cap=10%n");
        System.out.printf("  Standard LRU hit rate: %.1f%% (%d/200)%n", stdRate, stdHits);
        System.out.printf("  LRU-K (K=2) hit rate:  %.1f%% (%d/200)%n", lrukRate, lrukHits);
        System.out.printf("  Hit rate change: %+.1f%%%n", ((lrukRate - stdRate) / stdRate) * 100);
    }

    // =========================================================================
    // BENCHMARK 6: Dijkstra Route Optimization
    // =========================================================================
    @Test
    void benchmarkDijkstraRoute() {
        System.out.println("\n=== BENCHMARK 6: Dijkstra Route Optimization ===");

        LibraryRouteOptimizer optimizer = new LibraryRouteOptimizer();
        String[] zones = {"SHELF-A", "SHELF-B", "SHELF-C", "SHELF-D", "SHELF-E", "SHELF-F",
                          "SHELF-G", "SHELF-H", "SHELF-I", "SHELF-J", "SHELF-K", "SHELF-L"};

        Random rng = new Random(42);
        double totalOptimized = 0, totalNaive = 0;
        int trials = 100;
        for (int t = 0; t < trials; t++) {
            int pickupCount = 3 + rng.nextInt(3);
            List<String> locations = new ArrayList<>();
            Set<String> used = new HashSet<>();
            for (int i = 0; i < pickupCount; i++) {
                String z;
                do { z = zones[rng.nextInt(zones.length)]; } while (used.contains(z));
                used.add(z);
                locations.add(z);
            }

            LibraryRouteOptimizer.RouteResult result = optimizer.computeOptimalRoute(locations);
            double naive = optimizer.computeNaiveRouteDistance(locations);
            totalOptimized += result.totalDistanceMeters();
            totalNaive += naive;
        }

        double avgOpt = totalOptimized / trials;
        double avgNaive = totalNaive / trials;
        double reduction = ((avgNaive - avgOpt) / avgNaive) * 100;

        System.out.printf("  Trials: %d random multi-book pickups (3-5 books each)%n", trials);
        System.out.printf("  Naive (alphabetical) avg distance:    %.1f m%n", avgNaive);
        System.out.printf("  Dijkstra (optimized) avg distance:    %.1f m%n", avgOpt);
        System.out.printf("  Distance reduction:                   %.1f%%%n", reduction);
    }

    // =========================================================================
    // BENCHMARK 7: Sliding Window Rate Limiter — Correctness
    // =========================================================================
    @Test
    void benchmarkRateLimiter() {
        System.out.println("\n=== BENCHMARK 7: Sliding Window Rate Limiter ===");

        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(50, 60_000);
        String userId = "test-user-1";

        int accepted = 0, rejected = 0;
        for (int i = 0; i < 100; i++) {
            if (limiter.allowRequest(userId)) accepted++;
            else rejected++;
        }

        System.out.printf("  Config: %d requests per %d ms window%n", 50, 60_000);
        System.out.printf("  Burst: 100 requests sent instantly%n");
        System.out.printf("  Accepted: %d (expected: 50)%n", accepted);
        System.out.printf("  Rejected: %d (expected: 50)%n", rejected);
        System.out.printf("  Enforcement accuracy: %s%n",
                accepted == 50 && rejected == 50 ? "PERFECT ✓" : "IMPRECISE — check window logic");
    }

    private int weightedChoice(int[] weights, Random rng) {
        int total = 0;
        for (int w : weights) total += w;
        int r = rng.nextInt(total);
        int cum = 0;
        for (int i = 0; i < weights.length; i++) {
            cum += weights[i];
            if (r < cum) return i;
        }
        return weights.length - 1;
    }
}
