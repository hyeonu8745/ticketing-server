package com.ticketing.server.service;

import com.ticketing.server.domain.Seat;
import com.ticketing.server.domain.SeatStatus;
import com.ticketing.server.dto.EventResponse;
import com.ticketing.server.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAllByOrderByStartTimeAsc().stream()
                .map(event -> {
                    // 🌟 1. 해당 공연의 모든 좌석 가격을 추출 (중복 제거 및 정렬)
                    List<Long> prices = event.getSeats().stream()
                            .map(Seat::getPrice)
                            .distinct()
                            .sorted()
                            .toList();

                    // 🌟 2. 가격 표시 문자열 생성 (예: "120,000원 ~ 150,000원")
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
                            .remainingSeats(event.getSeats().stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count())
                            .category(event.getCategory().getDisplayName())
                            .description(event.getDescription())
                            .priceRange(priceRange) // 🌟 DTO에 가격 범위 추가
                            .build();
                })
                .toList();
    }
}