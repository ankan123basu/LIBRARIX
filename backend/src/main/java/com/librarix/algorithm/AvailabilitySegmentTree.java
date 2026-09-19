package com.librarix.algorithm;

import com.librarix.model.Loan;
import com.librarix.model.enums.LoanStatus;
import com.librarix.repository.LoanRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Segment Tree for Loan Availability Range Queries
 *
 * DSA: Segment tree with point update and range sum query.
 *
 * Problem: "How many books are expected to be returned between day 3 and day 10?"
 * This enables availability forecasting — predicting future stock levels.
 *
 * Each slot in the tree represents one day in the next N days.
 * Value at slot[i] = count of active loans with dueDate falling on day i.
 *
 * Operations:
 *   - rangeSum(l, r): O(log n) — total expected returns between day l and day r
 *   - pointUpdate(i, delta): O(log n) — increment/decrement day i on borrow/return
 *
 * Time Complexity:
 *   - Build: O(n) where n = number of days
 *   - Query: O(log n)
 *   - Update: O(log n)
 *   - vs Naive loop sum: O(n) per query
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilitySegmentTree {

    private final LoanRepository loanRepository;

    private static final int FORECAST_DAYS = 365;  // Next 365 days
    private int[] tree;
    private int n;

    @PostConstruct
    public void initialize() {
        rebuild();
    }

    /**
     * Build the segment tree from all active loans in the database.
     */
    public synchronized void rebuild() {
        this.n = FORECAST_DAYS;
        this.tree = new int[4 * n]; // Standard segment tree size

        int[] dayBuckets = new int[n];
        Instant now = Instant.now();

        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            if (loan.getDueDate() != null) {
                long dayOffset = Duration.between(now, loan.getDueDate()).toDays();
                if (dayOffset >= 0 && dayOffset < n) {
                    dayBuckets[(int) dayOffset]++;
                }
            }
        }

        buildTree(dayBuckets, 1, 0, n - 1);
        log.info("Segment tree built: {} days, {} active loans indexed", n, activeLoans.size());
    }

    private void buildTree(int[] arr, int node, int start, int end) {
        if (start == end) {
            tree[node] = arr[start];
        } else {
            int mid = (start + end) / 2;
            buildTree(arr, 2 * node, start, mid);
            buildTree(arr, 2 * node + 1, mid + 1, end);
            tree[node] = tree[2 * node] + tree[2 * node + 1];
        }
    }

    /**
     * Range sum query: total expected returns between day l and day r (inclusive).
     * O(log n) time complexity.
     *
     * @param l Start day (0-indexed, 0 = today)
     * @param r End day (0-indexed)
     * @return Count of loans due for return in [l, r]
     */
    public int rangeSum(int l, int r) {
        if (l < 0) l = 0;
        if (r >= n) r = n - 1;
        if (l > r) return 0;
        return queryTree(1, 0, n - 1, l, r);
    }

    private int queryTree(int node, int start, int end, int l, int r) {
        if (r < start || end < l) {
            return 0; // Out of range
        }
        if (l <= start && end <= r) {
            return tree[node]; // Fully within range
        }
        int mid = (start + end) / 2;
        return queryTree(2 * node, start, mid, l, r) +
               queryTree(2 * node + 1, mid + 1, end, l, r);
    }

    /**
     * Point update: add delta to day index.
     * Called on borrow (delta = +1 at due date) or early return (delta = -1).
     * O(log n) time complexity.
     *
     * @param dayIndex Day offset from today (0-indexed)
     * @param delta    Amount to add (+1 on borrow, -1 on return)
     */
    public void pointUpdate(int dayIndex, int delta) {
        if (dayIndex < 0 || dayIndex >= n) return;
        updateTree(1, 0, n - 1, dayIndex, delta);
    }

    private void updateTree(int node, int start, int end, int idx, int delta) {
        if (start == end) {
            tree[node] += delta;
        } else {
            int mid = (start + end) / 2;
            if (idx <= mid) {
                updateTree(2 * node, start, mid, idx, delta);
            } else {
                updateTree(2 * node + 1, mid + 1, end, idx, delta);
            }
            tree[node] = tree[2 * node] + tree[2 * node + 1];
        }
    }

    /**
     * Naive O(n) range sum for benchmarking comparison.
     */
    public int naiveRangeSum(int[] arr, int l, int r) {
        int sum = 0;
        for (int i = l; i <= r; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public int getForecastDays() {
        return FORECAST_DAYS;
    }
}
