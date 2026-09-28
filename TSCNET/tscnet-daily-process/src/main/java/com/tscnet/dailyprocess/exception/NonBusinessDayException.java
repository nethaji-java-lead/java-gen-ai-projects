package com.tscnet.dailyprocess.exception;

public class NonBusinessDayException extends Exception {

    public NonBusinessDayException(String message)
    {
        super(message);
    }
}
