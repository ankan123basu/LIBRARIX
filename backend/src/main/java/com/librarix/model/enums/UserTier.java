package com.librarix.model.enums;

public enum UserTier {
    REGULAR(1.0),
    CAPSTONE(1.5),
    FACULTY(2.0);

    private final double weightMultiplier;

    UserTier(double weightMultiplier) {
        this.weightMultiplier = weightMultiplier;
    }

    public double getWeightMultiplier() {
        return weightMultiplier;
    }
}
