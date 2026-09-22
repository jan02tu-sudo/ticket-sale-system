package com.example.ticketsalesystem.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long id) {
        super("Event id: "+ id +" not found");
    }
}
