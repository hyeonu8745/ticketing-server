package com.ticketing.server.dto;

public record LoginRequest(
        String email,
        String password
) {}