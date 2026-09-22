package com.example.ticketsalesystem.event.dto;

import com.example.ticketsalesystem.event.Event;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventResponse(
        Long id,
        String name,
        OffsetDateTime date,
        String location,
        Integer totalTickets,
        Integer availableTickets,
        BigDecimal ticketPrice){

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDate(),
                event.getLocation(),
                event.getTotalTickets(),
                event.getAvailableTickets(),
                event.getTicketPrice()
        );
    }
}
