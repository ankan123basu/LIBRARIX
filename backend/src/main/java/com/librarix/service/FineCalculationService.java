package com.librarix.service;

import com.librarix.model.Loan;
import com.librarix.model.Resource;
import com.librarix.model.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * FineCalculationService — Rule Engine for Overdue Penalties
 *
 * Business Rules:
 * 1. Grace Period: No fine applied if returned within grace period (e.g. 24h post due date).
 * 2. Resource-Specific Rates:
 *    - LAB_KIT / HARDWARE: High daily rate ($5.00/day) due to lab scarcity.
 *    - BOOK / SEMINAR_ROOM: Standard daily rate ($2.00/day).
 * 3. Max Fine Cap: Maximum fine capped at $50.00 per loan instance to prevent uncollectible penalties.
 */
@Service
@RequiredArgsConstructor
public class FineCalculationService {

    @Value("${librarix.fine-rules.base-rate-per-day:2.0}")
    private double baseRatePerDay;

    @Value("${librarix.fine-rules.lab-kit-rate-per-day:5.0}")
    private double labKitRatePerDay;

    @Value("${librarix.fine-rules.max-fine-cap:50.0}")
    private double maxFineCap;

    @Value("${librarix.fine-rules.grace-period-hours:24}")
    private long gracePeriodHours;

    public double calculateFineAmount(Loan loan, Resource resource) {
        Instant now = loan.getReturnDate() != null ? loan.getReturnDate() : Instant.now();
        Instant dueDate = loan.getDueDate();

        if (dueDate == null || now.isBefore(dueDate)) {
            return 0.0;
        }

        long totalOverdueHours = Duration.between(dueDate, now).toHours();

        if (totalOverdueHours <= gracePeriodHours) {
            return 0.0;
        }

        long chargeableHours = totalOverdueHours - gracePeriodHours;
        double chargeableDays = Math.ceil((double) chargeableHours / 24.0);

        double dailyRate = getDailyRateForResource(resource.getType());
        double calculatedFine = chargeableDays * dailyRate;

        return Math.min(calculatedFine, maxFineCap);
    }

    private double getDailyRateForResource(ResourceType type) {
        if (type == ResourceType.LAB_KIT || type == ResourceType.HARDWARE) {
            return labKitRatePerDay;
        }
        return baseRatePerDay;
    }
}
