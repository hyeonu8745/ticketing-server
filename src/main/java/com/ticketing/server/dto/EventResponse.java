package com.ticketing.server.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder // 🌟 빌더 패턴 적용
public record EventResponse(
        Long id,
        String title,
        String location,
        String posterUrl,
        LocalDateTime startTime,
        int totalSeats,
        long remainingSeats,
        String category,
        String description, // 🌟 상세 설명 포함
        String priceRange
) {}