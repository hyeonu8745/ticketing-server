package com.ticketing.server.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder // 🌟 빌더 패턴을 사용할 수 있게 해줍니다.
public record EventResponse(
        Long id,
        String title,
        String location,
        String posterUrl,
        LocalDateTime startTime,
        int totalSeats,
        long remainingSeats,
        String category,
        String description
) {}