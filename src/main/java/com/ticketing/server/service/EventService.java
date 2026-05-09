package com.ticketing.server.service;

import com.ticketing.server.domain.Event;
import com.ticketing.server.domain.Seat;
import com.ticketing.server.domain.SeatStatus;
import com.ticketing.server.dto.EventResponse;
import com.ticketing.server.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SearchKeywordExpander keywordExpander; // ✅ 동의어 확장기 주입

    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsPaging(String category, String keyword, Pageable pageable) {
        boolean hasCategory = org.springframework.util.StringUtils.hasText(category)
                && !"ALL".equalsIgnoreCase(category);
        boolean hasKeyword = org.springframework.util.StringUtils.hasText(keyword);

        Page<Event> eventPage;

        if (hasKeyword) {
            // ✅ 키워드 동의어 확장 후 통합 검색
            List<String> expanded = keywordExpander.expand(keyword);
            eventPage = searchByExpandedKeywords(expanded, hasCategory ? category : null, pageable);
        } else if (hasCategory) {
            eventPage = eventRepository.findByCategoryName(category, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }

        return eventPage.map(this::convertToResponse);
    }

    /**
     * 확장된 동의어 리스트로 통합 검색.
     * 같은 이벤트가 여러 키워드에 걸쳐 매칭될 수 있으므로 id 기준 중복 제거.
     */
    private Page<Event> searchByExpandedKeywords(List<String> keywords, String category, Pageable pageable) {
        Set<Long> seenIds = new LinkedHashSet<>();
        List<Event> merged = new ArrayList<>();

        for (String kw : keywords) {
            Page<Event> partial = (category != null)
                    ? eventRepository.findByCategoryNameAndKeyword(category, kw, pageable)
                    : eventRepository.findByKeywordAcrossFields(kw, pageable);

            for (Event ev : partial.getContent()) {
                if (seenIds.add(ev.getId())) {
                    merged.add(ev);
                }
            }
        }

        // 페이지네이션 재구성 (in-memory) — 결과가 수십~수백 건 규모 가정
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), merged.size());

        if (start > merged.size()) {
            return new PageImpl<>(List.of(), pageable, merged.size());
        }

        List<Event> sliced = merged.subList(start, end);
        return new PageImpl<>(sliced, pageable, merged.size());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 공연입니다."));
        return convertToResponse(event);
    }

    private EventResponse convertToResponse(Event event) {
        List<Long> prices = event.getSeats().stream()
                .map(Seat::getPrice)
                .distinct()
                .sorted()
                .toList();

        String priceRange = "가격 정보 없음";
        if (!prices.isEmpty()) {
            if (prices.size() == 1) {
                priceRange = String.format("%,d원", prices.get(0));
            } else {
                priceRange = String.format("%,d원 ~ %,d원", prices.get(0), prices.get(prices.size() - 1));
            }
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .location(event.getLocation())
                .posterUrl(event.getPosterUrl())
                .startTime(event.getStartTime())
                .totalSeats(event.getTotalSeats())
                .remainingSeats(event.getSeats().stream()
                        .filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count())
                .category(event.getCategory().getDisplayName())
                .description(event.getDescription())
                .priceRange(priceRange)
                .build();
    }
}