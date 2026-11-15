package com.abdulmanan.service;

import com.abdulmanan.exception.InvalidAlertOperationException;
import com.abdulmanan.model.Alert;
import com.abdulmanan.model.AlertStatus;
import com.abdulmanan.model.Analyst;
import com.abdulmanan.model.BruteForceAlert;
import com.abdulmanan.model.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class AlertServiceTest {

    private AlertService service;

    @BeforeEach
    void setUp() {
        service = new AlertService();

        service.addAnalyst(new Analyst(
                "ANA-001",
                "Analyste test",
                1
        ));

        service.addAlert(createAlert("ALT-001"));
        service.addAlert(createAlert("ALT-002"));
    }

    private Alert createAlert(String id) {
        return new BruteForceAlert(
                id,
                "Tentatives SSH suspectes",
                "serveur-test",
                LocalDateTime.of(2026, 11, 15, 10, 0),
                true,
                60,
                true
        );
    }

    @Test
    void shouldAssignAlertToRegisteredAnalyst() {
        service.assignAlert("ALT-001", "ANA-001");

        Alert alert = service.findAlertById("ALT-001");

        assertEquals(AlertStatus.ASSIGNED, alert.getStatus());
        assertSame(
                service.findAnalystById("ANA-001"),
                alert.getAssignedAnalyst()
        );
        assertEquals(
                1L,
                service.countActiveAlertsForAnalyst("ANA-001")
        );
    }

    @Test
    void shouldRejectAssignmentWhenCapacityIsReached() {
        service.assignAlert("ALT-001", "ANA-001");

        assertThrows(
                InvalidAlertOperationException.class,
                () -> service.assignAlert("ALT-002", "ANA-001")
        );

        Alert rejectedAlert = service.findAlertById("ALT-002");

        assertEquals(AlertStatus.NEW, rejectedAlert.getStatus());
        assertNull(rejectedAlert.getAssignedAnalyst());
        assertEquals(1, rejectedAlert.getHistory().size());
        assertEquals(
                1L,
                service.countActiveAlertsForAnalyst("ANA-001")
        );
    }

    @Test
    void shouldCountInvestigationAsActiveWork() {
        service.assignAlert("ALT-001", "ANA-001");
        service.startInvestigation("ALT-001");

        assertEquals(
                1L,
                service.countActiveAlertsForAnalyst("ANA-001")
        );

        assertThrows(
                InvalidAlertOperationException.class,
                () -> service.assignAlert("ALT-002", "ANA-001")
        );
    }

    @Test
    void shouldReleaseCapacityAfterResolution() {
        service.assignAlert("ALT-001", "ANA-001");
        service.startInvestigation("ALT-001");

        service.resolveAlert("ALT-001", "Incident traité.");

        assertEquals(
                0L,
                service.countActiveAlertsForAnalyst("ANA-001")
        );

        service.assignAlert("ALT-002", "ANA-001");

        assertEquals(
                AlertStatus.ASSIGNED,
                service.findAlertById("ALT-002").getStatus()
        );

        assertEquals(
                1L,
                service.countActiveAlertsForAnalyst("ANA-001")
        );
    }

    @Test
    void shouldReleaseCapacityAfterFalsePositive() {
        service.assignAlert("ALT-001", "ANA-001");
        service.startInvestigation("ALT-001");

        service.markAsFalsePositive(
                "ALT-001",
                "Test de connexion autorisé."
        );

        assertEquals(
                0L,
                service.countActiveAlertsForAnalyst("ANA-001")
        );

        service.assignAlert("ALT-002", "ANA-001");

        assertEquals(
                AlertStatus.ASSIGNED,
                service.findAlertById("ALT-002").getStatus()
        );
    }

    @Test
    void shouldRejectUnknownAnalystWithoutModifyingAlert() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.assignAlert("ALT-001", "ANA-UNKNOWN")
        );

        Alert alert = service.findAlertById("ALT-001");

        assertEquals(AlertStatus.NEW, alert.getStatus());
        assertNull(alert.getAssignedAnalyst());
        assertEquals(1, alert.getHistory().size());
    }

    @Test
    void shouldRejectUnknownAlert() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.assignAlert("ALT-UNKNOWN", "ANA-001")
        );

        assertEquals(
                0L,
                service.countActiveAlertsForAnalyst("ANA-001")
        );
    }

    @Test
    void shouldRejectDuplicateAlertIdentifier() {
        Alert original = service.findAlertById("ALT-001");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addAlert(createAlert("ALT-001"))
        );

        assertEquals(2, service.getAllAlerts().size());
        assertSame(original, service.findAlertById("ALT-001"));
    }

    @Test
    void shouldRejectDuplicateAnalystIdentifier() {
        Analyst original = service.findAnalystById("ANA-001");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addAnalyst(
                        new Analyst("ANA-001", "Autre analyste", 5)
                )
        );

        assertEquals(1, service.getAllAnalysts().size());
        assertSame(original, service.findAnalystById("ANA-001"));
    }

    @Test
    void shouldFilterAlertsByStatus() {
        service.assignAlert("ALT-001", "ANA-001");

        assertEquals(
                1,
                service.filterByStatus(AlertStatus.ASSIGNED).size()
        );

        assertEquals(
                "ALT-001",
                service.filterByStatus(AlertStatus.ASSIGNED)
                        .get(0).getId()
        );

        assertEquals(
                "ALT-002",
                service.filterByStatus(AlertStatus.NEW)
                        .get(0).getId()
        );

        assertTrue(
                service.filterByStatus(AlertStatus.RESOLVED).isEmpty()
        );
    }

    @Test
    void shouldFilterAlertsByPriority() {
        service.addAlert(new BruteForceAlert(
                "ALT-003",
                "Quelques échecs de connexion",
                "poste-test",
                LocalDateTime.of(2026, 11, 15, 11, 0),
                false,
                3,
                false
        ));

        assertEquals(
                2,
                service.filterByPriority(Priority.CRITICAL).size()
        );

        assertEquals(
                1,
                service.filterByPriority(Priority.LOW).size()
        );

        assertEquals(
                "ALT-003",
                service.filterByPriority(Priority.LOW)
                        .get(0).getId()
        );

        assertTrue(
                service.filterByPriority(Priority.MEDIUM).isEmpty()
        );
    }
}