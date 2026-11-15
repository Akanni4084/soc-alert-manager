package com.abdulmanan.model;

import java.time.LocalDateTime;

public class PhishingAlert extends Alert {

    private final String senderEmail;
    private final boolean linkClicked;
    private final boolean credentialsSubmitted;

    public PhishingAlert(
            String id,
            String description,
            String affectedHost,
            LocalDateTime detectedAt,
            boolean criticalHost,
            String senderEmail,
            boolean linkClicked,
            boolean credentialsSubmitted
    ) {
        super(id, description, affectedHost, detectedAt, criticalHost);

        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "L'adresse de l'expediteur ne doit pas etre vide."
            );
        }

        if (credentialsSubmitted && !linkClicked) {
            throw new IllegalArgumentException(
                    "Dans ce scenario, les identifiants ne peuvent pas "
                            + "etre saisis sans avoir ouvert le lien."
            );
        }

        this.senderEmail = senderEmail.strip();
        this.linkClicked = linkClicked;
        this.credentialsSubmitted = credentialsSubmitted;
    }

    @Override
    public int calculateRiskScore() {
        int score = 20;

        if (isCriticalHost()) {
            score += 20;
        }

        if (linkClicked) {
            score += 20;
        }

        if (credentialsSubmitted) {
            score += 40;
        }

        return score;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public boolean isLinkClicked() {
        return linkClicked;
    }

    public boolean isCredentialsSubmitted() {
        return credentialsSubmitted;
    }
}