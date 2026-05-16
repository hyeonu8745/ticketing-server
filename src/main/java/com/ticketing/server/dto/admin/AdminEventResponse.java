package com.ticketing.server.dto.admin;

import com.ticketing.server.domain.Event;
import com.ticketing.server.domain.SeatStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminEventResponse(
        Long id,
        String title,
        String location,
        String posterUrl,
        LocalDateTime startTime,
        String category,
        int totalSeats,
        long reservedSeats,
        long remainingSeats,
        boolean visible,
        String description
) {
    public static AdminEventResponse from(Event e) {
        long reserved = e.getSeats().stream()
                .filter(s -> s.getStatus() == SeatStatus.RESERVED).count();
        long remaining = e.getSeats().stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();

        return AdminEventResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .location(e.getLocation())
                .posterUrl(e.getPosterUrl())
                .startTime(e.getStartTime())
                .category(e.getCategory() != null ? e.getCategory().getDisplayName() : "-")
                .totalSeats(e.getTotalSeats())
                .reservedSeats(reserved)
                .remainingSeats(remaining)
                .visible(e.isVisible())
                .description(e.getDescription())
                .build();
    }
}
