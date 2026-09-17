package com.librarix.ai;

import com.librarix.model.Loan;
import com.librarix.model.Resource;
import com.librarix.repository.LoanRepository;
import com.librarix.repository.ResourceRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Addition 3 — AI Feature 4: Exam Demand Forecasting Service
 *
 * Implements exponential smoothing time-series model (alpha = 0.3, beta = 0.1)
 * predicting demand velocity per resource. Flags assets prone to exam-week spikes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamDemandForecastingService {

    private final LoanRepository loanRepository;
    private final ResourceRepository resourceRepository;

    private static final double ALPHA = 0.3; // Level smoothing coefficient
    private static final double BETA = 0.1;  // Trend smoothing coefficient

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemandForecast {
        private String resourceId;
        private String resourceTitle;
        private double historicalWeeklyAverage;
        private double projectedNextWeekDemand;
        private boolean highDemandRisk;
        private String statusFlag;
    }

    public List<DemandForecast> forecastDemandForExamPeriod() {
        List<Resource> resources = resourceRepository.findAll();
        List<Loan> allLoans = loanRepository.findAll();

        Instant now = Instant.now();
        List<DemandForecast> forecasts = new ArrayList<>();

        for (Resource res : resources) {
            List<Loan> resLoans = allLoans.stream()
                    .filter(l -> l.getResourceId().equals(res.getId()))
                    .toList();

            // Bucket loans by last 4 weeks
            double[] weeklyDemand = new double[4];
            for (Loan loan : resLoans) {
                if (loan.getBorrowDate() != null) {
                    long weeksAgo = ChronoUnit.WEEKS.between(loan.getBorrowDate(), now);
                    if (weeksAgo >= 0 && weeksAgo < 4) {
                        weeklyDemand[(int) weeksAgo]++;
                    }
                }
            }

            // Holt's Linear Exponential Smoothing
            double level = weeklyDemand[3];
            double trend = weeklyDemand[3] - weeklyDemand[2];

            for (int i = 2; i >= 0; i--) {
                double obs = weeklyDemand[i];
                double prevLevel = level;
                level = ALPHA * obs + (1 - ALPHA) * (level + trend);
                trend = BETA * (level - prevLevel) + (1 - BETA) * trend;
            }

            double forecast = Math.max(0.0, level + trend);
            double avg = Arrays.stream(weeklyDemand).average().orElse(0.0);
            boolean risk = forecast > (res.getTotalQuantity() * 0.8);

            forecasts.add(DemandForecast.builder()
                    .resourceId(res.getId())
                    .resourceTitle(res.getTitle())
                    .historicalWeeklyAverage(avg)
                    .projectedNextWeekDemand(forecast)
                    .highDemandRisk(risk)
                    .statusFlag(risk ? "HIGH_DEMAND_SPIKE_RISK" : "NORMAL_CAPACITY")
                    .build());
        }

        return forecasts;
    }
}
