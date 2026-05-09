package com.ticketing.server.controller;

import com.ticketing.server.dto.EventResponse;
import com.ticketing.server.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * 무한 스크롤 및 카테고리 필터링 API
     * GET /api/events?category=MUSICAL&page=0&size=12
     */
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        Page<EventResponse> responses = eventService.getEventsPaging(category, keyword, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * ✅ 단건 조회 API 추가
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }
}