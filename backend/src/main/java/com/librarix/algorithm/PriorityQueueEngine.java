package com.librarix.algorithm;

import com.librarix.model.ReservationQueueEntry;
import com.librarix.model.User;
import com.librarix.model.enums.UrgencyLevel;
import com.librarix.model.enums.UserTier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * PriorityQueueEngine
 *
 * Implements a dynamic multi-criteria waitlist allocation score algorithm:
 * Score S = (WaitTimeHours * 1.5) + UrgencyBoost + (UserTierMultiplier * 10)
 *
 * Why dynamic calculation?
 * Standard FIFO queues can cause starvations for urgent capstone/exam requests.
 * However, pure priority queues starve normal users.
 * This engine incorporates wait-time aging so every hour spent in queue continuously raises
 * the entry's score, eventually surpassing static urgency boosts to guarantee fairness.
 */
@Component
public class PriorityQueueEngine {

    private static final double WAIT_TIME_HOUR_MULTIPLIER = 1.5;
    private static final double TIER_BASE_MULTIPLIER = 10.0;

    public double calculatePriorityScore(ReservationQueueEntry entry, User user) {
        Instant now = Instant.now();
        Instant requestedAt = entry.getRequestedAt() != null ? entry.getRequestedAt() : now;

        long hoursWaiting = Math.max(0, Duration.between(requestedAt, now).toHours());

        double waitTimeScore = hoursWaiting * WAIT_TIME_HOUR_MULTIPLIER;
        
        UrgencyLevel urgency = entry.getUrgencyLevel() != null ? entry.getUrgencyLevel() : UrgencyLevel.STANDARD;
        double urgencyBoost = urgency.getScoreBoost();

        UserTier tier = user.getUserTier() != null ? user.getUserTier() : UserTier.REGULAR;
        double userTierBoost = tier.getWeightMultiplier() * TIER_BASE_MULTIPLIER;

        return waitTimeScore + urgencyBoost + userTierBoost;
    }
}
