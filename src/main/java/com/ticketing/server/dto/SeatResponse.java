package com.ticketing.server.dto;

import com.ticketing.server.domain.SeatStatus;

public record SeatResponse(
        Long id,
        String seatNumber,
        SeatStatus status
) {}