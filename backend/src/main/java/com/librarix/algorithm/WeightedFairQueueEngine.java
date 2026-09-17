package com.librarix.algorithm;

import com.librarix.model.ReservationQueueEntry;
import com.librarix.model.User;
import com.librarix.model.enums.UrgencyLevel;
import com.librarix.model.enums.UserTier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Addition 4 — Algorithm 1: Weighted Fair Reservation Queue Engine
 *
 * Combines:
 * 1. Wait Time Aging: (WaitTimeHours * 1.5) -> continuous linear score boost to prevent starvation
 * 2. Academic Urgency Boost: STANDARD (10), HIGH (25), CRITICAL (50)
 * 3. User Role Weighting: REGULAR (1.0x), CAPSTONE (1.5x), FACULTY (2.0x)
 *
 * Implements explicit Comparator for deterministic waitlist ordering.
 */
@Component
public class WeightedFairQueueEngine implements Comparator<ReservationQueueEntry> {

    private static final double WAIT_TIME_HOUR_MULTIPLIER = 1.5;
    private static final double TIER_BASE_WEIGHT = 10.0;

    public double calculatePriorityScore(ReservationQueueEntry entry, User user) {
        Instant now = Instant.now();
        Instant requestedAt = entry.getRequestedAt() != null ? entry.getRequestedAt() : now;

        long hoursWaiting = Math.max(0, Duration.between(requestedAt, now).toHours());
        double waitTimeScore = hoursWaiting * WAIT_TIME_HOUR_MULTIPLIER;

        UrgencyLevel urgency = entry.getUrgencyLevel() != null ? entry.getUrgencyLevel() : UrgencyLevel.STANDARD;
        double urgencyBoost = urgency.getScoreBoost();

        UserTier tier = (user != null && user.getUserTier() != null) ? user.getUserTier() : UserTier.REGULAR;
        double userTierBoost = tier.getWeightMultiplier() * TIER_BASE_WEIGHT;

        return waitTimeScore + urgencyBoost + userTierBoost;
    }

    public List<ReservationQueueEntry> sortWaitlist(List<ReservationQueueEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        return entries.stream()
                .sorted(this.reversed()) // Higher score comes first
                .toList();
    }

    @Override
    public int compare(ReservationQueueEntry o1, ReservationQueueEntry o2) {
        int scoreCompare = Double.compare(o1.getCalculatedPriorityScore(), o2.getCalculatedPriorityScore());
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        // Break tie using earliest requested timestamp
        return o2.getRequestedAt().compareTo(o1.getRequestedAt());
    }
}
