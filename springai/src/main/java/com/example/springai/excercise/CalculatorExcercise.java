package com.example.springai.excercise;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorExcercise {

    static void main() {

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                CalculatorSingleton calculator = CalculatorSingleton.getInstance();
            });
        }

        executor.shutdown();
    }
}
