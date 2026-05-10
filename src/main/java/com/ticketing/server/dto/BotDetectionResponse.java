package com.ticketing.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FastAPI 봇 탐지 서버 응답 DTO
 */
@Getter
@NoArgsConstructor
public class BotDetectionResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("is_bot")
    private boolean isBot;

    @JsonProperty("bot_score")
    private double botScore;        // 0.0(정상) ~ 1.0(봇)

    private String reason;

    private boolean blocked;
}