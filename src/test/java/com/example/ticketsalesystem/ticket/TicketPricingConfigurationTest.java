package com.example.ticketsalesystem.ticket;

import com.example.ticketsalesystem.event.Event;
import com.example.ticketsalesystem.event.EventRepository;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "ticket.pricing.threshold=2",
        "ticket.pricing.increase-percent=10"
})
@AutoConfigureMockMvc
@Transactional
class TicketPricingConfigurationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void configuredThresholdShouldBeUsed()
            throws Exception {

        String token = login();

        Event event = eventRepository.save(
                new Event(
                        "Config Test",
                        OffsetDateTime.parse(
                                "2026-12-01T18:00:00+01:00"
                        ),
                        "Wien",
                        10,
                        10,
                        new BigDecimal("100.00")
                )
        );

        // First ticket: no increase
        mockMvc.perform(post(
                        "/events/{id}/tickets",
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
                ));

        // Second ticket:
        // threshold=2 means price must increase here
        mockMvc.perform(post(
                        "/events/{id}/tickets",
                        event.getId()
                )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "X-Price-Changed",
                        "true"
                ))
                .andExpect(header().string(
                        "X-New-Ticket-Price",
                        "110.00"
                ));
    }

    private String login() throws Exception {

        String response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "user123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = Pattern
                .compile("\"token\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(response);

        assertTrue(matcher.find());

        return matcher.group(1);
    }
}