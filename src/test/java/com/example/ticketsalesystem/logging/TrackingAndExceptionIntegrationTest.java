package com.example.ticketsalesystem.logging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TrackingAndExceptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnProvidedTrackingId() throws Exception {

        mockMvc.perform(get("/events/999999").header("x-tracking-id", "test-tracking-123"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("x-tracking-id", "test-tracking-123"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/events/999999"))
                .andExpect(jsonPath("$.trackingId").value("test-tracking-123"));
    }

    @Test
    void shouldGenerateTrackingIdWhenHeaderIsMissing() throws Exception {

        mockMvc.perform(get("/events/999999")).andExpect(status().isNotFound())
                .andExpect(header().string("x-tracking-id", not(blankOrNullString())))
                .andExpect(jsonPath("$.trackingId").isNotEmpty());
    }

    @Test
    void securityErrorShouldAlsoContainTrackingId() throws Exception {

        mockMvc.perform(post("/events/1/tickets").header("x-tracking-id", "security-test-001"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("x-tracking-id", "security-test-001"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.trackingId").value("security-test-001"));
    }
}