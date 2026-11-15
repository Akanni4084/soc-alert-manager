package com.abdulmanan.ui;

import com.abdulmanan.exception.InvalidAlertOperationException;
import com.abdulmanan.model.Alert;
import com.abdulmanan.model.AlertHistoryEntry;
import com.abdulmanan.model.AlertStatus;
import com.abdulmanan.model.Analyst;
import com.abdulmanan.model.Priority;
import com.abdulmanan.service.AlertService;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

public class ConsoleMenu {

    private final AlertService service;
    private final Scanner scanner;

    public ConsoleMenu(AlertService service, Scanner scanner) {
        this.service = Objects.requireNonNull(service);
        this.scanner = Objects.requireNonNull(scanner);
    }

    public void run() {
        boolean running = true;

        while (running) {
            displayMenu();

            if (!scanner.hasNextLine()) {
                System.out.println("\nFin de la saisie.");
                return;
            }

            String choice = scanner.nextLine().strip();

            try {
                switch (choice) {
                    case "1" -> displayAlerts(service.getAllAlerts());
                    case "2" -> displayAlertDetails();
                    case "3" -> displayAnalysts();
                    case "4" -> assignAlert();
                    case "5" -> startInvestigation();
                    case "6" -> resolveAlert();
                    case "7" -> markAsFalsePositive();
                    case "8" -> displayHistory();
                    case "9" -> filterByStatus();
                    case "10" -> filterByPriority();
                    case "11" -> displayDashboard();

                    case "0" -> {
                        running = false;
                        System.out.println("À bientôt !");
                    }

                    default -> System.out.println(
                            "Choix inconnu. Réessaie."
                    );
                }
            } catch (InvalidAlertOperationException
                     | IllegalArgumentException exception) {
                System.out.println(
                        "Opération refusée : " + exception.getMessage()
                );
            } catch (NoSuchElementException exception) {
                if (!scanner.hasNextLine()) {
                    System.out.println("Fin de la saisie.");
                    return;
                }

                System.out.println(
                        "Recherche impossible : " + exception.getMessage()
                );
            }
        }
    }

    private void displayMenu() {
        System.out.println();
        System.out.println("========== SOC ALERT MANAGER ==========");
        System.out.println("1. Afficher toutes les alertes");
        System.out.println("2. Consulter une alerte");
        System.out.println("3. Afficher les analystes et leur charge");
        System.out.println("4. Attribuer une alerte");
        System.out.println("5. Démarrer une analyse");
        System.out.println("6. Résoudre une alerte");
        System.out.println("7. Classer une alerte comme faux positif");
        System.out.println("8. Consulter l'historique d'une alerte");
        System.out.println("9. Filtrer par statut");
        System.out.println("10. Filtrer par priorité");
        System.out.println("11. Afficher le tableau de bord");
        System.out.println("0. Quitter");
        System.out.print("Ton choix : ");
    }

    private String readRequired(String prompt) {
        System.out.print(prompt);

        String value = scanner.nextLine().strip();

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "La saisie ne doit pas être vide."
            );
        }

        return value;
    }

    private String readAlertId() {
        return readRequired("Identifiant de l'alerte : ")
                .toUpperCase(Locale.ROOT);
    }

    private void displayAlerts(List<Alert> alerts) {
        if (alerts.isEmpty()) {
            System.out.println("Aucune alerte trouvée.");
            return;
        }

        System.out.println();

        for (Alert alert : alerts) {
            System.out.println(
                    alert.getId()
                            + " | " + alert.getPriority()
                            + " | " + alert.getStatus()
                            + " | " + alert.getAffectedHost()
                            + " | " + alert.getDescription()
            );
        }
    }

    private void displayAlertDetails() {
        Alert alert = service.findAlertById(readAlertId());
        Analyst analyst = alert.getAssignedAnalyst();

        System.out.println();
        System.out.println("Identifiant : " + alert.getId());
        System.out.println(
                "Type : " + alert.getClass().getSimpleName()
        );
        System.out.println("Description : " + alert.getDescription());
        System.out.println("Machine : " + alert.getAffectedHost());
        System.out.println("Détectée le : " + alert.getDetectedAt());
        System.out.println(
                "Machine sensible : "
                        + (alert.isCriticalHost() ? "Oui" : "Non")
        );
        System.out.println("Score : " + alert.calculateRiskScore());
        System.out.println("Priorité : " + alert.getPriority());
        System.out.println("Statut : " + alert.getStatus());

        System.out.println(
                "Analyste : "
                        + (analyst == null
                        ? "Non attribuée"
                        : analyst.getName())
        );

        System.out.println(
                "Conclusion : "
                        + (alert.getConclusion() == null
                        ? "Aucune"
                        : alert.getConclusion())
        );
    }

    private void displayAnalysts() {
        if (service.getAllAnalysts().isEmpty()) {
            System.out.println("Aucun analyste enregistré.");
            return;
        }

        for (Analyst analyst : service.getAllAnalysts()) {
            long activeAlerts = service.countActiveAlertsForAnalyst(
                    analyst.getId()
            );

            System.out.println(
                    analyst.getId()
                            + " | " + analyst.getName()
                            + " | Charge : " + activeAlerts
                            + "/" + analyst.getMaxActiveAlerts()
            );
        }
    }

    private void assignAlert() {
        String alertId = readAlertId();

        displayAnalysts();

        String analystId = readRequired("Identifiant de l'analyste : ")
                .toUpperCase(Locale.ROOT);

        service.assignAlert(alertId, analystId);

        System.out.println("Alerte attribuée avec succès.");
    }

    private void startInvestigation() {
        service.startInvestigation(readAlertId());

        System.out.println("Analyse démarrée.");
    }

    private void resolveAlert() {
        String alertId = readAlertId();
        String conclusion = readRequired("Conclusion de l'analyse : ");

        service.resolveAlert(alertId, conclusion);

        System.out.println("Alerte résolue.");
    }

    private void markAsFalsePositive() {
        String alertId = readAlertId();
        String conclusion = readRequired("Justification du faux positif : ");

        service.markAsFalsePositive(alertId, conclusion);

        System.out.println("Alerte classée comme faux positif.");
    }

    private void displayHistory() {
        Alert alert = service.findAlertById(readAlertId());

        System.out.println(
                "=== Historique de " + alert.getId() + " ==="
        );

        for (AlertHistoryEntry entry : alert.getHistory()) {
            System.out.println(entry);
        }
    }

    private void filterByStatus() {
        System.out.println(
                "Statuts disponibles : NEW, ASSIGNED, IN_PROGRESS, "
                        + "RESOLVED, FALSE_POSITIVE"
        );

        String input = readRequired("Statut : ")
                .toUpperCase(Locale.ROOT);

        AlertStatus status = AlertStatus.valueOf(input);

        displayAlerts(service.filterByStatus(status));
    }

    private void filterByPriority() {
        System.out.println(
                "Priorités disponibles : LOW, MEDIUM, HIGH, CRITICAL"
        );

        String input = readRequired("Priorité : ")
                .toUpperCase(Locale.ROOT);

        Priority priority = Priority.valueOf(input);

        displayAlerts(service.filterByPriority(priority));
    }

    private void displayDashboard() {
        List<Alert> alerts = service.getAllAlerts();

        long openAlerts = alerts.stream()
                .filter(this::isOpen)
                .count();

        long criticalOpenAlerts = alerts.stream()
                .filter(this::isOpen)
                .filter(alert -> alert.getPriority() == Priority.CRITICAL)
                .count();

        System.out.println();
        System.out.println("========== TABLEAU DE BORD ==========");
        System.out.println("Total des alertes : " + alerts.size());
        System.out.println("Alertes ouvertes : " + openAlerts);
        System.out.println(
                "Alertes critiques ouvertes : " + criticalOpenAlerts
        );

        for (AlertStatus status : AlertStatus.values()) {
            System.out.println(
                    status + " : " + service.filterByStatus(status).size()
            );
        }

        System.out.println();
        System.out.println("=== Charge des analystes ===");
        displayAnalysts();
    }

    private boolean isOpen(Alert alert) {
        return alert.getStatus() != AlertStatus.RESOLVED
                && alert.getStatus() != AlertStatus.FALSE_POSITIVE;
    }
}