package com.ticketing.server.dto;

public record ReviewSummaryResponse(
        Long eventId,
        long totalCount,
        double averageRating    // 소수 첫째자리까지
) {}
