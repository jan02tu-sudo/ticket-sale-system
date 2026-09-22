package com.example.ticketsalesystem.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnEmptyEventListInitially() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldCreateEvent() throws Exception {
        String requestBody = """
                {
                  "name": "Vienna Gaming Event",
                  "date": "2026-11-20T18:00:00+01:00",
                  "location": "Wien",
                  "totalTickets": 1000,
                  "availableTickets": 1000,
                  "ticketPrice": 49.90
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        matchesPattern("http://localhost/events/\\d+")
                ))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Vienna Gaming Event"))
                .andExpect(jsonPath("$.location").value("Wien"))
                .andExpect(jsonPath("$.totalTickets").value(1000))
                .andExpect(jsonPath("$.availableTickets").value(1000))
                .andExpect(jsonPath("$.ticketPrice").value(49.9));
    }

    @Test
    void shouldRejectEventWhenTotalTicketsIsZero() throws Exception {
        String requestBody = """
                {
                  "name": "Invalid Event",
                  "date": "2026-11-20T18:00:00+01:00",
                  "location": "Wien",
                  "totalTickets": 0,
                  "availableTickets": 0,
                  "ticketPrice": 49.90
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEventWhenAvailableTicketsExceedTotalTickets() throws Exception {
        String requestBody = """
                {
                  "name": "Invalid Event",
                  "date": "2026-11-20T18:00:00+01:00",
                  "location": "Wien",
                  "totalTickets": 100,
                  "availableTickets": 150,
                  "ticketPrice": 49.90
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/events/999999"))
                .andExpect(status().isNotFound());
    }
}