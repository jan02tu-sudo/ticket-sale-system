package com.example.ticketsalesystem.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        String adminToken = login("admin", "admin123");

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
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
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
        String adminToken = login("admin", "admin123");

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
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEventWhenAvailableTicketsExceedTotalTickets() throws Exception {
        String adminToken = login("admin", "admin123");

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
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/events/999999"))
                .andExpect(status().isNotFound());
    }

    private String login(String username, String password) throws Exception {
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
                "Login response did not contain a JWT: " + response
        );

        return matcher.group(1);
    }
}