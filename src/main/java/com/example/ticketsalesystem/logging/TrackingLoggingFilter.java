package com.example.ticketsalesystem.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrackingLoggingFilter extends OncePerRequestFilter {

    public static final String TRACKING_HEADER = "x-tracking-id";
    public static final String TRACKING_ATTRIBUTE = "xTrackingId";
    private static final Logger log = LoggerFactory.getLogger(TrackingLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String trackingId = request.getHeader(TRACKING_HEADER);
        if (trackingId == null || trackingId.isBlank()) {
            trackingId = UUID.randomUUID().toString();
        }

        request.setAttribute(TRACKING_ATTRIBUTE, trackingId);
        response.setHeader(TRACKING_HEADER, trackingId);
        MDC.put(TRACKING_HEADER, trackingId);
        String path = request.getRequestURI();

        log.info("ENTRY {} - x-tracking-id={}", path, trackingId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("EXIT {} - {} - x-tracking-id={}", path, response.getStatus(), trackingId);
            MDC.remove(TRACKING_HEADER);
        }
    }
}