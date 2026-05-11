package com.ticketing.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * ChatController.java
 * ====================
 * 고객센터 챗봇 프록시 컨트롤러
 *
 * React → Spring(/api/chat) → 파이참 챗봇 서버(localhost:8003)
 *
 * [SecurityConfig.java 에 아래 한 줄 추가 필요]
 * .requestMatchers("/api/chat/**").permitAll()
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    // 파이참에서 실행 중인 챗봇 서버 (로컬)
    private static final String CHATBOT_URL = "http://localhost:8003/chat";

    private final RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "질문을 입력해 주세요."));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                    Map.of("message", message), headers
            );

            ResponseEntity<Map> response = restTemplate.exchange(
                    CHATBOT_URL, HttpMethod.POST, entity, Map.class
            );

            log.info("[CHAT] 질문={} / 답변 완료",
                    message.length() > 30 ? message.substring(0, 30) + "..." : message);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            log.error("[CHAT] 챗봇 서버 호출 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "reply", "현재 고객센터 챗봇이 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해 주세요."
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "http://localhost:8003/health", Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "DOWN", "message", e.getMessage()));
        }
    }
}
