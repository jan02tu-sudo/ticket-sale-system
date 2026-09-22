package com.example.ticketsalesystem.ticket;

import com.example.ticketsalesystem.event.Event;
import com.example.ticketsalesystem.event.EventRepository;
import com.example.ticketsalesystem.security.LoginResponse;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        ticketRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @AfterEach
    void cleanAfterTest() {
        ticketRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    void concurrentPurchasesMustNotOversellLastTicket()
            throws Exception {

        Event event = eventRepository.saveAndFlush(
                new Event(
                        "Concurrency Test",
                        OffsetDateTime.parse(
                                "2026-12-01T18:00:00+01:00"
                        ),
                        "Wien",
                        1,
                        1,
                        new BigDecimal("50.00")
                )
        );

        String token = login();
        int numberOfBuyers = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfBuyers);
        CountDownLatch ready = new CountDownLatch(numberOfBuyers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfBuyers; i++) {

            futures.add(
                    executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        return mockMvc.perform(
                                post("/events/{id}/tickets", event.getId()).header("Authorization", "Bearer " + token))
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    }));
        }


        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statuses.add(future.get(10, TimeUnit.SECONDS));
        }

        executor.shutdown();
        long successfulPurchases = statuses.stream().filter(status -> status == 201).count();
        long conflicts = statuses.stream().filter(status -> status == 409).count();

        assertEquals(1, successfulPurchases);
        assertEquals(numberOfBuyers - 1, conflicts);
        assertEquals(1, ticketRepository.count());

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(0, updatedEvent.getAvailableTickets());
    }

    private String login() throws Exception {

        String response = mockMvc.perform(
                        post("/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "user",
                                          "password": "user123"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse loginResponse =
                objectMapper.readValue(
                        response,
                        LoginResponse.class
                );

        return loginResponse.token();
    }
}