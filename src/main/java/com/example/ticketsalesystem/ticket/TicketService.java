package com.example.ticketsalesystem.ticket;

import com.example.ticketsalesystem.event.Event;
import com.example.ticketsalesystem.event.EventRepository;
import com.example.ticketsalesystem.exception.EventNotFoundException;
import com.example.ticketsalesystem.exception.EventSoldOutException;
import com.example.ticketsalesystem.user.User;
import com.example.ticketsalesystem.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final int priceThreshold;
    private final int priceIncreasePercent;

    public TicketService(
            TicketRepository ticketRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            @Value("${ticket.pricing.threshold}")
            int priceThreshold,
            @Value("${ticket.pricing.increase-percent}")
            int priceIncreasePercent
    ) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.priceThreshold = priceThreshold;
        this.priceIncreasePercent = priceIncreasePercent;
    }

    @CacheEvict(
            cacheNames = "events",
            key = "#eventId"
    )
    @Transactional
    public PurchaseResult purchaseTicket(
            Long eventId,
            String username){

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getAvailableTickets() <= 0) {
            throw new EventSoldOutException(eventId);
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        BigDecimal pricePaid = event.getTicketPrice();
        event.setAvailableTickets(event.getAvailableTickets() - 1);
        int soldTickets = event.getTotalTickets() - event.getAvailableTickets();
        boolean priceChanged = soldTickets % priceThreshold == 0;
        BigDecimal newPrice = pricePaid;

        if (priceChanged) {
            BigDecimal multiplier = BigDecimal.valueOf(100L + priceIncreasePercent).divide(BigDecimal.valueOf(100));
            newPrice = pricePaid.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            event.setTicketPrice(newPrice);
        }

        Ticket ticket = ticketRepository.save(
                new Ticket(
                        event,
                        user,
                        OffsetDateTime.now()
                )
        );

        TicketResponse response =
                new TicketResponse(
                        ticket.getId(),
                        event.getId(),
                        user.getId(),
                        ticket.getPurchaseTime(),
                        pricePaid
                );

        return new PurchaseResult(
                response,
                priceChanged,
                pricePaid,
                newPrice
        );
    }

    public record PurchaseResult(
            TicketResponse ticket,
            boolean priceChanged,
            BigDecimal previousPrice,
            BigDecimal newPrice){
    }
}