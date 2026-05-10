package com.ticketing.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DemandForecastResponse {

    private Long eventId;
    private String soldOutPrediction;   // 매진 예측 일시 (null이면 예측 기간 내 매진 없음)
    private Integer soldOutDaysLeft;    // 매진까지 남은 일수
    private String currentDemandLevel; // LOW | MEDIUM | HIGH | VERY_HIGH
    private List<HourlyDemand> hourlyDemands;
    private Double reservationRate;     // 현재 예매율 (%)
    private String insight;             // AI 한줄 인사이트

    public static DemandForecastResponse empty(Long eventId) {
        DemandForecastResponse r = new DemandForecastResponse();
        r.eventId = eventId;
        r.currentDemandLevel = "LOW";
        r.hourlyDemands = List.of();
        r.reservationRate = 0.0;
        r.insight = "예측 데이터를 불러오는 중입니다.";
        return r;
    }

    @Getter
    @NoArgsConstructor
    public static class HourlyDemand {
        private String hour;    // "14:00"
        private int demand;     // 예측 예매 수
        private String level;   // LOW | MEDIUM | HIGH | VERY_HIGH
        private String label;   // 여유 | 보통 | 혼잡 | 매우 혼잡
    }
}