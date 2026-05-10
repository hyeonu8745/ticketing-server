package com.ticketing.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RecommendationResponse {

    private Long userId;
    private List<RecommendedEvent> recommendations;
    private String reason;

    // 추천 실패 시 빈 응답
    public static RecommendationResponse empty(Long userId) {
        RecommendationResponse r = new RecommendationResponse();
        r.userId = userId;
        r.recommendations = List.of();
        r.reason = "추천 데이터 없음";
        return r;
    }

    @Getter
    @NoArgsConstructor
    public static class RecommendedEvent {
        private Long id;
        private String title;
        private String location;
        private String posterUrl;
        private String category;
        private String priceRange;
        private int remainingSeats;
        private int totalSeats;
        private double similarityScore;
    }
}