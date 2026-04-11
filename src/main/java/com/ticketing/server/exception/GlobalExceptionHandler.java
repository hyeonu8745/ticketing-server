package com.ticketing.server.exception;

import com.ticketing.server.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// 🌟 [수정] Swagger 패키지는 건드리지 말고, 현우 님의 컨트롤러 패키지만 지정하세요!
@RestControllerAdvice(basePackages = "com.ticketing.server.controller") // 🌟 이 한 줄이 핵심입니다!
public class GlobalExceptionHandler {

    // 1. 비즈니스 로직 중 발생하는 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("❌ 비즈니스 로직 에러 발생: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code("BIZ_ERROR")
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. 그 외 예상치 못한 모든 에러 처리 (500 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // [중요] Swagger 관련 500 에러가 계속 나면 여기서 로그를 찍어 원인을 볼 수 있습니다.
        log.error("🔥 서버 내부 에러 발생!", e);

        ErrorResponse response = ErrorResponse.builder()
                .code("SERVER_ERROR")
                .message("서버에서 알 수 없는 오류가 발생했습니다.")
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}