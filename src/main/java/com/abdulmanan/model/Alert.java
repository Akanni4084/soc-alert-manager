package com.abdulmanan.model;

import com.abdulmanan.exception.InvalidAlertOperationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.Serializable;

public abstract class Alert implements Serializable {

    private final String id;
    private final String description;
    private final String affectedHost;
    private final LocalDateTime detectedAt;
    private final boolean criticalHost;

    private AlertStatus status;
    private Analyst assignedAnalyst;
    private String conclusion;

    private final List<AlertHistoryEntry> history = new ArrayList<>();

    protected Alert(
            String id,
            String description,
            String affectedHost,
            LocalDateTime detectedAt,
            boolean criticalHost
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant ne doit pas être vide."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "La description ne doit pas être vide."
            );
        }

        if (affectedHost == null || affectedHost.isBlank()) {
            throw new IllegalArgumentException(
                    "La machine concernée ne doit pas être vide."
            );
        }

        this.id = id.strip();
        this.description = description.strip();
        this.affectedHost = affectedHost.strip();
        this.detectedAt = Objects.requireNonNull(
                detectedAt,
                "La date de détection est obligatoire."
        );
        this.criticalHost = criticalHost;
        this.status = AlertStatus.NEW;

        addHistoryEntry("Création de l'alerte.");
    }

    public abstract int calculateRiskScore();

    public Priority getPriority() {
        return Priority.fromScore(calculateRiskScore());
    }

    public void assignTo(Analyst analyst) {
        Objects.requireNonNull(
                analyst,
                "L'analyste est obligatoire."
        );

        if (status != AlertStatus.NEW) {
            throw new InvalidAlertOperationException(
                    "Seule une nouvelle alerte peut être attribuée."
            );
        }

        this.assignedAnalyst = analyst;
        this.status = AlertStatus.ASSIGNED;

        addHistoryEntry("Attribution à " + analyst.getName() + ".");
    }

    public void startInvestigation() {
        if (status != AlertStatus.ASSIGNED) {
            throw new InvalidAlertOperationException(
                    "L'alerte doit être attribuée avant de commencer l'analyse."
            );
        }

        this.status = AlertStatus.IN_PROGRESS;

        addHistoryEntry("Démarrage de l'analyse.");
    }

    public void resolve(String conclusion) {
        close(AlertStatus.RESOLVED, conclusion);
    }

    public void markAsFalsePositive(String conclusion) {
        close(AlertStatus.FALSE_POSITIVE, conclusion);
    }

    private void close(AlertStatus finalStatus, String conclusion) {
        if (status != AlertStatus.IN_PROGRESS) {
            throw new InvalidAlertOperationException(
                    "L'alerte doit être en cours d'analyse pour être clôturée."
            );
        }

        if (conclusion == null || conclusion.isBlank()) {
            throw new IllegalArgumentException(
                    "Une conclusion est obligatoire pour clôturer l'alerte."
            );
        }

        this.conclusion = conclusion.strip();
        this.status = finalStatus;

        addHistoryEntry(
                "Clôture : " + finalStatus
                        + ". Conclusion : " + this.conclusion
        );
    }

    private void addHistoryEntry(String action) {
        history.add(new AlertHistoryEntry(
                LocalDateTime.now(),
                action
        ));
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getAffectedHost() {
        return affectedHost;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public boolean isCriticalHost() {
        return criticalHost;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public Analyst getAssignedAnalyst() {
        return assignedAnalyst;
    }

    public String getConclusion() {
        return conclusion;
    }

    public List<AlertHistoryEntry> getHistory() {
        return List.copyOf(history);
    }
}