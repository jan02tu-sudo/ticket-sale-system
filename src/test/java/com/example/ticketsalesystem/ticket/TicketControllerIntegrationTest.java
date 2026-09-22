package com.example.ticketsalesystem.ticket;

import com.example.ticketsalesystem.event.Event;
import com.example.ticketsalesystem.event.EventRepository;
import com.example.ticketsalesystem.user.User;
import com.example.ticketsalesystem.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "ticket.pricing.threshold=10",
        "ticket.pricing.increase-percent=10"
})
@AutoConfigureMockMvc
@Transactional
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPurchaseTicketSuccessfully()
            throws Exception {

        String token = login("user", "user123");

        User user = userRepository.findByUsername("user")
                .orElseThrow();

        Event event = createEvent(
                3,
                3,
                "50.00"
        );

        long ticketsBefore = ticketRepository.count();

        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isCreated())

                .andExpect(header().string(
                        "Location",
                        matchesPattern(
                                "http://localhost/events/"
                                        + event.getId()
                                        + "/tickets/\\d+"
                        )
                ))

                .andExpect(header().string(
                        "X-Price-Changed",
                        "false"
                ))

                .andExpect(jsonPath("$.id").exists())

                .andExpect(jsonPath("$.eventId")
                        .value(event.getId()))

                .andExpect(jsonPath("$.userId")
                        .value(user.getId()))

                .andExpect(jsonPath("$.pricePaid")
                        .value(50.0));

        assertEquals(
                ticketsBefore + 1,
                ticketRepository.count()
        );

        Event updated = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                2,
                updated.getAvailableTickets()
        );
    }

    @Test
    void purchasedTicketShouldBeStoredForAuthenticatedUser()
            throws Exception {

        String token = login("user", "user123");

        User user = userRepository
                .findByUsername("user")
                .orElseThrow();

        Event event = createEvent(
                5,
                5,
                "20.00"
        );

        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isCreated());

        Ticket ticket = ticketRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        assertEquals(
                user.getId(),
                ticket.getUser().getId()
        );

        assertEquals(
                event.getId(),
                ticket.getEvent().getId()
        );

        assertNotNull(ticket.getPurchaseTime());
    }

    @Test
    void firstNineTicketsShouldNotChangePrice()
            throws Exception {

        String token = login("user", "user123");

        Event event = createEvent(
                20,
                20,
                "50.00"
        );

        for (int i = 0; i < 9; i++) {

            mockMvc.perform(post(
                            "/events/{eventId}/tickets",
                            event.getId()
                    )
                            .header(
                                    "Authorization",
                                    "Bearer " + token
                            ))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(
                            "X-Price-Changed",
                            "false"
                    ))
                    .andExpect(jsonPath("$.pricePaid")
                            .value(50.0));
        }

        Event updated = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("50.00")
                        .compareTo(updated.getTicketPrice())
        );

        assertEquals(
                11,
                updated.getAvailableTickets()
        );
    }

    @Test
    void tenthTicketShouldIncreasePrice()
            throws Exception {

        String token = login("user", "user123");

        Event event = createEvent(
                20,
                20,
                "50.00"
        );

        // tickets 1 - 9
        for (int i = 0; i < 9; i++) {

            mockMvc.perform(post(
                            "/events/{eventId}/tickets",
                            event.getId()
                    )
                            .header(
                                    "Authorization",
                                    "Bearer " + token
                            ))
                    .andExpect(status().isCreated());
        }

        // ticket 10
        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.pricePaid")
                        .value(50.0))

                .andExpect(header().string(
                        "X-Price-Changed",
                        "true"
                ))

                .andExpect(header().string(
                        "X-Previous-Ticket-Price",
                        "50.00"
                ))

                .andExpect(header().string(
                        "X-New-Ticket-Price",
                        "55.00"
                ));

        Event updated = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("55.00")
                        .compareTo(updated.getTicketPrice())
        );

        assertEquals(
                10,
                updated.getAvailableTickets()
        );
    }

    @Test
    void eleventhTicketShouldUseIncreasedPrice()
            throws Exception {

        String token = login("user", "user123");

        Event event = createEvent(
                11,
                11,
                "50.00"
        );

        // Sell ten tickets
        for (int i = 0; i < 10; i++) {

            mockMvc.perform(post(
                            "/events/{eventId}/tickets",
                            event.getId()
                    )
                            .header(
                                    "Authorization",
                                    "Bearer " + token
                            ))
                    .andExpect(status().isCreated());
        }

        // Ticket 11
        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.pricePaid")
                        .value(55.0))

                .andExpect(header().string(
                        "X-Price-Changed",
                        "false"
                ));
    }

    @Test
    void soldOutEventShouldReturn409()
            throws Exception {

        String token = login("user", "user123");

        Event event = createEvent(
                1,
                0,
                "50.00"
        );

        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isConflict());
    }

    @Test
    void secondPurchaseShouldFailWhenOnlyOneTicketExists()
            throws Exception {

        String token = login("user", "user123");

        Event event = createEvent(
                1,
                1,
                "50.00"
        );

        // Ticket #1 succeeds
        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isCreated());

        // Ticket #2 does not exist
        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isConflict());

        Event updated = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                0,
                updated.getAvailableTickets()
        );
    }

    @Test
    void nonexistentEventShouldReturn404()
            throws Exception {

        String token = login("user", "user123");

        mockMvc.perform(post(
                        "/events/999999/tickets"
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isNotFound());
    }

    @Test
    void purchaseWithoutAuthenticationShouldReturn401()
            throws Exception {

        Event event = createEvent(
                10,
                10,
                "50.00"
        );

        mockMvc.perform(post(
                        "/events/{eventId}/tickets",
                        event.getId()
                ))
                .andExpect(status().isUnauthorized());
    }

    private Event createEvent(
            int totalTickets,
            int availableTickets,
            String price
    ) {

        return eventRepository.save(new Event(
                "Test Event",
                OffsetDateTime.parse(
                        "2026-12-01T18:00:00+01:00"
                ),
                "Wien",
                totalTickets,
                availableTickets,
                new BigDecimal(price)
        ));
    }

    private String login(
            String username,
            String password
    ) throws Exception {

        String response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = Pattern
                .compile("\"token\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(response);

        assertTrue(
                matcher.find(),
                "Login response does not contain JWT: " + response
        );

        return matcher.group(1);
    }
}