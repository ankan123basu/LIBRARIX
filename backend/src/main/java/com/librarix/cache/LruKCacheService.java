package com.librarix.cache;

import com.librarix.dto.ResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Addition 4 — Algorithm 2: LRU-K Metadata Cache (K = 2)
 *
 * Tracks the history of the last K access timestamps for each resource ID.
 * Evicts the resource with the largest backward K-distance:
 * D_K(x) = t_now - t_access(x, K-th_most_recent)
 *
 * Why LRU-K instead of plain LRU?
 * Plain LRU suffers from cache pollution during bulk catalog searches (one-off access sweeps).
 * LRU-K (K=2) requires an item to be accessed twice before gaining high cache retention priority,
 * ensuring genuinely popular items stay cached.
 */
@Slf4j
@Service
public class LruKCacheService {

    private final int capacity;
    private final int k;
    private final Map<String, CacheEntry> cache;

    public LruKCacheService() {
        this(100, 2); // Default capacity 100, K=2
    }

    public LruKCacheService(int capacity, int k) {
        this.capacity = capacity;
        this.k = k;
        this.cache = new ConcurrentHashMap<>();
    }

    private static class CacheEntry {
        ResourceDTO value;
        List<Instant> accessHistory = new LinkedList<>();

        CacheEntry(ResourceDTO value, Instant initialAccess) {
            this.value = value;
            recordAccess(initialAccess, 2);
        }

        void recordAccess(Instant timestamp, int maxHistory) {
            accessHistory.add(timestamp);
            if (accessHistory.size() > maxHistory) {
                accessHistory.remove(0); // Keep only last K entries
            }
        }

        Instant getKthAccessTimestamp(int k) {
            if (accessHistory.size() < k) {
                return Instant.MIN; // Less than K accesses -> infinite K-distance
            }
            return accessHistory.get(0); // Oldest of the last K accesses
        }
    }

    public synchronized ResourceDTO get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        entry.recordAccess(Instant.now(), k);
        log.debug("LRU-K Cache HIT for resource key: {}", key);
        return entry.value;
    }

    public synchronized void put(String key, ResourceDTO value) {
        Instant now = Instant.now();
        if (cache.containsKey(key)) {
            CacheEntry entry = cache.get(key);
            entry.value = value;
            entry.recordAccess(now, k);
            return;
        }

        if (cache.size() >= capacity) {
            evictLruKEntry(now);
        }

        cache.put(key, new CacheEntry(value, now));
        log.debug("LRU-K Cache PUT for key: {}", key);
    }

    public synchronized void evict(String key) {
        cache.remove(key);
    }

    public synchronized void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    private void evictLruKEntry(Instant now) {
        String candidateKey = null;
        long maxKDistanceMs = -1;

        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            Instant kthAccess = entry.getValue().getKthAccessTimestamp(k);
            long kDistanceMs = kthAccess == Instant.MIN ? Long.MAX_VALUE : Duration.between(kthAccess, now).toMillis();

            if (kDistanceMs > maxKDistanceMs) {
                maxKDistanceMs = kDistanceMs;
                candidateKey = entry.getKey();
            }
        }

        if (candidateKey != null) {
            cache.remove(candidateKey);
            log.info("LRU-K Evicted cache entry for key: {} (K-distance: {}ms)", candidateKey, maxKDistanceMs);
        }
    }
}
