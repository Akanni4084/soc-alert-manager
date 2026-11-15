package com.abdulmanan.repository;

import com.abdulmanan.model.Alert;
import com.abdulmanan.model.Analyst;
import com.abdulmanan.model.BruteForceAlert;
import com.abdulmanan.model.MalwareAlert;
import com.abdulmanan.model.PhishingAlert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvLoaderTest {

    @Test
    void shouldLoadAlertsFromCsv() throws IOException {
        AlertLoader loader = new CsvAlertLoader();

        List<Alert> alerts = loader.load("alerts.csv");

        assertEquals(4, alerts.size());

        BruteForceAlert firstAlert = assertInstanceOf(
                BruteForceAlert.class,
                alerts.get(0)
        );

        assertEquals("ALT-001", firstAlert.getId());
        assertEquals("serveur-production", firstAlert.getAffectedHost());
        assertEquals(
                LocalDateTime.of(2026, 11, 15, 8, 30),
                firstAlert.getDetectedAt()
        );
        assertTrue(firstAlert.isCriticalHost());
        assertEquals(60, firstAlert.getFailedAttempts());
        assertTrue(firstAlert.hasSuccessfulLogin());
        assertEquals(100, firstAlert.calculateRiskScore());

        MalwareAlert malware = assertInstanceOf(
                MalwareAlert.class,
                alerts.get(1)
        );

        assertEquals("DemoTrojan", malware.getMalwareName());
        assertFalse(malware.isExecuted());
        assertEquals(60, malware.calculateRiskScore());

        PhishingAlert phishing = assertInstanceOf(
                PhishingAlert.class,
                alerts.get(2)
        );

        assertEquals("support@example.com", phishing.getSenderEmail());
        assertTrue(phishing.isLinkClicked());
        assertTrue(phishing.isCredentialsSubmitted());
        assertEquals(100, phishing.calculateRiskScore());

        assertEquals("ALT-004", alerts.get(3).getId());
        assertEquals(20, alerts.get(3).calculateRiskScore());
    }

    @Test
    void shouldLoadAnalystsFromCsv() throws IOException {
        AnalystLoader loader = new CsvAnalystLoader();

        List<Analyst> analysts = loader.load("analysts.csv");

        assertEquals(3, analysts.size());
        assertEquals("ANA-001", analysts.get(0).getId());
        assertEquals("Abdul Manan", analysts.get(0).getName());
        assertEquals(5, analysts.get(0).getMaxActiveAlerts());

        assertEquals("ANA-002", analysts.get(1).getId());
        assertEquals(3, analysts.get(1).getMaxActiveAlerts());

        assertEquals("ANA-003", analysts.get(2).getId());
        assertEquals(2, analysts.get(2).getMaxActiveAlerts());
    }

    @Test
    void shouldRejectMissingAlertsFile() {
        AlertLoader loader = new CsvAlertLoader();

        assertThrows(
                IOException.class,
                () -> loader.load("missing-alerts.csv")
        );
    }

    @Test
    void shouldRejectMissingAnalystsFile() {
        AnalystLoader loader = new CsvAnalystLoader();

        assertThrows(
                IOException.class,
                () -> loader.load("missing-analysts.csv")
        );
    }

    @Test
    void shouldRejectInvalidBooleanAndReportLineNumber() {
        AlertLoader loader = new CsvAlertLoader();

        IOException exception = assertThrows(
                IOException.class,
                () -> loader.load("invalid-alerts.csv")
        );

        assertTrue(exception.getMessage().contains("ligne 2"));
        assertInstanceOf(
                IllegalArgumentException.class,
                exception.getCause()
        );
    }

    @Test
    void shouldRejectInvalidCapacityAndReportLineNumber() {
        AnalystLoader loader = new CsvAnalystLoader();

        IOException exception = assertThrows(
                IOException.class,
                () -> loader.load("invalid-analysts.csv")
        );

        assertTrue(exception.getMessage().contains("ligne 2"));
        assertInstanceOf(
                IllegalArgumentException.class,
                exception.getCause()
        );
    }
}