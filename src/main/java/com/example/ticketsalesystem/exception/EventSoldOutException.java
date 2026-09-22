package com.example.ticketsalesystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventSoldOutException extends RuntimeException {

    public EventSoldOutException(Long eventId) {
        super("Event " + eventId + " is sold out");
    }
}