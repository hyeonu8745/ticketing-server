package com.ticketing.server.dto;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        LocalDateTime startTime,
        int totalSeats
) {}