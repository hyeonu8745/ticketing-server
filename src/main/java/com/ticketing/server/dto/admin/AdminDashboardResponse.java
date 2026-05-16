package com.ticketing.server.dto.admin;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminDashboardResponse(
        // 전체 카운트
        long totalUsers,
        long totalEvents,
        long visibleEvents,
        long totalReservations,
        long activeReservations,    // CONFIRMED
        long totalReviews,
        long totalRevenue,          // 누적 매출 (CONFIRMED 예매 금액 합산)

        // 카테고리별 공연 수
        List<CategoryCount> eventsByCategory,

        // 최근 7일 예매 추이
        List<DailyCount> reservationsLast7Days,

        // 인기 공연 TOP 5 (예매 수 기준)
        List<TopEvent> topEvents
) {
    @Builder
    public record CategoryCount(String category, long count) {}

    @Builder
    public record DailyCount(String date, long count) {}

    @Builder
    public record TopEvent(Long eventId, String title, long reservationCount) {}
}
