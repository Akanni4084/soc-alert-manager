package com.abdulmanan.repository;

import com.abdulmanan.model.Analyst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CsvAnalystLoader implements AnalystLoader {

    private static final String HEADER = "id;name;maxActiveAlerts";

    @Override
    public List<Analyst> load(String resourceName) throws IOException {
        InputStream input = CsvAnalystLoader.class
                .getResourceAsStream("/" + resourceName);

        if (input == null) {
            throw new IOException(
                    "Fichier des analystes introuvable : " + resourceName
            );
        }

        List<Analyst> analysts = new ArrayList<>();
        Set<String> identifiers = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8)
        )) {
            String header = reader.readLine();

            if (!HEADER.equals(header)) {
                throw new IOException(
                        "L'en-tête du fichier des analystes est incorrect."
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
                    Analyst analyst = parseLine(line);

                    if (!identifiers.add(analyst.getId())) {
                        throw new IllegalArgumentException(
                                "Identifiant en double : " + analyst.getId()
                        );
                    }

                    analysts.add(analyst);
                } catch (IllegalArgumentException exception) {
                    throw new IOException(
                            "Erreur à la ligne " + lineNumber
                                    + " : " + exception.getMessage(),
                            exception
                    );
                }
            }
        }

        return analysts;
    }

    private Analyst parseLine(String line) {
        String[] columns = line.split(";", -1);

        if (columns.length != 3) {
            throw new IllegalArgumentException(
                    "Chaque ligne doit contenir exactement 3 colonnes."
            );
        }

        String id = columns[0].strip();
        String name = columns[1].strip();
        int maxActiveAlerts = Integer.parseInt(columns[2].strip());

        return new Analyst(id, name, maxActiveAlerts);
    }
}