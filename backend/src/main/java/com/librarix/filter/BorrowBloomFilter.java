package com.librarix.filter;

import com.librarix.model.Loan;
import com.librarix.model.enums.LoanStatus;
import com.librarix.repository.LoanRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.BitSet;
import java.util.List;

/**
 * Bloom Filter for Duplicate Borrow Detection
 *
 * DSA: Bloom filter — probabilistic data structure using k independent hash functions
 * over a bit array of size m. Guarantees zero false negatives with a tunable false positive rate.
 *
 * Purpose: Before hitting MongoDB to check "has this user already borrowed this book?",
 * first check the Bloom filter. If it says NO → the user definitely hasn't borrowed it,
 * skip the DB query entirely. If it says MAYBE → fall through to the actual DB check.
 *
 * Parameters:
 *   m = 4096 bits (bit array size)
 *   k = 3 hash functions
 *   Expected n ≈ 200 active loans
 *   Theoretical FP rate: (1 − e^(−kn/m))^k ≈ 0.2% for n=200
 *
 * Time Complexity: O(k) per lookup/insert = O(1) constant.
 * Space Complexity: O(m) = 4096 bits = 512 bytes total.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowBloomFilter {

    private final LoanRepository loanRepository;

    private static final int BIT_ARRAY_SIZE = 4096;
    private static final int NUM_HASH_FUNCTIONS = 3;

    private BitSet bitArray = new BitSet(BIT_ARRAY_SIZE);
    private int insertedCount = 0;

    @PostConstruct
    public void initialize() {
        rebuildFilter();
    }

    /**
     * Rebuild the Bloom filter from all active loans in the database.
     */
    public synchronized void rebuildFilter() {
        BitSet newBits = new BitSet(BIT_ARRAY_SIZE);
        int count = 0;

        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            String key = buildKey(loan.getUserId(), loan.getResourceId());
            for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
                int hash = computeHash(key, i);
                newBits.set(hash);
            }
            count++;
        }

        this.bitArray = newBits;
        this.insertedCount = count;
        log.info("Bloom filter rebuilt: {} active loans indexed into {} bits ({} hash functions)",
                count, BIT_ARRAY_SIZE, NUM_HASH_FUNCTIONS);
    }

    /**
     * Add a borrow record to the filter. Called when a new loan is created.
     */
    public synchronized void addBorrow(String userId, String resourceId) {
        String key = buildKey(userId, resourceId);
        for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
            int hash = computeHash(key, i);
            bitArray.set(hash);
        }
        insertedCount++;
    }

    /**
     * Check if a user MIGHT have already borrowed this resource.
     *
     * @return false = DEFINITELY NOT borrowed (skip DB query).
     *         true  = MAYBE borrowed (must verify with DB query).
     */
    public boolean mightHaveBorrowed(String userId, String resourceId) {
        String key = buildKey(userId, resourceId);
        for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
            int hash = computeHash(key, i);
            if (!bitArray.get(hash)) {
                return false; // Guaranteed: this combination is NOT in the filter
            }
        }
        return true; // Maybe — need to verify with DB
    }

    /**
     * Build composite key from userId and resourceId.
     */
    private String buildKey(String userId, String resourceId) {
        return userId + ":" + resourceId;
    }

    /**
     * Compute the i-th hash function for a given key.
     * Uses double hashing: h_i(key) = (h1(key) + i * h2(key)) mod m
     * where h1 = hashCode(), h2 = FNV-1a variant.
     */
    private int computeHash(String key, int i) {
        int h1 = Math.abs(key.hashCode());
        int h2 = fnv1aHash(key);
        return Math.abs((h1 + i * h2) % BIT_ARRAY_SIZE);
    }

    /**
     * FNV-1a hash function — fast, well-distributed hash for strings.
     */
    private int fnv1aHash(String key) {
        int hash = 0x811c9dc5; // FNV offset basis
        for (int j = 0; j < key.length(); j++) {
            hash ^= key.charAt(j);
            hash *= 0x01000193; // FNV prime
        }
        return Math.abs(hash);
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public int getBitArraySize() {
        return BIT_ARRAY_SIZE;
    }

    public int getNumHashFunctions() {
        return NUM_HASH_FUNCTIONS;
    }

    /**
     * Calculate the theoretical false positive rate: (1 − e^(−kn/m))^k
     */
    public double getTheoreticalFalsePositiveRate() {
        double exponent = -1.0 * NUM_HASH_FUNCTIONS * insertedCount / BIT_ARRAY_SIZE;
        return Math.pow(1.0 - Math.exp(exponent), NUM_HASH_FUNCTIONS);
    }
}
