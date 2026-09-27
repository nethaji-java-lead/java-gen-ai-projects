package com.tscnet.dailyprocess.service;
import org.junit.jupiter.api.Test; import java.time.*; import static org.junit.jupiter.api.Assertions.*;
class BusinessDayServiceTest { private final BusinessDayService s=new BusinessDayService(); @Test void weekendIsNotBusinessDay(){assertFalse(s.isBusinessDay(LocalDate.of(2026,9,26)));} @Test void sundayMapsToFriday(){assertEquals(LocalDate.of(2026,9,25),s.applicableBusinessDate(LocalDate.of(2026,9,27)));} }
