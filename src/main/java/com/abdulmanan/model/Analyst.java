package com.abdulmanan.model;

import java.io.Serializable;

public class Analyst implements Serializable  {

    private final String id;
    private final String name;
    private final int maxActiveAlerts;

    public Analyst(String id, String name, int maxActiveAlerts) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant de l'analyste ne doit pas etre vide."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de l'analyste ne doit pas etre vide."
            );
        }

        if (maxActiveAlerts <= 0) {
            throw new IllegalArgumentException(
                    "La capacite de traitement doit etre positive."
            );
        }

        this.id = id.strip();
        this.name = name.strip();
        this.maxActiveAlerts = maxActiveAlerts;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxActiveAlerts() {
        return maxActiveAlerts;
    }

    @Override
    public String toString() {
        return id + " - " + name
                + " (capacite : " + maxActiveAlerts + ")";
    }
}