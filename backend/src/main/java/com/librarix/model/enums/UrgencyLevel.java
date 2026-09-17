package com.librarix.model.enums;

public enum UrgencyLevel {
    STANDARD(10.0),
    HIGH(25.0),
    CRITICAL(50.0);

    private final double scoreBoost;

    UrgencyLevel(double scoreBoost) {
        this.scoreBoost = scoreBoost;
    }

    public double getScoreBoost() {
        return scoreBoost;
    }
}
