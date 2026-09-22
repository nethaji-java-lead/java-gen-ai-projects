package com.example.springai.excercise;

public interface InterfaceExample {

    void print();
    default void printName()
    {
        System.out.println("Hi, Nethaji");
    }

    static void printMessage()
    {
        System.out.println("Good Morning");
    }
}
