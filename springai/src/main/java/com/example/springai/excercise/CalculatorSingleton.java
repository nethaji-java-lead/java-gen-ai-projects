package com.example.springai.excercise;

import org.springframework.cache.annotation.Cacheable;

public class CalculatorSingleton {

    int a;
    int b;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int calculate()
    {
        return a+b;
    }

    private static CalculatorSingleton calculatorSingleton;

    private CalculatorSingleton() {

        System.out.println("Object Initialized");

    }

    public  static CalculatorSingleton getInstance()
    {
        if(calculatorSingleton==null)
        {
            synchronized(CalculatorSingleton.class)
            {
                if(calculatorSingleton==null)
                {
                    calculatorSingleton = new CalculatorSingleton();
                }
            }
        }

        return calculatorSingleton;
    }
}
