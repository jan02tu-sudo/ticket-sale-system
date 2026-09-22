package com.example.ticketsalesystem.ticket;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/events/{eventId}/tickets")
public class TicketController {
    private final TicketService ticketService;


    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> purchaseTicket(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Jwt jwt){

        var result = ticketService.purchaseTicket(eventId, jwt.getSubject());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{ticketId}")
                .buildAndExpand(result.ticket().id())
                .toUri();
        ResponseEntity.BodyBuilder response =
                ResponseEntity.created(location).header("X-Price-Changed", Boolean.toString(result.priceChanged()));
        if (result.priceChanged()) {response.header(
                            "X-Previous-Ticket-Price",
                            result.previousPrice().toString()).header(
                            "X-New-Ticket-Price",
                            result.newPrice().toString());
        }
        return response.body(result.ticket());
    }
}