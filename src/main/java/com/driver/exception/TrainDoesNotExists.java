package com.driver.exception;

public class TrainDoesNotExists extends RuntimeException{
    public TrainDoesNotExists(String message) {
        super(message);
    }
}
