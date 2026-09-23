package com.example.ticketsalesystem.security;

import com.example.ticketsalesystem.exception.ApiError;
import com.example.ticketsalesystem.logging.TrackingLoggingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
public class SecurityExceptionHandlers {

    private final ObjectMapper objectMapper;

    public SecurityExceptionHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) ->
                writeError(request, response, HttpStatus.UNAUTHORIZED, "Authentication is required.");
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) ->
                writeError(request, response, HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {

        Object tracking = request.getAttribute(TrackingLoggingFilter.TRACKING_ATTRIBUTE);
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                tracking == null ? null : tracking.toString());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}