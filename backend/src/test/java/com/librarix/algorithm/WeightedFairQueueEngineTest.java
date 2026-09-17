package com.librarix.algorithm;

import com.librarix.model.ReservationQueueEntry;
import com.librarix.model.User;
import com.librarix.model.enums.UrgencyLevel;
import com.librarix.model.enums.UserTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeightedFairQueueEngineTest {

    private WeightedFairQueueEngine queueEngine;

    @BeforeEach
    void setUp() {
        queueEngine = new WeightedFairQueueEngine();
    }

    @Test
    void testCalculatePriorityScore_WithAgingAndUrgency() {
        User user = User.builder()
                .userTier(UserTier.CAPSTONE) // 1.5x multiplier -> 15 pts
                .build();

        ReservationQueueEntry entry = ReservationQueueEntry.builder()
                .requestedAt(Instant.now().minus(10, ChronoUnit.HOURS)) // 10h * 1.5 = 15 pts
                .urgencyLevel(UrgencyLevel.HIGH) // 25 pts
                .build();

        double score = queueEngine.calculatePriorityScore(entry, user);
        // Expected: 15 (wait) + 25 (urgency) + 15 (tier) = 55.0
        assertEquals(55.0, score, 0.5);
    }

    @Test
    void testSortWaitlist_HighestScoreFirst() {
        ReservationQueueEntry lowScore = ReservationQueueEntry.builder()
                .id("low")
                .calculatedPriorityScore(20.0)
                .requestedAt(Instant.now())
                .build();

        ReservationQueueEntry highScore = ReservationQueueEntry.builder()
                .id("high")
                .calculatedPriorityScore(80.0)
                .requestedAt(Instant.now())
                .build();

        List<ReservationQueueEntry> sorted = queueEngine.sortWaitlist(List.of(lowScore, highScore));
        assertEquals("high", sorted.get(0).getId());
        assertEquals("low", sorted.get(1).getId());
    }
}
