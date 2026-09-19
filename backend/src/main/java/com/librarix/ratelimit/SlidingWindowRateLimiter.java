package com.librarix.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Sliding Window Log Rate Limiter
 *
 * DSA: Sliding window over a deque of timestamps per user.
 * System Design: API rate limiting — classic interview topic at every FAANG company.
 *
 * Algorithm:
 *   1. For each user, maintain a Deque<Long> of request timestamps.
 *   2. On each request: remove timestamps older than (now - windowMs).
 *   3. If deque.size() >= maxRequests → reject (429 Too Many Requests).
 *   4. Otherwise, add current timestamp and allow.
 *
 * Time Complexity: O(1) amortized per request (each timestamp is added once, removed once).
 * Space Complexity: O(n) where n = max requests per window per user.
 *
 * Configuration: 50 requests per 60 seconds per user (configurable).
 */
@Slf4j
@Component
public class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, Deque<Long>> userWindows = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter() {
        this(50, 60_000); // Default: 50 requests per 60 seconds
    }

    public SlidingWindowRateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * Check if a request from the given user should be allowed.
     *
     * @param userId The user identifier (or IP address for unauthenticated requests)
     * @return true if the request is allowed, false if rate-limited
     */
    public boolean allowRequest(String userId) {
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = userWindows.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());

        // Remove timestamps outside the current window
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - windowMs) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            log.warn("Rate limit exceeded for user: {} ({}/{} requests in {}ms window)",
                    userId, timestamps.size(), maxRequests, windowMs);
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    /**
     * Get the number of remaining requests in the current window for a user.
     */
    public int getRemainingRequests(String userId) {
        Deque<Long> timestamps = userWindows.get(userId);
        if (timestamps == null) return maxRequests;

        long now = System.currentTimeMillis();
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - windowMs) {
            timestamps.pollFirst();
        }
        return Math.max(0, maxRequests - timestamps.size());
    }

    /**
     * Get the time in milliseconds until the window resets for a user.
     */
    public long getRetryAfterMs(String userId) {
        Deque<Long> timestamps = userWindows.get(userId);
        if (timestamps == null || timestamps.isEmpty()) return 0;
        long oldest = timestamps.peekFirst();
        return Math.max(0, (oldest + windowMs) - System.currentTimeMillis());
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getWindowMs() {
        return windowMs;
    }
}
