package com.ticketing.server.service;

import com.ticketing.server.domain.*;
import com.ticketing.server.dto.*;
import com.ticketing.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class KopisService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final CategoryRepository categoryRepository;

    @Value("${kopis.api-key}")
    private String KOPIS_API_KEY;

    public void fetchAndSaveLargeEvents(int count) {
        // 1. 날짜 설정 (오늘부터 2026년 말까지)
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endOfYear = "20261231";

        // 2. KOPIS 제한(100개)에 맞춰 페이지 계산 (예: 1000개 요청 시 10페이지)
        int rowsPerPage = 100;
        int totalPages = (int) Math.ceil((double) count / rowsPerPage);

        log.info("🌐 KOPIS 동기화 시작 (총 {}개 요청, {}번 호출 예정)", count, totalPages);

        for (int page = 1; page <= totalPages; page++) {
            // 🌟 cpage를 1부터 totalPages까지 바꿔가며 100개씩 요청합니다.
            String url = "http://www.kopis.or.kr/openApi/restful/pblprfr?service=" + KOPIS_API_KEY
                    + "&stdate=" + today + "&eddate=" + endOfYear + "&rows=" + rowsPerPage + "&cpage=" + page;

            try {
                KopisResponse response = new RestTemplate().getForObject(url, KopisResponse.class);

                if (response == null || response.events() == null) {
                    log.warn("⚠️ {}페이지 데이터가 없습니다.", page);
                    continue;
                }

                for (KopisEventDto dto : response.events()) {
                    // 중복 데이터 체크
                    if (eventRepository.existsByKopisEventId(dto.kopisId())) continue;

                    try {
                        saveDetailedEvent(dto);
                    } catch (Exception e) {
                        log.error("❌ 저장 실패: {} | 사유: {}", dto.title(), e.getMessage());
                    }
                }
                log.info("🔄 데이터 수집 중... ({}/{} 페이지 완료)", page, totalPages);

            } catch (Exception e) {
                log.error("❌ {}페이지 호출 중 오류 발생: {}", page, e.getMessage());
            }
        }
        log.info("✅ 모든 데이터 동기화 프로세스 완료!");
    }

    @Transactional
    public void saveDetailedEvent(KopisEventDto listDto) {
        String detailUrl = "http://www.kopis.or.kr/openApi/restful/pblprfr/" + listDto.kopisId() + "?service=" + KOPIS_API_KEY;
        KopisDetailResponse detailRes = new RestTemplate().getForObject(detailUrl, KopisDetailResponse.class);
        KopisDetailDto d = (detailRes != null) ? detailRes.detail() : null;

        String description = "상세 정보가 준비 중입니다.";
        String rawSchedule = "상세 일정 참조";

        if (d != null) {
            rawSchedule = (d.schedule() != null && !d.schedule().isBlank()) ? d.schedule() : "정보 없음";
            if (d.synopsis() != null && !d.synopsis().isBlank()) {
                description = d.synopsis();
            } else if (d.styurls() != null && d.styurls().urlList() != null && !d.styurls().urlList().isEmpty()) {
                description = "[이미지 안내] " + d.styurls().urlList().get(0);
            }
        }

        LocalDateTime startTime = parseStartTime(listDto.startDate(), rawSchedule);

        List<Long> prices = parsePriceList(d != null ? d.priceInfo() : "");
        String categoryCode = determineCategory(listDto.genre(), listDto.title(), description);
        Category category = categoryRepository.findByName(categoryCode)
                .orElseGet(() -> categoryRepository.save(new Category(categoryCode,
                        categoryCode.equals("VISIT") ? "내한공연" : (categoryCode.equals("MUSICAL") ? "뮤지컬" : "콘서트"))));

        Event event = Event.builder()
                .title(listDto.title())
                .location(listDto.location())
                .posterUrl(listDto.posterUrl())
                .startTime(startTime)
                .playTime(rawSchedule)
                .category(category)
                .description(description)
                .runtime(d != null && d.runtime() != null ? d.runtime() : "정보없음")
                .rating(d != null && d.age() != null ? d.age() : "전체관람가")
                .cast(d != null && d.cast() != null ? d.cast() : "정보없음")
                .kopisEventId(listDto.kopisId())
                .totalSeats(100)
                .build();

        Event savedEvent = eventRepository.save(event);
        createGradedSeats(savedEvent, prices);
    }

    private LocalDateTime parseStartTime(String dateText, String scheduleText) {
        // null 체크 추가 (안전장치)
        if (dateText == null) dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        String baseDate = dateText.replace(".", "-");
        String foundTime = "19:30";
        if (scheduleText != null) {
            Matcher m = Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]").matcher(scheduleText);
            if (m.find()) foundTime = m.group();
        }
        return LocalDateTime.parse(baseDate + "T" + (foundTime.length() == 4 ? "0" + foundTime : foundTime) + ":00");
    }

    private List<Long> parsePriceList(String priceText) {
        List<Long> priceList = new ArrayList<>();
        if (priceText == null || priceText.isEmpty() || priceText.contains("무료")) return priceList;
        Matcher m = Pattern.compile("\\d{1,3}(,\\d{3})+").matcher(priceText);
        while (m.find()) priceList.add(Long.parseLong(m.group().replace(",", "")));
        return priceList.stream().distinct().sorted(java.util.Comparator.reverseOrder()).toList();
    }

    private void createGradedSeats(Event event, List<Long> prices) {
        long vip = !prices.isEmpty() ? prices.get(0) : 150000L;
        long r = prices.size() > 1 ? prices.get(1) : vip;
        long s = prices.size() > 2 ? prices.get(2) : r;

        List<Seat> seats = new ArrayList<>();
        for (char row = 'A'; row <= 'J'; row++) {
            long p = (row <= 'C') ? vip : (row <= 'G' ? r : s);
            String g = (row <= 'C') ? "VIP" : (row <= 'G' ? "R" : "S");
            for (int col = 1; col <= 10; col++) {
                seats.add(Seat.builder().event(event).seatNumber(row + String.valueOf(col))
                        .price(p).grade(g).status(SeatStatus.AVAILABLE).build());
            }
        }
        seatRepository.saveAll(seats);
    }

    private String determineCategory(String genre, String title, String desc) {
        String total = (genre + title + desc).toUpperCase();
        if (total.contains("내한") || total.contains("WORLD TOUR")) return "VISIT";
        if (total.contains("뮤지컬")) return "MUSICAL";
        if (total.contains("연극")) return "THEATER";
        return "CONCERT";
    }

    @Transactional
    public void deleteAllExistingData() {
        seatRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
    }
}