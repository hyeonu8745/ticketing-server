package com.ticketing.server.dto;

public record SignupRequest(
        String email,
        String name,
        String password
) {}