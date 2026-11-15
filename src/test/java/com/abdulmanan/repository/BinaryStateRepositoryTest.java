package com.abdulmanan.repository;

import com.abdulmanan.model.Alert;
import com.abdulmanan.model.AlertHistoryEntry;
import com.abdulmanan.model.AlertStatus;
import com.abdulmanan.model.Analyst;
import com.abdulmanan.model.BruteForceAlert;
import com.abdulmanan.model.MalwareAlert;
import com.abdulmanan.model.PhishingAlert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinaryStateRepositoryTest {

    @TempDir
    Path temporaryDirectory;

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
    void shouldReturnEmptyWhenNoSaveExists() throws IOException {
        StateRepository repository = new BinaryStateRepository(
                temporaryDirectory.resolve("state.bin")
        );

        assertTrue(repository.load().isEmpty());
    }

    @Test
    void shouldRestoreAssignmentConclusionAndHistory() throws IOException {
        Path file = temporaryDirectory.resolve("data/state.bin");

        StateRepository writer = new BinaryStateRepository(file);

        Analyst analyst = new Analyst("ANA-001", "Analyste test", 5);
        Alert alert = createAlert("ALT-001");

        alert.assignTo(analyst);
        alert.startInvestigation();
        alert.resolve("Compte sécurisé après analyse.");

        writer.save(new ApplicationState(
                List.of(alert),
                List.of(analyst)
        ));

        StateRepository reader = new BinaryStateRepository(file);
        ApplicationState restored = reader.load().orElseThrow();

        assertEquals(1, restored.alerts().size());
        assertEquals(1, restored.analysts().size());

        Alert restoredAlert = restored.alerts().get(0);

        assertEquals("ALT-001", restoredAlert.getId());
        assertEquals(AlertStatus.RESOLVED, restoredAlert.getStatus());
        assertEquals(
                "Compte sécurisé après analyse.",
                restoredAlert.getConclusion()
        );

        assertEquals(alert.getDetectedAt(), restoredAlert.getDetectedAt());
        assertEquals(
                alert.calculateRiskScore(),
                restoredAlert.calculateRiskScore()
        );

        assertSame(
                restored.analysts().get(0),
                restoredAlert.getAssignedAnalyst()
        );

        List<AlertHistoryEntry> originalHistory = alert.getHistory();
        List<AlertHistoryEntry> restoredHistory = restoredAlert.getHistory();

        assertEquals(4, restoredHistory.size());

        for (int index = 0; index < originalHistory.size(); index++) {
            assertEquals(
                    originalHistory.get(index).getTimestamp(),
                    restoredHistory.get(index).getTimestamp()
            );

            assertEquals(
                    originalHistory.get(index).getAction(),
                    restoredHistory.get(index).getAction()
            );
        }
    }

    @Test
    void shouldPreserveAllAlertTypes() throws IOException {
        StateRepository repository = new BinaryStateRepository(
                temporaryDirectory.resolve("state.bin")
        );

        LocalDateTime date = LocalDateTime.of(2026, 11, 15, 10, 0);

        Alert bruteForce = createAlert("ALT-001");

        Alert malware = new MalwareAlert(
                "ALT-002",
                "Malware détecté",
                "poste-test",
                date,
                true,
                "DemoTrojan",
                false
        );

        Alert phishing = new PhishingAlert(
                "ALT-003",
                "Message suspect",
                "poste-direction",
                date,
                true,
                "support@example.com",
                true,
                true
        );

        repository.save(new ApplicationState(
                List.of(bruteForce, malware, phishing),
                List.of()
        ));

        ApplicationState restored = repository.load().orElseThrow();

        assertEquals(3, restored.alerts().size());

        BruteForceAlert restoredBruteForce = assertInstanceOf(
                BruteForceAlert.class,
                restored.alerts().get(0)
        );

        MalwareAlert restoredMalware = assertInstanceOf(
                MalwareAlert.class,
                restored.alerts().get(1)
        );

        PhishingAlert restoredPhishing = assertInstanceOf(
                PhishingAlert.class,
                restored.alerts().get(2)
        );

        assertEquals(60, restoredBruteForce.getFailedAttempts());
        assertTrue(restoredBruteForce.hasSuccessfulLogin());
        assertEquals(100, restoredBruteForce.calculateRiskScore());

        assertEquals("DemoTrojan", restoredMalware.getMalwareName());
        assertFalse(restoredMalware.isExecuted());
        assertEquals(60, restoredMalware.calculateRiskScore());

        assertEquals(
                "support@example.com",
                restoredPhishing.getSenderEmail()
        );
        assertTrue(restoredPhishing.isLinkClicked());
        assertTrue(restoredPhishing.isCredentialsSubmitted());
        assertEquals(100, restoredPhishing.calculateRiskScore());
    }

    @Test
    void shouldReplacePreviousSave() throws IOException {
        StateRepository repository = new BinaryStateRepository(
                temporaryDirectory.resolve("state.bin")
        );

        repository.save(new ApplicationState(
                List.of(createAlert("ALT-OLD")),
                List.of()
        ));

        repository.save(new ApplicationState(
                List.of(createAlert("ALT-NEW")),
                List.of()
        ));

        ApplicationState restored = repository.load().orElseThrow();

        assertEquals(1, restored.alerts().size());
        assertEquals("ALT-NEW", restored.alerts().get(0).getId());
    }

    @Test
    void shouldRejectCorruptedFile() throws IOException {
        Path file = temporaryDirectory.resolve("state.bin");
        Files.writeString(file, "Ceci n'est pas une sauvegarde Java.");

        StateRepository repository = new BinaryStateRepository(file);

        assertThrows(
                IOException.class,
                () -> repository.load()
        );
    }
}