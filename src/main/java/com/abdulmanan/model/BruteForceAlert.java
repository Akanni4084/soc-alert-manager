package com.abdulmanan.model;

import java.time.LocalDateTime;

public class BruteForceAlert extends Alert {

    private final int failedAttempts;
    private final boolean successfulLogin;

    public BruteForceAlert(
            String id,
            String description,
            String affectedHost,
            LocalDateTime detectedAt,
            boolean criticalHost,
            int failedAttempts,
            boolean successfulLogin
    ) {
        super(id, description, affectedHost, detectedAt, criticalHost);

        if (failedAttempts <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre de tentatives echouees doit etre positif."
            );
        }

        this.failedAttempts = failedAttempts;
        this.successfulLogin = successfulLogin;
    }

    @Override
    public int calculateRiskScore() {
        int score;

        if (failedAttempts >= 50) {
            score = 60;
        } else if (failedAttempts >= 10) {
            score = 40;
        } else {
            score = 20;
        }

        if (isCriticalHost()) {
            score += 20;
        }

        if (successfulLogin) {
            score += 20;
        }

        return score;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean hasSuccessfulLogin() {
        return successfulLogin;
    }
}