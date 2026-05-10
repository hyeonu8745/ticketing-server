package com.ticketing.server.service;

import com.ticketing.server.dto.BotDetectionRequest;
import com.ticketing.server.dto.BotDetectionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

/**
 * FastAPI GraphSAGE 봇 탐지 서버 호출 서비스
 *
 * [Fail-Open 전략]
 * FastAPI 서버가 다운되거나 응답이 없을 때,
 * 예매를 막는 대신 통과시킵니다. (서비스 가용성 우선)
 * 필요하면 Fail-Closed(차단 우선)로 변경 가능.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BotDetectionService {

    private static final String BOT_DETECTION_URL = "http://localhost:8000/detect";

    // RestTemplate은 간단한 동기 호출에 적합
    // 실제 운영 시 WebClient(비동기) 또는 타임아웃 설정 권장
    private final RestTemplate restTemplate;

    /**
     * 봇 여부 판단 메인 메서드
     *
     * @return true = 봇 (차단), false = 정상 (허용)
     */
    public boolean isBot(BotDetectionRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<BotDetectionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BotDetectionResponse> response = restTemplate.exchange(
                    BOT_DETECTION_URL,
                    HttpMethod.POST,
                    entity,
                    BotDetectionResponse.class
            );

            BotDetectionResponse result = response.getBody();
            if (result == null) return false; // fail-open

            log.info("[BOT_CHECK] userId={} score={} isBot={} reason={}",
                    request.getUserId(), result.getBotScore(),
                    result.isBot(), result.getReason());

            return result.isBot();

        } catch (Exception e) {
            // FastAPI 서버 장애 시 → 차단하지 않음 (fail-open)
            log.warn("[BOT_CHECK_FAIL] 봇 탐지 서버 호출 실패, fail-open 처리: {}", e.getMessage());
            return false;
        }
    }
}