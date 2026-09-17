package com.soufiane.eventservice.exception;

public class EventNotAvailableException extends RuntimeException{
    public EventNotAvailableException(String message) {
        super(message);
    }
}
