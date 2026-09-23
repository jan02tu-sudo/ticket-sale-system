package com.example.ticketsalesystem.event;

import com.example.ticketsalesystem.event.dto.EventRequest;
import com.example.ticketsalesystem.event.dto.EventResponse;
import com.example.ticketsalesystem.exception.EventNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventResponse::from)
                .toList();
    }

    @Cacheable(
            cacheNames = "events",
            key = "#id"
    )
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long id) {
        Event event = findEvent(id);

        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {

        Event event = new Event(
                request.name(),
                request.date(),
                request.location(),
                request.totalTickets(),
                request.availableTickets(),
                request.ticketPrice()
        );

        Event savedEvent = eventRepository.save(event);

        return EventResponse.from(savedEvent);
    }

    @CachePut(
            cacheNames = "events",
            key = "#id"
    )
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {

        Event event = findEvent(id);

        event.setName(request.name());
        event.setDate(request.date());
        event.setLocation(request.location());
        event.setTotalTickets(request.totalTickets());
        event.setAvailableTickets(request.availableTickets());
        event.setTicketPrice(request.ticketPrice());

        return EventResponse.from(event);
    }

    @CacheEvict(
            cacheNames = "events",
            key = "#id"
    )
    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEvent(id);

        eventRepository.delete(event);
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }
}
