package com.ticketing.server.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final String code;    // 에러 코드 (예: ERR_ALREADY_RESERVED)
    private final String message; // 사용자에게 보여줄 메시지
}