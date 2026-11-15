package com.abdulmanan.service;

import com.abdulmanan.exception.InvalidAlertOperationException;
import com.abdulmanan.model.Alert;
import com.abdulmanan.model.AlertStatus;
import com.abdulmanan.model.Analyst;
import com.abdulmanan.model.Priority;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class AlertService {

    private final Map<String, Alert> alerts = new LinkedHashMap<>();
    private final Map<String, Analyst> analysts = new LinkedHashMap<>();

    public void addAlert(Alert alert) {
        Objects.requireNonNull(alert, "L'alerte est obligatoire.");

        if (alerts.containsKey(alert.getId())) {
            throw new IllegalArgumentException(
                    "Une alerte possède déjà cet identifiant : " + alert.getId()
            );
        }

        alerts.put(alert.getId(), alert);
    }

    public void addAnalyst(Analyst analyst) {
        Objects.requireNonNull(analyst, "L'analyste est obligatoire.");

        if (analysts.containsKey(analyst.getId())) {
            throw new IllegalArgumentException(
                    "Un analyste possède déjà cet identifiant : " + analyst.getId()
            );
        }

        analysts.put(analyst.getId(), analyst);
    }

    public Alert findAlertById(String id) {
        Alert alert = alerts.get(id);

        if (alert == null) {
            throw new NoSuchElementException(
                    "Alerte introuvable : " + id
            );
        }

        return alert;
    }

    public Analyst findAnalystById(String id) {
        Analyst analyst = analysts.get(id);

        if (analyst == null) {
            throw new NoSuchElementException(
                    "Analyste introuvable : " + id
            );
        }

        return analyst;
    }

    public List<Alert> getAllAlerts() {
        return List.copyOf(alerts.values());
    }

    public List<Analyst> getAllAnalysts() {
        return List.copyOf(analysts.values());
    }

    public long countActiveAlertsForAnalyst(String analystId) {
        Analyst analyst = findAnalystById(analystId);

        return alerts.values().stream()
                .filter(alert -> alert.getAssignedAnalyst() != null)
                .filter(alert -> alert.getAssignedAnalyst()
                        .getId().equals(analyst.getId()))
                .filter(alert -> alert.getStatus() == AlertStatus.ASSIGNED
                        || alert.getStatus() == AlertStatus.IN_PROGRESS)
                .count();
    }

    public void assignAlert(String alertId, String analystId) {
        Alert alert = findAlertById(alertId);
        Analyst analyst = findAnalystById(analystId);

        if (alert.getStatus() != AlertStatus.NEW) {
            throw new InvalidAlertOperationException(
                    "Seule une nouvelle alerte peut être attribuée."
            );
        }

        if (countActiveAlertsForAnalyst(analystId)
                >= analyst.getMaxActiveAlerts()) {
            throw new InvalidAlertOperationException(
                    "L'analyste " + analyst.getName()
                            + " a atteint sa capacité maximale."
            );
        }

        alert.assignTo(analyst);
    }

    public void startInvestigation(String alertId) {
        findAlertById(alertId).startInvestigation();
    }

    public void resolveAlert(String alertId, String conclusion) {
        findAlertById(alertId).resolve(conclusion);
    }

    public void markAsFalsePositive(String alertId, String conclusion) {
        findAlertById(alertId).markAsFalsePositive(conclusion);
    }

    public List<Alert> filterByStatus(AlertStatus status) {
        Objects.requireNonNull(status, "Le statut est obligatoire.");

        return alerts.values().stream()
                .filter(alert -> alert.getStatus() == status)
                .toList();
    }

    public List<Alert> filterByPriority(Priority priority) {
        Objects.requireNonNull(priority, "La priorité est obligatoire.");

        return alerts.values().stream()
                .filter(alert -> alert.getPriority() == priority)
                .toList();
    }
}