package com.ticketing.server.dto.admin;

public record AdminEventUpdateRequest(
        String title,
        String location,
        String description
) {}
