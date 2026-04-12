package com.ticketing.server.service;

import com.ticketing.server.domain.*;
import com.ticketing.server.dto.KopisDetailResponse;
import com.ticketing.server.dto.KopisEventDto;
import com.ticketing.server.dto.KopisResponse;
import com.ticketing.server.repository.CategoryRepository;
import com.ticketing.server.repository.EventRepository;
import com.ticketing.server.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KopisService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final CategoryRepository categoryRepository;
    private final String KOPIS_API_KEY = "4c8b614b6c924e0cbe899bf85c6c4045"; // 여기에 현우 님의 API 키를 입력하세요

    @Transactional
    public void fetchAndSaveLargeEvents() {
        String url = "http://www.kopis.or.kr/openApi/restful/pblprfr?service=" + KOPIS_API_KEY
                + "&stdate=20260101&eddate=20261231&rows=100&cpage=10";

        RestTemplate restTemplate = new RestTemplate();
        KopisResponse response = restTemplate.getForObject(url, KopisResponse.class);

        if (response == null || response.events() == null) return;

        for (KopisEventDto dto : response.events()) {
            if (eventRepository.existsByKopisEventId(dto.kopisId())) continue;

            try {
                saveEventWithDetailAndSeats(dto);
            } catch (Exception e) {
                log.error("공연 저장 중 오류 발생 (ID: {}): {}", dto.kopisId(), e.getMessage());
            }
        }
    }

    private void saveEventWithDetailAndSeats(KopisEventDto listDto) throws Exception {
        String description = fetchDescription(listDto.kopisId());
        String categoryName = determineCategoryName(listDto.genre(), listDto.title(), description);

        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    String displayName = switch (categoryName) {
                        case "MUSICAL" -> "뮤지컬";
                        case "THEATER" -> "연극";
                        default -> "콘서트";
                    };
                    return categoryRepository.save(new Category(categoryName, displayName));
                });

        // 🌟 URL 정제: 주소가 꼬여있을 경우를 대비해 마지막 'http'부터 잘라냅니다.
        String rawUrl = listDto.posterUrl();
        String finalUrl = rawUrl;
        if (rawUrl != null && rawUrl.contains("http")) {
            finalUrl = rawUrl.substring(rawUrl.lastIndexOf("http"));
        }

        Event event = Event.builder()
                .title(listDto.title())
                .location(listDto.location())
                .posterUrl(finalUrl) // 🌟 정제된 URL 사용
                .startTime(LocalDateTime.parse(listDto.startDate().replace(".", "-") + "T19:30:00"))
                .totalSeats(100)
                .category(category)
                .description(description)
                .kopisEventId(listDto.kopisId())
                .build();

        eventRepository.save(event);
        generate100Seats(event);
    }

    private void generate100Seats(Event event) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            char rowName = (char) ('A' + i);
            for (int j = 1; j <= 10; j++) {
                seats.add(Seat.builder()
                        .event(event)
                        .seatNumber(rowName + String.valueOf(j))
                        .price(150000L)
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
    }

    /**
     * 1. KOPIS 상세 API를 호출하여 상세 설명(시간표)을 가져옵니다.
     */
    private String fetchDescription(String kopisId) {
        String detailUrl = "http://www.kopis.or.kr/openApi/restful/pblprfr/" + kopisId + "?service=" + KOPIS_API_KEY;
        RestTemplate restTemplate = new RestTemplate();
        try {
            KopisDetailResponse detailRes = restTemplate.getForObject(detailUrl, KopisDetailResponse.class);
            return (detailRes != null && detailRes.detail() != null) ? detailRes.detail().schedule() : "";
        } catch (Exception e) {
            log.error("상세 정보 호출 실패 (ID: {}): {}", kopisId, e.getMessage());
            return "";
        }
    }

    /**
     * 2. 장르, 제목, 상세 설명을 모두 분석하여 카테고리 코드(String)를 반환합니다.
     */
    private String determineCategoryName(String genre, String title, String description) {
        // 모든 텍스트를 하나로 합쳐서 대문자로 변환 후 검색
        String totalContent = (genre + " " + title + " " + description).toUpperCase();

        if (totalContent.contains("뮤지컬")) return "MUSICAL";
        if (totalContent.contains("연극") || totalContent.contains("소극장")) return "THEATER";

        // 기본값은 CONCERT
        return "CONCERT";
    }
}