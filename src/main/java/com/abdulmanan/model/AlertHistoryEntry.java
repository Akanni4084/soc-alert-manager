package com.abdulmanan.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.io.Serializable;

public class AlertHistoryEntry implements Serializable {

    private final LocalDateTime timestamp;
    private final String action;

    public AlertHistoryEntry(LocalDateTime timestamp, String action) {
        this.timestamp = Objects.requireNonNull(
                timestamp,
                "La date de l'action est obligatoire."
        );

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "L'action ne doit pas être vide."
            );
        }

        this.action = action.strip();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return timestamp.format(formatter) + " - " + action;
    }
}