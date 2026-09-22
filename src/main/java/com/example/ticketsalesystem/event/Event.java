package com.example.ticketsalesystem.event;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "event_date", nullable = false)
    private OffsetDateTime date;

    @Column(nullable = false)
    private String location;

    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;

    @Column(name = "available_tickets", nullable = false)
    private Integer availableTickets;

    @Column(name = "ticket_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    public Event() {
    }

    public Event(
            String name,
            OffsetDateTime date,
            String location,
            Integer totalTickets,
            Integer availableTickets,
            BigDecimal ticketPrice
    ) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.totalTickets = totalTickets;
        this.availableTickets = availableTickets;
        this.ticketPrice = ticketPrice;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public Integer getTotalTickets() {
        return totalTickets;
    }

    public Integer getAvailableTickets() {
        return availableTickets;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }

    public void setAvailableTickets(Integer availableTickets) {
        this.availableTickets = availableTickets;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
}
