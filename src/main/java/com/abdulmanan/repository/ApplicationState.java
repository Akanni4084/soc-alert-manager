package com.abdulmanan.repository;

import com.abdulmanan.model.Alert;
import com.abdulmanan.model.Analyst;

import java.io.Serializable;
import java.util.List;

public record ApplicationState(
        List<Alert> alerts,
        List<Analyst> analysts
) implements Serializable {

    public ApplicationState {
        alerts = List.copyOf(alerts);
        analysts = List.copyOf(analysts);
    }
}