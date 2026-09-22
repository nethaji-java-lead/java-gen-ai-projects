package com.example.springai.excercise;

public class ChildClass implements InterfaceExample
{
    @Override
    public void print() {
        printName();
        InterfaceExample.printMessage();
    }


    static void main() {
        ChildClass childClass = new ChildClass();
        childClass.print();
    }
}
