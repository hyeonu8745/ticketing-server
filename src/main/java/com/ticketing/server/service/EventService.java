package com.ticketing.server.service;

import com.ticketing.server.domain.SeatStatus;
import com.ticketing.server.dto.EventResponse;
import com.ticketing.server.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> {
                    long remainingSeats = event.getSeats().stream()
                            .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                            .count();

                    // 🌟 빌더 패턴으로 명확하게 매핑!
                    return EventResponse.builder()
                            .id(event.getId())
                            .title(event.getTitle())
                            .location(event.getLocation())   // 이제 제자리를 찾았습니다.
                            .posterUrl(event.getPosterUrl()) // 이미지도 제자리에!
                            .startTime(event.getStartTime())
                            .totalSeats(event.getTotalSeats())
                            .remainingSeats(remainingSeats)
                            .category(event.getCategory().getName())
                            .description(event.getDescription())
                            .build();
                })
                .collect(Collectors.toList());
    }
}