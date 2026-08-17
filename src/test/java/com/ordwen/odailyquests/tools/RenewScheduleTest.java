package com.ordwen.odailyquests.tools;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenewScheduleTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void nextExecutionUsesDailyAnchor() {
        RenewSchedule.Settings settings = new RenewSchedule.Settings(
                LocalTime.of(3, 0), Duration.ofDays(1), UTC, 1);
        ZonedDateTime now = ZonedDateTime.of(2026, 8, 17, 1, 30, 0, 0, UTC);
        assertEquals(ZonedDateTime.of(2026, 8, 17, 3, 0, 0, 0, UTC),
                RenewSchedule.nextExecutionAtOrAfter(now, settings));
    }

    @Test
    void intervalCanRepeatWithinDay() {
        RenewSchedule.Settings settings = new RenewSchedule.Settings(
                LocalTime.MIDNIGHT, Duration.ofHours(2), UTC, 1);
        ZonedDateTime now = ZonedDateTime.of(2026, 8, 17, 5, 10, 0, 0, UTC);
        assertEquals(ZonedDateTime.of(2026, 8, 17, 6, 0, 0, 0, UTC),
                RenewSchedule.nextExecutionAtOrAfter(now, settings));
    }

    @Test
    void shouldRenewOnlyAfterARealBoundary() {
        RenewSchedule.Settings settings = new RenewSchedule.Settings(
                LocalTime.MIDNIGHT, Duration.ofDays(1), UTC, 1);
        ZonedDateTime last = ZonedDateTime.of(2026, 8, 17, 0, 5, 0, 0, UTC);
        assertFalse(RenewSchedule.shouldRenewSince(last,
                ZonedDateTime.of(2026, 8, 17, 23, 59, 0, 0, UTC), settings));
        assertTrue(RenewSchedule.shouldRenewSince(last,
                ZonedDateTime.of(2026, 8, 18, 0, 0, 0, 0, UTC), settings));
    }

    @Test
    void invalidZeroIntervalIsRejected() {
        RenewSchedule.Settings settings = new RenewSchedule.Settings(
                LocalTime.MIDNIGHT, Duration.ZERO, UTC, 1);
        assertFalse(RenewSchedule.isValid(settings));
    }
}
