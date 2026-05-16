package com.ticketing.server.dto;

import com.ticketing.server.domain.Review;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponse(
        Long id,
        Long eventId,
        String eventTitle,
        Long userId,
        String userName,     // 마스킹된 작성자 이름 (예: 김*수)
        int rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .eventId(r.getEvent().getId())
                .eventTitle(r.getEvent().getTitle())
                .userId(r.getUser().getId())
                .userName(maskName(r.getUser().getName()))
                .rating(r.getRating())
                .content(r.getContent())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    // 🌟 이름 마스킹: "김지현" → "김*현", "홍길동" → "홍*동", "AB" → "A*"
    private static String maskName(String name) {
        if (name == null || name.isBlank()) return "익명";
        if (name.length() == 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }
}
