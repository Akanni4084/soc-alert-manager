package com.abdulmanan.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoreTest {

    private static final LocalDateTime DATE =
            LocalDateTime.of(2026, 11, 15, 10, 0);

    @ParameterizedTest
    @CsvSource({
            "1, false, false, 20",
            "9, false, false, 20",
            "10, false, false, 40",
            "49, false, false, 40",
            "50, false, false, 60",
            "50, true, false, 80",
            "50, false, true, 80",
            "50, true, true, 100"
    })
    void shouldCalculateBruteForceRisk(
            int failedAttempts,
            boolean criticalHost,
            boolean successfulLogin,
            int expectedScore
    ) {
        Alert alert = new BruteForceAlert(
                "ALT-001",
                "Tentatives SSH suspectes",
                "serveur-test",
                DATE,
                criticalHost,
                failedAttempts,
                successfulLogin
        );

        assertEquals(expectedScore, alert.calculateRiskScore());
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, 40",
            "true, false, 60",
            "false, true, 80",
            "true, true, 100"
    })
    void shouldCalculateMalwareRisk(
            boolean criticalHost,
            boolean executed,
            int expectedScore
    ) {
        Alert alert = new MalwareAlert(
                "ALT-002",
                "Malware détecté",
                "poste-test",
                DATE,
                criticalHost,
                "DemoTrojan",
                executed
        );

        assertEquals(expectedScore, alert.calculateRiskScore());
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, false, 20",
            "true, false, false, 40",
            "false, true, false, 40",
            "true, true, false, 60",
            "false, true, true, 80",
            "true, true, true, 100"
    })
    void shouldCalculatePhishingRisk(
            boolean criticalHost,
            boolean linkClicked,
            boolean credentialsSubmitted,
            int expectedScore
    ) {
        Alert alert = new PhishingAlert(
                "ALT-003",
                "Message suspect",
                "poste-test",
                DATE,
                criticalHost,
                "support@example.com",
                linkClicked,
                credentialsSubmitted
        );

        assertEquals(expectedScore, alert.calculateRiskScore());
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "-1"
    })
    void shouldRejectNonPositiveFailedAttempts(int failedAttempts) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BruteForceAlert(
                        "ALT-001",
                        "Tentatives SSH suspectes",
                        "serveur-test",
                        DATE,
                        false,
                        failedAttempts,
                        false
                )
        );
    }

    @Test
    void shouldRejectBlankMalwareName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MalwareAlert(
                        "ALT-002",
                        "Malware détecté",
                        "poste-test",
                        DATE,
                        false,
                        "   ",
                        false
                )
        );
    }

    @Test
    void shouldRejectBlankPhishingSender() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhishingAlert(
                        "ALT-003",
                        "Message suspect",
                        "poste-test",
                        DATE,
                        false,
                        "   ",
                        false,
                        false
                )
        );
    }

    @Test
    void shouldRejectCredentialsSubmittedWithoutLinkClick() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhishingAlert(
                        "ALT-003",
                        "Message suspect",
                        "poste-test",
                        DATE,
                        false,
                        "support@example.com",
                        false,
                        true
                )
        );
    }
}