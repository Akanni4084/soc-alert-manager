package com.abdulmanan.repository;

import com.abdulmanan.model.Alert;
import com.abdulmanan.model.BruteForceAlert;
import com.abdulmanan.model.MalwareAlert;
import com.abdulmanan.model.PhishingAlert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CsvAlertLoader implements AlertLoader {

    private static final String HEADER =
            "type;id;description;affectedHost;detectedAt;"
                    + "criticalHost;detail;flag1;flag2";

    @Override
    public List<Alert> load(String resourceName) throws IOException {
        InputStream input = CsvAlertLoader.class
                .getResourceAsStream("/" + resourceName);

        if (input == null) {
            throw new IOException(
                    "Fichier de données introuvable : " + resourceName
            );
        }

        List<Alert> alerts = new ArrayList<>();
        Set<String> identifiers = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8)
        )) {
            String header = reader.readLine();

            if (!HEADER.equals(header)) {
                throw new IOException(
                        "L'en-tête du fichier CSV est incorrect."
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                try {
                    Alert alert = parseLine(line);

                    if (!identifiers.add(alert.getId())) {
                        throw new IllegalArgumentException(
                                "Identifiant en double : " + alert.getId()
                        );
                    }

                    alerts.add(alert);
                } catch (IllegalArgumentException
                         | java.time.DateTimeException exception) {
                    throw new IOException(
                            "Erreur à la ligne " + lineNumber
                                    + " : " + exception.getMessage(),
                            exception
                    );
                }
            }
        }

        return alerts;
    }

    private Alert parseLine(String line) {
        String[] columns = line.split(";", -1);

        if (columns.length != 9) {
            throw new IllegalArgumentException(
                    "Chaque ligne doit contenir exactement 9 colonnes."
            );
        }

        for (int index = 0; index < columns.length; index++) {
            columns[index] = columns[index].strip();
        }

        String type = columns[0];
        String id = columns[1];
        String description = columns[2];
        String affectedHost = columns[3];

        LocalDateTime detectedAt = LocalDateTime.parse(columns[4]);
        boolean criticalHost = parseBoolean(columns[5]);
        String detail = columns[6];
        boolean flag1 = parseBoolean(columns[7]);
        boolean flag2 = parseBoolean(columns[8]);

        return switch (type) {
            case "BRUTE_FORCE" -> new BruteForceAlert(
                    id,
                    description,
                    affectedHost,
                    detectedAt,
                    criticalHost,
                    Integer.parseInt(detail),
                    flag1
            );

            case "MALWARE" -> new MalwareAlert(
                    id,
                    description,
                    affectedHost,
                    detectedAt,
                    criticalHost,
                    detail,
                    flag1
            );

            case "PHISHING" -> new PhishingAlert(
                    id,
                    description,
                    affectedHost,
                    detectedAt,
                    criticalHost,
                    detail,
                    flag1,
                    flag2
            );

            default -> throw new IllegalArgumentException(
                    "Type d'alerte inconnu : " + type
            );
        };
    }

    private boolean parseBoolean(String value) {
        if ("true".equals(value)) {
            return true;
        }

        if ("false".equals(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Valeur booléenne incorrecte : " + value
                        + ". Utiliser true ou false."
        );
    }
}