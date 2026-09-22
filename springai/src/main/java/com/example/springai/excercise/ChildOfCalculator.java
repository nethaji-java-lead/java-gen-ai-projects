package com.example.springai.excercise;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ChildOfCalculator {

    static void main() {
        Calculator calc = (a,b) -> a+b;
        System.out.println(calc.calculate(100, 200));

        Function<Integer, Integer> square = (num) -> num*num;
        System.out.println(square.apply(20));

        BiFunction<Integer, Integer, Integer> multiply = (num1, num2) -> num1*num2;
        System.out.println(multiply.apply(12, 12));

        Function<Integer, Integer> f1 = (num) -> num*2;

        Function<Integer, Integer> f2 = (num) -> num + 3;

        System.out.println(f2.compose(f1).apply(5));


    }

}
