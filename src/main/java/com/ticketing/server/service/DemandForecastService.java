package com.ticketing.server.service;

import com.ticketing.server.domain.Event;
import com.ticketing.server.dto.DemandForecastResponse;
import com.ticketing.server.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandForecastService {

    private static final String FORECAST_URL = "http://localhost:8002/forecast";

    private final RestTemplate restTemplate;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public DemandForecastResponse getForecast(Long eventId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("공연 없음"));

            // 잔여석 계산
            long remaining = event.getSeats().stream()
                    .filter(s -> s.getStatus() == com.ticketing.server.domain.SeatStatus.AVAILABLE)
                    .count();

            // FastAPI 요청 바디 구성
            Map<String, Object> body = new HashMap<>();
            body.put("eventId",             eventId);
            body.put("totalSeats",          event.getTotalSeats());
            body.put("remainingSeats",      (int) remaining);
            body.put("startTime",           event.getStartTime().toString());
            body.put("hourlyReservations",  List.of()); // 실제 이력 없으면 빈 리스트 → 서버에서 시뮬레이션

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<DemandForecastResponse> response = restTemplate.exchange(
                    FORECAST_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    DemandForecastResponse.class
            );

            DemandForecastResponse result = response.getBody();
            log.info("✅ [FORECAST] eventId={} 예매율={}% 인사이트={}",
                    eventId,
                    result != null ? result.getReservationRate() : "?",
                    result != null ? result.getInsight() : "?");

            return result;

        } catch (Exception e) {
            log.warn("[FORECAST] 수요 예측 실패: {}", e.getMessage());
            return DemandForecastResponse.empty(eventId);
        }
    }
}