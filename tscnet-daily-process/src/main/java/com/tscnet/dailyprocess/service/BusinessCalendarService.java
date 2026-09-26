package com.tscnet.dailyprocess.service;

import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class BusinessCalendarService {

    /**
     * Demo rule: Monday-Friday are business days.
     * In production this should be backed by an approved regional holiday calendar.
     */
    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SUNDAY;
    }
}
