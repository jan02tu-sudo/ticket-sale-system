package com.example.ticketsalesystem.exception;

public class EventSoldOutException extends RuntimeException {

    public EventSoldOutException(Long eventId) {
        super("Event " + eventId + " is sold out");
    }
}