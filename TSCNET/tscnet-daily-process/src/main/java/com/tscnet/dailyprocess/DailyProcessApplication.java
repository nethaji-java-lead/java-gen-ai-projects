package com.tscnet.dailyprocess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DailyProcessApplication {
    public static void main(String[] args) {
        SpringApplication.run(DailyProcessApplication.class, args);
    }
}
