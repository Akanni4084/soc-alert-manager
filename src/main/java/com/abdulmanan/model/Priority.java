package com.abdulmanan.model;

public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static Priority fromScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException(
                    "Le score doit etre compris entre 0 et 100."
            );
        }

        if (score >= 80) {
            return CRITICAL;
        }

        if (score >= 60) {
            return HIGH;
        }

        if (score >= 30) {
            return MEDIUM;
        }

        return LOW;
    }
}