package com.toiec.demo.policy;

import java.time.OffsetDateTime;
import static java.time.temporal.ChronoUnit.DAYS;

public class SrsPolicy {

    public static SrsResult calculate(int quality, int currentIntervalDays, int repetitions, double easeFactor) {
        if (quality >= 3) {  // correct
            if (repetitions == 0) {
                currentIntervalDays = 1;
            } else if (repetitions == 1) {
                currentIntervalDays = 6;
            } else {
                currentIntervalDays = (int) Math.round(currentIntervalDays * easeFactor);
            }
            repetitions++;
            easeFactor = easeFactor + (0.1 - (5 - quality) * 0.08);
            if (easeFactor < 1.3) easeFactor = 1.3;
        } else {  // incorrect
            currentIntervalDays = 1;
            repetitions = 0;
            easeFactor = Math.max(1.3, easeFactor - 0.2);
        }
        int maxDays = 365;
        int newInterval = Math.min(currentIntervalDays, maxDays);
        OffsetDateTime nextReview = OffsetDateTime.now().plusDays(newInterval);
        return new SrsResult(repetitions, newInterval, nextReview, easeFactor);
    }

    public record SrsResult(int repetitions, int intervalDays, OffsetDateTime nextReview, double easeFactor) {}
}