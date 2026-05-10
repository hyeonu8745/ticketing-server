package com.ticketing.server.service;

import com.ticketing.server.domain.Event;
import com.ticketing.server.domain.ReservationStatus;
import com.ticketing.server.domain.SeatStatus;
import com.ticketing.server.dto.RecommendationResponse;
import com.ticketing.server.repository.EventRepository;
import com.ticketing.server.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final String RECOMMEND_URL   = "http://localhost:8001/recommend";
    private static final String INDEX_URL        = "http://localhost:8001/index";

    private final RestTemplate restTemplate;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    // ──────────────────────────────────────────────
    // 1. 서버 시작 시 전체 공연 인덱싱
    // ──────────────────────────────────────────────
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void indexAllEventsOnStartup() {
        try {
            List<Event> events = eventRepository.findAll();
            if (events.isEmpty()) {
                log.info("[RECOMMEND] 인덱싱할 공연 없음 — 스킵");
                return;
            }

            List<Map<String, Object>> eventList = events.stream()
                    .map(this::toEventMap)
                    .toList();

            Map<String, Object> body = Map.of("events", eventList);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.exchange(
                    INDEX_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            log.info("✅ [RECOMMEND] {}개 공연 인덱싱 완료", events.size());

        } catch (Exception e) {
            // 추천 서버가 꺼져 있어도 메인 서버 시작에 영향 없음
            log.warn("[RECOMMEND] 인덱싱 실패 (추천 서버 미실행 가능): {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 2. 공연 상세 페이지 진입 시 추천 반환
    // ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendations(Long currentEventId, Long userId) {
        try {
            // 유저 예매 내역 조회
            List<Long> reservedEventIds = new ArrayList<>();
            List<String> reservedCategories = new ArrayList<>();

            if (userId != null) {
                reservationRepository
                        .findAllByUserIdAndStatusOrderByReservedAtDesc(userId, ReservationStatus.CONFIRMED)
                        .forEach(r -> {
                            reservedEventIds.add(r.getSeat().getEvent().getId());
                            reservedCategories.add(r.getSeat().getEvent().getCategory().getName());
                        });
            }

            // FastAPI 추천 요청
            Map<String, Object> body = new HashMap<>();
            body.put("userId",              userId != null ? userId : 0);
            body.put("currentEventId",      currentEventId);
            body.put("reservedEventIds",    reservedEventIds);
            body.put("reservedCategories",  reservedCategories);
            body.put("topK",                12);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
                    RECOMMEND_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    RecommendationResponse.class
            );

            RecommendationResponse result = response.getBody();
            log.info("✅ [RECOMMEND] eventId={} userId={} → {}개 추천",
                    currentEventId, userId,
                    result != null ? result.getRecommendations().size() : 0);

            return result;

        } catch (Exception e) {
            log.warn("[RECOMMEND] 추천 실패: {}", e.getMessage());
            return RecommendationResponse.empty(userId);
        }
    }

    // ──────────────────────────────────────────────
    // 유틸: Event 엔티티 → Map 변환
    // ──────────────────────────────────────────────
    private Map<String, Object> toEventMap(Event event) {
        // 가격 범위 계산
        List<Long> prices = event.getSeats().stream()
                .map(s -> s.getPrice())
                .distinct().sorted().toList();
        String priceRange = prices.isEmpty() ? "정보 없음"
                : prices.size() == 1
                  ? String.format("%,d원", prices.get(0))
                  : String.format("%,d원 ~ %,d원", prices.get(0), prices.get(prices.size() - 1));

        long remaining = event.getSeats().stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();

        Map<String, Object> map = new HashMap<>();
        map.put("id",             event.getId());
        map.put("title",          event.getTitle());
        map.put("category",       event.getCategory().getDisplayName());
        map.put("location",       event.getLocation());
        map.put("posterUrl",      event.getPosterUrl());
        map.put("description",    event.getDescription() != null ? event.getDescription() : "");
        map.put("priceRange",     priceRange);
        map.put("remainingSeats", remaining);
        map.put("totalSeats",     event.getTotalSeats());
        return map;
    }
}