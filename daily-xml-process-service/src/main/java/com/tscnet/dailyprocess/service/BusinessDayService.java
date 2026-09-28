package com.tscnet.dailyprocess.service;
import org.springframework.stereotype.Service; import java.time.*;
@Service public class BusinessDayService {
    public boolean isBusinessDay(LocalDate d){ return d.getDayOfWeek()!=DayOfWeek.SATURDAY && d.getDayOfWeek()!=DayOfWeek.SUNDAY; }
    public LocalDate applicableBusinessDate(LocalDate d){
        LocalDate x=d;
        while(!isBusinessDay(x))
            x=x.minusDays(1);
        return x;
    }
}
