package com.ticketing.server.dto;

import com.ticketing.server.domain.SeatStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        String eventTitle,
        String seatNumber,
        Long price,
        LocalDateTime reservedAt
) {}