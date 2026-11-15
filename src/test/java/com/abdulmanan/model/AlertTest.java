package com.abdulmanan.model;

import com.abdulmanan.exception.InvalidAlertOperationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    private Alert createAlert() {
        return new BruteForceAlert(
                "ALT-TEST",
                "Tentatives SSH suspectes",
                "serveur-test",
                LocalDateTime.of(2026, 11, 15, 10, 0),
                true,
                60,
                true
        );
    }

    private Analyst createAnalyst() {
        return new Analyst("ANA-TEST", "Analyste test", 5);
    }

    @Test
    void shouldAssignNewAlert() {
        Alert alert = createAlert();
        Analyst analyst = createAnalyst();

        alert.assignTo(analyst);

        assertEquals(AlertStatus.ASSIGNED, alert.getStatus());
        assertSame(analyst, alert.getAssignedAnalyst());
        assertEquals(2, alert.getHistory().size());
    }

    @Test
    void shouldRejectSecondAssignmentWithoutChangingHistory() {
        Alert alert = createAlert();
        Analyst firstAnalyst = createAnalyst();
        Analyst otherAnalyst = new Analyst("ANA-OTHER", "Autre analyste", 3);

        alert.assignTo(firstAnalyst);
        int historySize = alert.getHistory().size();

        assertThrows(
                InvalidAlertOperationException.class,
                () -> alert.assignTo(otherAnalyst)
        );

        assertSame(firstAnalyst, alert.getAssignedAnalyst());
        assertEquals(AlertStatus.ASSIGNED, alert.getStatus());
        assertEquals(historySize, alert.getHistory().size());
    }

    @Test
    void shouldRejectInvestigationBeforeAssignment() {
        Alert alert = createAlert();

        assertThrows(
                InvalidAlertOperationException.class,
                alert::startInvestigation
        );

        assertEquals(AlertStatus.NEW, alert.getStatus());
        assertEquals(1, alert.getHistory().size());
    }

    @Test
    void shouldStartInvestigationAfterAssignment() {
        Alert alert = createAlert();
        alert.assignTo(createAnalyst());

        alert.startInvestigation();

        assertEquals(AlertStatus.IN_PROGRESS, alert.getStatus());
        assertEquals(3, alert.getHistory().size());
    }

    @Test
    void shouldRejectResolutionBeforeInvestigation() {
        Alert alert = createAlert();
        alert.assignTo(createAnalyst());

        assertThrows(
                InvalidAlertOperationException.class,
                () -> alert.resolve("Analyse terminée.")
        );

        assertEquals(AlertStatus.ASSIGNED, alert.getStatus());
        assertNull(alert.getConclusion());
        assertEquals(2, alert.getHistory().size());
    }

    @Test
    void shouldRejectBlankConclusionWithoutClosingAlert() {
        Alert alert = createAlert();
        alert.assignTo(createAnalyst());
        alert.startInvestigation();

        assertThrows(
                IllegalArgumentException.class,
                () -> alert.resolve("   ")
        );

        assertEquals(AlertStatus.IN_PROGRESS, alert.getStatus());
        assertNull(alert.getConclusion());
        assertEquals(3, alert.getHistory().size());
    }

    @Test
    void shouldResolveAlertWithConclusion() {
        Alert alert = createAlert();
        alert.assignTo(createAnalyst());
        alert.startInvestigation();

        alert.resolve("  Compte sécurisé après analyse.  ");

        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
        assertEquals(
                "Compte sécurisé après analyse.",
                alert.getConclusion()
        );
        assertEquals(4, alert.getHistory().size());
        assertTrue(
                alert.getHistory().get(3).getAction()
                        .contains("Compte sécurisé après analyse.")
        );
    }

    @Test
    void shouldCloseAlertAsFalsePositive() {
        Alert alert = createAlert();
        alert.assignTo(createAnalyst());
        alert.startInvestigation();

        alert.markAsFalsePositive(
                "Tentatives provenant d'un test autorisé."
        );

        assertEquals(AlertStatus.FALSE_POSITIVE, alert.getStatus());
        assertEquals(
                "Tentatives provenant d'un test autorisé.",
                alert.getConclusion()
        );
        assertEquals(4, alert.getHistory().size());
    }

    @Test
    void shouldRejectChangesAfterResolution() {
        Alert alert = createAlert();
        alert.assignTo(createAnalyst());
        alert.startInvestigation();
        alert.resolve("Incident traité.");

        assertThrows(
                InvalidAlertOperationException.class,
                alert::startInvestigation
        );

        assertThrows(
                InvalidAlertOperationException.class,
                () -> alert.markAsFalsePositive("Nouvelle conclusion.")
        );

        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
        assertEquals("Incident traité.", alert.getConclusion());
        assertEquals(4, alert.getHistory().size());
    }

    @Test
    void shouldProtectHistoryFromExternalModification() {
        Alert alert = createAlert();

        assertThrows(
                UnsupportedOperationException.class,
                () -> alert.getHistory().clear()
        );

        assertEquals(1, alert.getHistory().size());
    }
}
