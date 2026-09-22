package com.example.ticketsalesystem.ticket;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TicketResponse(
        Long id,
        Long eventId,
        Long userId,
        OffsetDateTime purchaseTime,
        BigDecimal pricePaid){
}