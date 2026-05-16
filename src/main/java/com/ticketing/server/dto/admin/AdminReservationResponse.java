package com.ticketing.server.dto.admin;

import com.ticketing.server.domain.Reservation;
import com.ticketing.server.domain.ReservationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminReservationResponse(
        Long reservationId,
        Long userId,
        String userEmail,
        String userName,
        Long eventId,
        String eventTitle,
        String seatNumber,
        String seatGrade,
        Long price,
        ReservationStatus status,
        LocalDateTime reservedAt
) {
    public static AdminReservationResponse from(Reservation r) {
        return AdminReservationResponse.builder()
                .reservationId(r.getId())
                .userId(r.getUser().getId())
                .userEmail(r.getUser().getEmail())
                .userName(r.getUser().getName())
                .eventId(r.getSeat().getEvent().getId())
                .eventTitle(r.getSeat().getEvent().getTitle())
                .seatNumber(r.getSeat().getSeatNumber())
                .seatGrade(r.getSeat().getGrade())
                .price(r.getSeat().getPrice())
                .status(r.getStatus())
                .reservedAt(r.getReservedAt())
                .build();
    }
}
