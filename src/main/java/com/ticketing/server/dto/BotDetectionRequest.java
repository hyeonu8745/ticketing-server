package com.ticketing.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * FastAPI 봇 탐지 서버로 보내는 요청 DTO
 * Python FastAPI는 snake_case를 기대하므로 @JsonProperty로 매핑
 */
@Getter
@Builder
public class BotDetectionRequest {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("event_id")
    private Long eventId;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("queue_wait_seconds")
    private double queueWaitSeconds;

    @JsonProperty("has_interaction")
    private boolean hasInteraction;

    @JsonProperty("cancel_count")
    private int cancelCount;

    @JsonProperty("success_count")
    private int successCount;
}