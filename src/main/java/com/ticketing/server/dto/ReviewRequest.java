package com.ticketing.server.dto;

public record ReviewRequest(
        int rating,
        String content
) {}
