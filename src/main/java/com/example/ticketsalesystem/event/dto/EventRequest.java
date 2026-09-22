package com.example.ticketsalesystem.event.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventRequest(
        @NotBlank
        String name,

        @NotNull
        OffsetDateTime date,

        @NotBlank
        String location,

        @NotNull
        @Positive
        Integer totalTickets,

        @NotNull
        @PositiveOrZero
        Integer availableTickets,

        @NotNull
        @PositiveOrZero
        BigDecimal ticketPrice) {

    @AssertTrue(message = "availableTikcets darf nicht größer sein als totalTickets")
    public boolean isTicketCountValid(){
        if (totalTickets == null || availableTickets == null){
            return true;
        }
        return availableTickets <= totalTickets;
    }
}
