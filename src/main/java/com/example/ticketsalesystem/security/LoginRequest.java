package com.example.ticketsalesystem.security;

public record LoginRequest(
        String username,
        String password) {
}