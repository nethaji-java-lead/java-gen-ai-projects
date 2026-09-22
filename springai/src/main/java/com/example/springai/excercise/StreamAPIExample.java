package com.example.springai.excercise;

import java.util.Arrays;
import java.util.List;

public class StreamAPIExample {

    static void main() {
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8);
        List<Integer> evenNumbers = list.stream().filter((num) ->
        {
            System.out.println("Filtering the numbers");
            return num%2==0;
        }).map((num) ->
        {
            System.out.println("Mapping the numbers");
            return num * 2;
        }).toList();

        System.out.println(String.valueOf(evenNumbers));
    }
}
