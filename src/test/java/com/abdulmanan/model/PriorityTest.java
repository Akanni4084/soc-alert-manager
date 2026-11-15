package com.abdulmanan.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriorityTest {

    @ParameterizedTest
    @CsvSource({
            "0, LOW",
            "29, LOW",
            "30, MEDIUM",
            "59, MEDIUM",
            "60, HIGH",
            "79, HIGH",
            "80, CRITICAL",
            "100, CRITICAL"
    })
    void shouldReturnPriorityForScoreBoundaries(
            int score,
            Priority expectedPriority
    ) {
        Priority actualPriority = Priority.fromScore(score);

        assertEquals(expectedPriority, actualPriority);
    }

    @Test
    void shouldRejectNegativeScore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Priority.fromScore(-1)
        );
    }

    @Test
    void shouldRejectScoreAboveOneHundred() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Priority.fromScore(101)
        );
    }
}