package com.ticketing.server.dto.admin;

import com.ticketing.server.domain.User;
import com.ticketing.server.domain.UserRole;
import lombok.Builder;

@Builder
public record AdminUserResponse(
        Long id,
        String email,
        String name,
        UserRole role,
        Long point,
        long reservationCount    // 누적 예매 수 (CONFIRMED)
) {
    public static AdminUserResponse from(User u, long reservationCount) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .name(u.getName())
                .role(u.getRole())
                .point(u.getPoint())
                .reservationCount(reservationCount)
                .build();
    }
}
