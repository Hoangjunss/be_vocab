package com.toiec.demo.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static java.time.temporal.ChronoUnit.MINUTES;

class SrsPolicyTest {

    @Test
    @DisplayName("calculate - correct interval for quality 5 (easy), repetition 0")
    void calculate_Quality4_Repetition0() {
        SrsPolicy.SrsResult result = SrsPolicy.calculate(5, 0, 0, 2.5);

        assertThat(result.repetitions()).isEqualTo(1);
        assertThat(result.intervalDays()).isEqualTo(1);
        assertThat(result.easeFactor()).isCloseTo(2.6, org.assertj.core.data.Offset.offset(0.01));
        assertThat(result.nextReview()).isCloseTo(OffsetDateTime.now().plusDays(1), within(1, MINUTES));
    }

    @Test
    @DisplayName("calculate - correct interval for quality 3 (good), repetition 1")
    void calculate_Quality3_Repetition1() {
        SrsPolicy.SrsResult result = SrsPolicy.calculate(3, 1, 1, 2.5);

        assertThat(result.repetitions()).isEqualTo(2);
        assertThat(result.intervalDays()).isEqualTo(6);
        assertThat(result.easeFactor()).isCloseTo(2.44, org.assertj.core.data.Offset.offset(0.01));
        assertThat(result.nextReview()).isCloseTo(OffsetDateTime.now().plusDays(6), within(1, MINUTES));
    }

    @Test
    @DisplayName("calculate - correct interval for quality 4 (easy), repetition 2")
    void calculate_Quality4_Repetition2() {
        SrsPolicy.SrsResult result = SrsPolicy.calculate(4, 6, 2, 2.5);

        assertThat(result.repetitions()).isEqualTo(3);
        assertThat(result.intervalDays()).isEqualTo(15);
        assertThat(result.easeFactor()).isCloseTo(2.52, org.assertj.core.data.Offset.offset(0.01));
        assertThat(result.nextReview()).isCloseTo(OffsetDateTime.now().plusDays(15), within(1, MINUTES));
    }

    @Test
    @DisplayName("calculate - incorrect quality (<3) resets repetitions and reduces ease factor")
    void calculate_IncorrectQuality() {
        SrsPolicy.SrsResult result = SrsPolicy.calculate(2, 6, 2, 2.5);

        assertThat(result.repetitions()).isEqualTo(0);
        assertThat(result.intervalDays()).isEqualTo(1);
        assertThat(result.easeFactor()).isCloseTo(2.3, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("calculate - ease factor bounds (minimum 1.3)")
    void calculate_EaseFactorMinimumBound() {
        SrsPolicy.SrsResult result1 = SrsPolicy.calculate(2, 1, 0, 1.4);
        assertThat(result1.easeFactor()).isEqualTo(1.3); // max(1.3, 1.4 - 0.2) = max(1.3, 1.2) = 1.3

        SrsPolicy.SrsResult result2 = SrsPolicy.calculate(3, 1, 0, 1.3);
        // quality 3 -> easeFactor = 1.3 + (0.1 - (5-3)*0.08) = 1.3 + (0.1 - 0.16) = 1.3 - 0.06 = 1.24 -> min 1.3
        assertThat(result2.easeFactor()).isEqualTo(1.3);
    }

    @Test
    @DisplayName("calculate - max interval 365 days")
    void calculate_MaxInterval() {
        SrsPolicy.SrsResult result = SrsPolicy.calculate(4, 200, 5, 2.5);

        // Interval should be Math.round(200 * 2.5) = 500, but capped at 365
        assertThat(result.intervalDays()).isEqualTo(365);
    }

    @Test
    @DisplayName("calculate - quality 0 (complete blackout) resets everything")
    void calculate_Quality0_FullReset() {
        SrsPolicy.SrsResult result = SrsPolicy.calculate(0, 30, 5, 2.5);

        assertThat(result.repetitions()).isEqualTo(0);
        assertThat(result.intervalDays()).isEqualTo(1);
        assertThat(result.easeFactor()).isEqualTo(2.3);
    }
}
