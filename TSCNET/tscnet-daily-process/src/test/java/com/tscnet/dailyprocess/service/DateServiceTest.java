package com.tscnet.dailyprocess.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DateServiceTest {

    @InjectMocks
    private DateService dateService;

    @Test
    void isBusinessDay_ShouldReturnTrue_ForWeekdays() {
        // Monday: 2026-09-28
        LocalDate monday = LocalDate.of(2026, 9, 28);
        // Friday: 2026-10-02
        LocalDate friday = LocalDate.of(2026, 10, 2);

        assertTrue(dateService.isBusinessDay(monday));
        assertTrue(dateService.isBusinessDay(friday));
    }

    @Test
    void isBusinessDay_ShouldReturnFalse_ForWeekendDays() {
        // Saturday: 2026-09-26
        LocalDate saturday = LocalDate.of(2026, 9, 26);
        // Sunday: 2026-09-27
        LocalDate sunday = LocalDate.of(2026, 9, 27);

        assertFalse(dateService.isBusinessDay(saturday));
        assertFalse(dateService.isBusinessDay(sunday));
    }
}