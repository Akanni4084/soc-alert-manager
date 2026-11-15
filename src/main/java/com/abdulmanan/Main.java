package com.abdulmanan;

import com.abdulmanan.model.Alert;
import com.abdulmanan.model.Analyst;
import com.abdulmanan.repository.AlertLoader;
import com.abdulmanan.repository.AnalystLoader;
import com.abdulmanan.repository.ApplicationState;
import com.abdulmanan.repository.BinaryStateRepository;
import com.abdulmanan.repository.CsvAlertLoader;
import com.abdulmanan.repository.CsvAnalystLoader;
import com.abdulmanan.repository.StateRepository;
import com.abdulmanan.service.AlertService;
import com.abdulmanan.ui.ConsoleMenu;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        AlertService service = new AlertService();

        StateRepository repository = new BinaryStateRepository(
                Path.of("data", "soc-state.bin")
        );

        try {
            Optional<ApplicationState> savedState = repository.load();

            ApplicationState state;

            if (savedState.isPresent()) {
                state = savedState.get();
                System.out.println("Sauvegarde précédente restaurée.");
            } else {
                state = loadInitialData();
                System.out.println("Données initiales chargées depuis les CSV.");
            }

            for (Analyst analyst : state.analysts()) {
                service.addAnalyst(analyst);
            }

            for (Alert alert : state.alerts()) {
                service.addAlert(alert);
            }
        } catch (IOException | IllegalArgumentException exception) {
            System.err.println(
                    "Impossible de démarrer : " + exception.getMessage()
            );
            return;
        }

        System.out.println(
                "Données disponibles : "
                        + service.getAllAlerts().size() + " alertes et "
                        + service.getAllAnalysts().size() + " analystes."
        );

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenu menu = new ConsoleMenu(service, scanner);
            menu.run();
        }

        try {
            ApplicationState state = new ApplicationState(
                    service.getAllAlerts(),
                    service.getAllAnalysts()
            );

            repository.save(state);

            System.out.println("Sauvegarde effectuée avec succès.");
        } catch (IOException exception) {
            System.err.println(
                    "Échec de la sauvegarde : " + exception.getMessage()
            );
        }
    }

    private static ApplicationState loadInitialData() throws IOException {
        AlertLoader alertLoader = new CsvAlertLoader();
        AnalystLoader analystLoader = new CsvAnalystLoader();

        return new ApplicationState(
                alertLoader.load("alerts.csv"),
                analystLoader.load("analysts.csv")
        );
    }
}