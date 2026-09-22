package com.example.ticketsalesystem.ticket;

import com.example.ticketsalesystem.event.Event;
import com.example.ticketsalesystem.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "purchase_time", nullable = false)
    private OffsetDateTime purchaseTime;

    protected Ticket() {
    }

    public Ticket(
            Event event,
            User user,
            OffsetDateTime purchaseTime
    ) {
        this.event = event;
        this.user = user;
        this.purchaseTime = purchaseTime;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public User getUser() {
        return user;
    }

    public OffsetDateTime getPurchaseTime() {
        return purchaseTime;
    }
}