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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminShouldCreateEvent() throws Exception {

        String token = login("admin", "admin123");
        mockMvc.perform(post("/events")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Admin Event",
                                  "date": "2026-12-01T18:00:00+01:00",
                                  "location": "Wien",
                                  "totalTickets": 100,
                                  "availableTickets": 100,
                                  "ticketPrice": 50.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("Admin Event"));
    }

    @Test
    void normalUserShouldNotCreateEvent() throws Exception {

        String token = login("user", "user123");
        mockMvc.perform(post("/events")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Forbidden Event",
                                  "date": "2026-12-01T18:00:00+01:00",
                                  "location": "Wien",
                                  "totalTickets": 100,
                                  "availableTickets": 100,
                                  "ticketPrice": 50.00
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotCreateEvent() throws Exception {

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unauthorized Event",
                                  "date": "2026-12-01T18:00:00+01:00",
                                  "location": "Wien",
                                  "totalTickets": 100,
                                  "availableTickets": 100,
                                  "ticketPrice": 50.00
                                }
                                """))
                .andExpect(status().isUnauthorized());
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

        assertTrue(matcher.find());
        return matcher.group(1);
    }
}