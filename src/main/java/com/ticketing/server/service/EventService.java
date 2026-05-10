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
    private final SearchKeywordExpander keywordExpander;

    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsPaging(String category, String keyword, Pageable pageable) {
        boolean hasCategory = org.springframework.util.StringUtils.hasText(category)
                && !"ALL".equalsIgnoreCase(category);
        boolean hasKeyword = org.springframework.util.StringUtils.hasText(keyword);

        Page<Event> eventPage;

        if (hasKeyword) {
            List<String> expanded = keywordExpander.expand(keyword);
            eventPage = searchByExpandedKeywords(expanded, hasCategory ? category : null, pageable);
        } else if (hasCategory) {
            eventPage = eventRepository.findByCategoryName(category, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }

        return eventPage.map(this::convertToResponse);
    }

    private Page<Event> searchByExpandedKeywords(List<String> keywords, String category, Pageable pageable) {
        Set<Long> seenIds = new LinkedHashSet<>();
        List<Event> merged = new ArrayList<>();

        for (String kw : keywords) {
            Page<Event> partial = (category != null)
                    ? eventRepository.findByCategoryNameAndKeyword(category, kw, pageable)
                    : eventRepository.findByKeywordAcrossFields(kw, pageable);

            for (Event ev : partial.getContent()) {
                if (seenIds.add(ev.getId())) merged.add(ev);
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), merged.size());
        if (start > merged.size()) return new PageImpl<>(List.of(), pageable, merged.size());

        return new PageImpl<>(merged.subList(start, end), pageable, merged.size());
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
                // 🌟 http 이미지를 백엔드 프록시 경유로 변환
                .posterUrl(toProxyUrl(event.getPosterUrl()))
                .startTime(event.getStartTime())
                .totalSeats(event.getTotalSeats())
                .remainingSeats(event.getSeats().stream()
                        .filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count())
                .category(event.getCategory().getDisplayName())
                .description(event.getDescription())
                .priceRange(priceRange)
                .build();
    }

    /**
     * KOPIS http 이미지 URL → 백엔드 프록시 URL 변환
     * 클라우드플레어 mixed content 차단 우회
     */
    private String toProxyUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) return originalUrl;
        if (originalUrl.startsWith("http://")) {
            return "/api/proxy/image?url=" + originalUrl;
        }
        return originalUrl; // 이미 https면 그대로
    }
}