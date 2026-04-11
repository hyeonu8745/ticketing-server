package com.ticketing.server.dto;

public record QueueResponse(
        String status,    // 상태 (ACTIVE: 예매 가능, WAITING: 대기 중)
        Long position     // 내 앞의 대기자 수 (ACTIVE일 경우 0)
) {}